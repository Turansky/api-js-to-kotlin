package com.github.turansky.yfiles.ide.binding

import com.github.turansky.yfiles.ide.binding.BindingDirective.TEMPLATE_BINDING
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext

internal class BindingReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(XmlAttributeValue::class.java),
            BindingReferenceProvider()
        )
    }
}

private class BindingReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<out PsiReference> {
        element as XmlAttributeValue

        if (!element.bindingEnabled)
            return PsiReference.EMPTY_ARRAY

        val binding = element.value.toBinding() as? TemplateBinding
            ?: return PsiReference.EMPTY_ARRAY

        val value = element.value
        val valueOffset = element.valueTextRange.startOffset - element.textRange.startOffset

        val key = TEMPLATE_BINDING.key
        val keyIndex = value.indexOf(key)
        val keyStartOffset = keyIndex + valueOffset

        val classReference = ContextClass(
            element = element,
            rangeInElement = TextRange.from(keyStartOffset, key.length),
            className = binding.parentReference
        )

        val name = binding.name
            ?: return arrayOf(classReference)

        val nameStartOffset = value.indexOf(name, keyIndex + key.length) + valueOffset

        val propertyReference = ContextProperty(
            element = element,
            rangeInElement = TextRange.from(nameStartOffset, name.length),
            className = binding.parentReference,
            propertyName = name
        )
        return arrayOf(
            classReference,
            propertyReference
        )
    }
}

private class ContextClass(
    element: XmlAttributeValue,
    rangeInElement: TextRange,
    private val className: String
) : PsiReferenceBase<XmlAttributeValue>(element, rangeInElement, true) {
    override fun resolve(): PsiElement? =
        findKotlinClass(element, className)
}

private class ContextProperty(
    element: XmlAttributeValue,
    rangeInElement: TextRange,
    private val className: String,
    private val propertyName: String
) : PsiReferenceBase<XmlAttributeValue>(element, rangeInElement, isValidContextParameter(propertyName)) {
    override fun resolve(): PsiElement? =
        when {
            isSoft -> findKotlinProperty(element, className, propertyName)
            else -> null
        }
}