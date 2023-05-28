package com.ndhzs.plugin.kotlin.ir

import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.shallowCopy
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.FqName

/**
 * .
 *
 * @author 985892345
 * 2023/5/26 17:50
 */
class KcpTestIrGenerationExtension(
  private val messageCollector: MessageCollector
) : IrGenerationExtension {
  
  @OptIn(FirIncompatiblePluginAPI::class)
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    val annotationTestA = FqName("TestA")
    val annotationTestB = FqName("TestB")
    moduleFragment.accept(object : IrElementTransformerVoid() {
      override fun visitClass(declaration: IrClass): IrStatement {
        if (declaration.hasAnnotation(annotationTestA)) {
          val annotation = declaration.getAnnotation(annotationTestA)!!
          log(annotation.dump())
          
          // 构造 TestB 注解
          val testBIrClassSymbol = pluginContext.referenceClass(annotationTestB)!!
          val testBImpl = IrConstructorCallImpl.fromSymbolOwner(
            type = testBIrClassSymbol.starProjectedType,
            constructorSymbol = testBIrClassSymbol.constructors.first(),
            origin = null,
          )
          
          // 复制 path 参数
          val valueArgument = annotation.getValueArgument(0)!!
          testBImpl.putValueArgument(0, valueArgument.shallowCopy())
          log("testBImpl: \n" + testBImpl.dump())
          
          // 添加 TestB 注解
          declaration.annotations = declaration.annotations + testBImpl
        }
        
        // 正常的 TestB 注解形式
        if (declaration.hasAnnotation(annotationTestB)) {
          val annotation = declaration.getAnnotation(annotationTestB)!!
          log("annotation: \n" + annotation.dump())
        }
        return super.visitClass(declaration)
      }
    }, null)
  }
  
  private fun IrClass.addAnnotation(annotation: IrClassSymbol, ) {
    val irConstructorCall = factory.run {
      IrConstructorCallImpl.fromSymbolOwner(
        type = annotation.starProjectedType,
        constructorSymbol = annotation.constructors.first(),
        origin = null,
      )
    }
    annotations = annotations + irConstructorCall
  }
  
  private fun log(msg: Any?) {
    messageCollector.report(
      CompilerMessageSeverity.WARNING,
      "${this.javaClass.simpleName} -> \n${msg.toString()}"
    )
  }
}