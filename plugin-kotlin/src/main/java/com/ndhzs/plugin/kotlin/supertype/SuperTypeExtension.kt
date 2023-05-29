package com.ndhzs.plugin.kotlin.supertype

import com.ndhzs.plugin.kotlin.BaseIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.hasEqualFqName
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.FqName

/**
 * .
 *
 * @author 985892345
 * 2023/5/29 11:00
 */
class SuperTypeExtension(
  messageCollector: MessageCollector
) : BaseIrGenerationExtension(messageCollector) {
  @OptIn(FirIncompatiblePluginAPI::class)
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    val iAbcTest = FqName("com.abc.IAbc")
    val iTestSymbol = pluginContext.referenceClass(FqName("ITest"))!!
    moduleFragment.transformChildrenVoid(object : IrElementTransformerVoid() {
      override fun visitClass(declaration: IrClass): IrStatement {
        if (declaration.hasEqualFqName(iAbcTest)) {
          // 给 IAbc 接口添加父接口
          declaration.superTypes = declaration.superTypes + iTestSymbol.defaultType
        }
        return super.visitClass(declaration)
      }
    })
    
  }
}