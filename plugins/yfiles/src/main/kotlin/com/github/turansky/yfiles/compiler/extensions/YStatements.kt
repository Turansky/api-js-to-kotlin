package com.github.turansky.yfiles.compiler.extensions

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.jsAssignment
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.prototypeOf
import org.jetbrains.kotlin.ir.backend.js.utils.Namer.JS_OBJECT_CREATE_FUNCTION
import org.jetbrains.kotlin.js.backend.ast.JsInvocation
import org.jetbrains.kotlin.js.backend.ast.JsStatement
import org.jetbrains.kotlin.js.backend.ast.JsStringLiteral
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.reference.ReferenceTranslator.translateAsValueReference
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name.identifier
import org.jetbrains.kotlin.resolve.DescriptorUtils.getFunctionByName

private val YCLASS_ID = ClassId(LANG_PACKAGE, YCLASS_NAME)
private val FIX_TYPE = identifier("fixType")

internal fun TranslationContext.fixType(
    descriptor: ClassDescriptor
): JsStatement {
    val yclassCompanion = currentModule.findClassAcrossModuleDependencies(YCLASS_ID)!!
        .companionObjectDescriptor!!

    val fixType = getFunctionByName(
        yclassCompanion.unsubstitutedMemberScope,
        FIX_TYPE
    )

    return JsInvocation(
        translateAsValueReference(fixType, this),
        translateAsValueReference(descriptor, this),
        JsStringLiteral(descriptor.name.identifier)
    ).makeStmt()
}

internal fun TranslationContext.setBaseClassPrototype(
    descriptor: ClassDescriptor,
    interfaces: List<ClassDescriptor>
): JsStatement {
    val arguments = interfaces
        .map { translateAsValueReference(it, this) }
        .toTypedArray()

    val baseClass = JsInvocation(
        findFunction(LANG_PACKAGE, BASE_CLASS_NAME),
        *arguments
    )

    val assignment = jsAssignment(
        prototypeOf(translateAsValueReference(descriptor, this)),
        JsInvocation(JS_OBJECT_CREATE_FUNCTION, baseClass)
    )

    return assignment.makeStmt()
}