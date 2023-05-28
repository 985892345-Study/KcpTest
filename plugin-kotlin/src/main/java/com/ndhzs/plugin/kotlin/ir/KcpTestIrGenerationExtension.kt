package com.ndhzs.plugin.kotlin.ir

import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.shallowCopy
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
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
    val testBIrClassSymbol = pluginContext.referenceClass(annotationTestB)!!
    val annotationTestC = FqName("TestC")
    val testCIrClassSymbol = pluginContext.referenceClass(annotationTestC)!!
    moduleFragment.transformChildrenVoid(object : IrElementTransformerVoid() {
      override fun visitClass(declaration: IrClass): IrStatement {
        if (declaration.hasAnnotation(annotationTestB)) {
          val annotation = declaration.getAnnotation(annotationTestB)!!
          // 打印正常的 TestB 注解形式
          log("正常的 @TestB: \n" + annotation.dump())
        }
        if (declaration.hasAnnotation(annotationTestA)) {
          val annotation = declaration.getAnnotation(annotationTestA)!!
          log("@TestA: \n" + annotation.dump())
          
          // 构造 TestB 注解
          val testBImpl = IrConstructorCallImpl.fromSymbolOwner(
            type = testBIrClassSymbol.starProjectedType, // 这个应该跟泛型有关系
            constructorSymbol = testBIrClassSymbol.constructors.first(), // 构造器相关
            origin = null, // 未知，但填 null 就对了
          )
    
          // 复制 path 参数，这个只表示变量值，不会包含变量名，变量名由 putValueArgument 中的 index 获取
          val pathArgument = annotation.getValueArgument(0)!!.shallowCopy()
          pathArgument.dump()
          testBImpl.putValueArgument(0, pathArgument)
          log("构造的 @TestB: \n" + testBImpl.dump())
    
          // 添加 @TestB 注解
          declaration.annotations = declaration.annotations + testBImpl
  
          // 构造 @TestC 注解
          val testCImpl = IrConstructorCallImpl.fromSymbolOwner(
            type = testCIrClassSymbol.starProjectedType,
            constructorSymbol = testCIrClassSymbol.constructors.first(),
            origin = null
          )
          // 自定义填充参数给 name
          testCImpl.putValueArgument(0, "自定义填充的参数".toIrConst(pluginContext.irBuiltIns.stringType))
          log("构造的 @TestC: \n" + testCImpl.dump())
  
          // 添加 @TestC 注解
          declaration.annotations = declaration.annotations + testCImpl
        }
        
        return super.visitClass(declaration)
      }
    })
  }
  
  private fun log(msg: Any?) {
    messageCollector.report(
      CompilerMessageSeverity.WARNING,
      "${this.javaClass.simpleName} -> \n${msg.toString()}"
    )
  }
  
  private fun <T> T.log2(msg: String = ""): T {
    if (msg.isEmpty()) log(this) else log("$msg   $this")
    return this
  }
}