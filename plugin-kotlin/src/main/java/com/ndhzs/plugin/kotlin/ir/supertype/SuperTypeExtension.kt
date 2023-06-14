package com.ndhzs.plugin.kotlin.ir.supertype

import com.ndhzs.plugin.kotlin.LogUtils
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.copyTo
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.hasEqualFqName
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * .
 *
 * @author 985892345
 * 2023/5/29 11:00
 */
class SuperTypeExtension(
  override val messageCollector: MessageCollector
) : IrGenerationExtension, LogUtils {
  @OptIn(FirIncompatiblePluginAPI::class)
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    val iTestName = FqName("ITest")
    val iTest1Name = FqName("ITest1")
    val iTest2Name = FqName("ITest2")
    val testImplClassName = FqName("TestImpl")
    val testImpl2ClassName = FqName("TestImpl2")
    
    val iTest1Symbol = pluginContext.referenceClass(iTest1Name)!!
    val iTest2Symbol = pluginContext.referenceClass(iTest2Name)!!
    
    moduleFragment.transformChildrenVoid(object : IrElementTransformerVoid() {
      override fun visitClass(declaration: IrClass): IrStatement {
        if (declaration.hasEqualFqName(testImpl2ClassName)) {
          // 用于参考的 TestImpl2 的实现
          declaration.declarations[2].dump().log2("TestImpl2 ->\n")
        }
        if (declaration.hasEqualFqName(iTestName)) {
          // ITest 添加 ITest1 接口
          declaration.superTypes = declaration.superTypes + iTest1Symbol.defaultType
        }
        if (declaration.hasEqualFqName(testImplClassName)) {
          // 构造 ITest1.set 方法的实现
          // 可以看官方的示例 FunctionNVarargBridgeLowering.kt
          val function = declaration.addFunction {
            name = Name.identifier("get") // 方法名
            returnType = pluginContext.irBuiltIns.stringType // 返回类型
            modality = Modality.OPEN // 类型修饰符
            visibility = DescriptorVisibilities.PUBLIC // 访问修饰符
          }.apply {
            val superFunction = iTest1Symbol.functions.single {
              it.owner.name == name
            }.owner
  
            // 实现的方法
            overriddenSymbols = overriddenSymbols + superFunction.symbol
            
            // 这个是 $this (后面那个 origin 经过比较发现 TestImpl2 中用的 DEFINED)
            dispatchReceiverParameter = declaration.thisReceiver!!.copyTo(this, IrDeclarationOrigin.DEFINED)
            
            // 参数
            valueParameters = valueParameters + superFunction.valueParameters.map { it.copyTo(this) }

            // 方法体
            body = DeclarationIrBuilder(pluginContext, symbol).irBlockBody {
              +irReturn(irGet(valueParameters[0]))
            }
          }
          
          function.dump().log2("ITest1.set ->\n")
          
          
          declaration.superTypes = declaration.superTypes + iTest2Symbol.defaultType
          val function2 = declaration.addFunction {
            name = Name.identifier("abc")
            returnType = pluginContext.irBuiltIns.stringType
            modality = Modality.OPEN
            visibility = DescriptorVisibilities.PUBLIC
          }.apply {
            val thisFunction = this
            val superFunction = iTest2Symbol.functions.single { it.owner.name == name }.owner
            overriddenSymbols = listOf(superFunction.symbol)
            dispatchReceiverParameter = declaration.thisReceiver!!.copyTo(
              thisFunction,
              origin = IrDeclarationOrigin.DEFINED,
            )
            // 因为 java 层带有其他东西，所以不能直接复制
            valueParameters = superFunction.valueParameters.map {
              it.copyTo(
                thisFunction,
                origin = IrDeclarationOrigin.DEFINED,
                type = iTest1Symbol.defaultType
              ).apply { annotations = emptyList() } // java 层带有 @NotNull，需要取消掉
            }
            body = DeclarationIrBuilder(pluginContext, symbol).irBlockBody {
              +irReturn(irCall(function.symbol).apply {
                dispatchReceiver = irGet(dispatchReceiverParameter!!)
                putValueArgument(0, irCall(
                  iTest1Symbol.functions.single {
                    it.owner.name.asString() == "toString"
                  }
                ).also { it.dispatchReceiver = irGet(valueParameters[0]) })
              })
            }
          }
          function2.dump().log2("ITest1.abc ->\n")
        }
        return super.visitClass(declaration)
      }
    })
  }
}