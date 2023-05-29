package com.ndhzs.plugin.kotlin.annotation

import com.ndhzs.plugin.kotlin.BaseIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
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
import org.jetbrains.kotlin.ir.util.hasEqualFqName
import org.jetbrains.kotlin.ir.util.isInterface
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
class AnnotationExtension(
  messageCollector: MessageCollector
) : BaseIrGenerationExtension(messageCollector) {
  
  @OptIn(FirIncompatiblePluginAPI::class)
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    val annotation1Name = FqName("Annotation1")
    val annotation2Name = FqName("Annotation2")
    val javaAnnotationName = FqName("JavaAnnotation")
    val testClass1Name = FqName("TestClass1")
    
    val annotation1Symbol = pluginContext.referenceClass(annotation1Name)!!
    val annotation2Symbol = pluginContext.referenceClass(annotation2Name)!!
    val javaAnnotationSymbol = pluginContext.referenceClass(javaAnnotationName)!!
    
    moduleFragment.transformChildrenVoid(object : IrElementTransformerVoid() {
      override fun visitClass(declaration: IrClass): IrStatement {
        if (declaration.hasEqualFqName(testClass1Name)) {
          // 给 TestClass1 添加 @Annotation1 注解
          
          // 构造 @Annotation1
          val annotation1 = IrConstructorCallImpl.fromSymbolOwner(
            type = annotation1Symbol.starProjectedType, // 这个应该跟泛型有关系
            constructorSymbol = annotation1Symbol.constructors.first(), // 构造器相关
            origin = null, // 未知，但填 null 就对了
          )
  
          // 生成自定义的参数
          val pathArgument1 = "自定义填充的参数".toIrConst(pluginContext.irBuiltIns.stringType)
          
          // 填充参数
          annotation1.putValueArgument(0, pathArgument1)
  
          // 添加 @Annotation1 注解
          declaration.annotations = declaration.annotations + annotation1
          
          
          // 给 TestClass1 添加 @JavaAnnotation 注解
          // 构造 @JavaAnnotation
          val javaAnnotation = IrConstructorCallImpl.fromSymbolOwner(
            type = javaAnnotationSymbol.starProjectedType, // 这个应该跟泛型有关系
            constructorSymbol = javaAnnotationSymbol.constructors.first(), // 构造器相关
            origin = null, // 未知，但填 null 就对了
          )
          
          // 复制 pathArgument1 参数
          val pathArgument2 = pathArgument1.shallowCopy()
          
          // 填充参数
          javaAnnotation.putValueArgument(0, pathArgument2)
          
          // 添加 @Annotation1 注解
          declaration.annotations = declaration.annotations + javaAnnotation
        }
        
        
        // 给带有 @Annotation1 的添加 @Annotation2 注解 (这里也会包含 TestClass1，因为上面添加了 @Annotation1)
        if (declaration.hasAnnotation(annotation1Name)) {
          val annotation1 = declaration.getAnnotation(annotation1Name)!!
          
          // 获取 @Annotation1 参数
          val annotation1PathArgument = annotation1.getValueArgument(0)!!
          
          // 构造 @Annotation2
          val annotation2 = IrConstructorCallImpl.fromSymbolOwner(
            type = annotation2Symbol.starProjectedType, // 这个应该跟泛型有关系
            constructorSymbol = annotation2Symbol.constructors.first(), // 构造器相关
            origin = null, // 未知，但填 null 就对了
          )
          
          // 把 @Annotation1 的参数填充到 @Annotation2 上，这个 index 决定了变量名
          annotation2.putValueArgument(0, annotation1PathArgument.shallowCopy())
          
          // 添加 @Annotation2 注解
          declaration.annotations = declaration.annotations + annotation2
        }
        return super.visitClass(declaration)
      }
    })
  }
}