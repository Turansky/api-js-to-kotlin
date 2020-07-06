package com.github.turansky.yfiles.compiler.extensions

import com.github.turansky.yfiles.compiler.backend.ir.YCastTransformer
import com.github.turansky.yfiles.compiler.backend.ir.YClassTransformer
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

internal class YTransformExtension : IrGenerationExtension {
    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext
    ) {
        val castLowering = YCastTransformer()
        moduleFragment.transformChildrenVoid(castLowering)

        val classTransformer = YClassTransformer(pluginContext)
        moduleFragment.transformChildrenVoid(classTransformer)
    }
}