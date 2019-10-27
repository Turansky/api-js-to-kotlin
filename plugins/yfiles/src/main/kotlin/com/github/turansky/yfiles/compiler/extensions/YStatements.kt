package com.github.turansky.yfiles.compiler.extensions

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.jsAssignment
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.prototypeOf
import org.jetbrains.kotlin.ir.backend.js.utils.Namer.JS_OBJECT_CREATE_FUNCTION
import org.jetbrains.kotlin.js.backend.ast.JsInvocation
import org.jetbrains.kotlin.js.backend.ast.JsNameRef
import org.jetbrains.kotlin.js.backend.ast.JsStatement
import org.jetbrains.kotlin.js.backend.ast.JsStringLiteral
import org.jetbrains.kotlin.js.translate.context.TranslationContext
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
        toValueReference(fixType),
        toValueReference(descriptor),
        JsStringLiteral(descriptor.name.identifier)
    ).makeStmt()
}

internal fun TranslationContext.setBaseClassPrototype(
    descriptor: ClassDescriptor,
    interfaces: List<ClassDescriptor>
): JsStatement {
    val arguments = interfaces
        .map { toValueReference(it) }
        .toTypedArray()

    val baseClass = JsInvocation(
        findFunction(LANG_PACKAGE, BASE_CLASS_NAME),
        *arguments
    )

    val assignment = jsAssignment(
        prototypeOf(toValueReference(descriptor)),
        JsInvocation(JS_OBJECT_CREATE_FUNCTION, prototypeOf(baseClass))
    )

    return assignment.makeStmt()
}

internal fun TranslationContext.reassignConstructor(
    descriptor: ClassDescriptor
): JsStatement {
    val clazz = toValueReference(descriptor)

    val assignment = jsAssignment(
        JsNameRef("constructor", prototypeOf(clazz)),
        clazz
    )

    return assignment.makeStmt()
}