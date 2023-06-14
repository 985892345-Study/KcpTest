package com.ndhzs.plugin.kotlin.app.ir

import com.ndhzs.plugin.kotlin.LogUtils
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.serialization.findPackage
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.FqName

/**
 * .
 *
 * @author 985892345
 * 2023/6/1 21:36
 */
class AppIrExtension(
  override val messageCollector: MessageCollector
) : IrGenerationExtension, LogUtils {
  @OptIn(FirIncompatiblePluginAPI::class)
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    val serviceProviderAnnotation = FqName("com.g985892345.service.provider.ServiceProvider")
    val iServiceProvider = FqName("com.g985892345.service.provider.IServiceProvider")
    val testAnnotation = FqName("com.ndhzs.kcptest.annotation.TestAnnotation")
    val serviceProviderImpl = FqName("com.ndhzs.kcptest.service.ServiceProviderImpl")
    val testAnnotationSymbol = pluginContext.referenceClass(testAnnotation)!!
    moduleFragment.transformChildrenVoid(object : IrElementTransformerVoid() {
      override fun visitClass(declaration: IrClass): IrStatement {
        log("class = ${declaration.name}")
        if (declaration.hasAnnotation(serviceProviderAnnotation)) {
          declaration.annotations = declaration.annotations + IrConstructorCallImpl.fromSymbolOwner(
            type = testAnnotationSymbol.starProjectedType, // 这个应该跟泛型有关系
            constructorSymbol = testAnnotationSymbol.constructors.first(), // 构造器相关
            origin = null, // 未知，但填 null 就对了
          )
        }
        return super.visitClass(declaration)
      }
    })
  }
}