package com.ndhzs.plugin.kotlin.ir.superclass

import com.ndhzs.plugin.kotlin.LogUtils
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irBlock
import org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionExpressionOrBuilder
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.AnonymousFunctionDescriptor
import org.jetbrains.kotlin.fir.expressions.builder.buildLambdaArgumentExpression
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.IrFunctionBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irFunctionReference
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrFunctionImpl
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.symbols.impl.IrSimpleFunctionSymbolImpl
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.isAny
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.copyTo
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.hasEqualFqName
import org.jetbrains.kotlin.ir.util.irConstructorCall
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.isFunctionOrKFunction
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.isOverridable
import org.jetbrains.kotlin.ir.util.overrides
import org.jetbrains.kotlin.ir.util.setDeclarationsParent
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtElementImpl
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement

/**
 * .
 *
 * @author 985892345
 * 2023/6/14 14:51
 */
class SuperClassExtension(
  override val messageCollector: MessageCollector
) : IrGenerationExtension, LogUtils {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    val child1Symbol = FqName("Child1")
    val child2Symbol = FqName("Child2")
    moduleFragment.transformChildrenVoid(object : IrElementTransformerVoid() {
      override fun visitClass(declaration: IrClass): IrStatement {
        val superClass = declaration.superClass
//        if (superClass != null && superClass.hasEqualFqName(parentSymbol)) {
//          val initFun = declaration.functions.single { it.name.asString() == "init" }
//          log(
//            "${declaration.name}\n" +
//              "${initFun.origin}\n" +
//              "${initFun.dump()}\n"
//          )
//        }
//        if (declaration.hasEqualFqName(child3Symbol) || declaration.hasEqualFqName(child4Symbol)) {
//          log(
//            "${declaration.name}\n" +
//              "${declaration.functions.single { it.name.asString() == "init" }.dump()}\n"
//          )
//        }
        if (declaration.hasEqualFqName(child1Symbol)) {
          val initImplFun = addInitImplFun(moduleFragment, pluginContext, declaration)
          initImplFun.dump().log2("addInitImplFun ->\n")
          overrideInitFun(moduleFragment, pluginContext, declaration, initImplFun)
          declaration.functions
            .filter { it.name.asString() == "init" || it.name.asString() == "initImpl" }
            .joinToString("\n") {
              it.dump()
            }.log2()
        }
        if (declaration.hasEqualFqName(child2Symbol)) {
          declaration.functions
            .filter { it.name.asString() == "init" || it.name.asString() == "initImpl" }
            .joinToString("\n") {
              it.dump()
            }.log2()
        }
        return super.visitClass(declaration)
      }
    })
  }
  
  private fun addInitImplFun(
    moduleFragment: IrModuleFragment,
    pluginContext: IrPluginContext,
    declaration: IrClass,
  ): IrSimpleFunction {
    val child3Id = ClassId(FqName.ROOT, FqName("Child3"), false)
    val child3Symbol = pluginContext.referenceClass(child3Id)!!
    val constructor = child3Symbol.constructors.single()
    val parentId = ClassId(FqName.ROOT, FqName("Parent"), false)
    val parentSymbol = pluginContext.referenceClass(parentId)!!
    val parentSetFun = parentSymbol.owner
      .functions
      .filter { it.name.toString() == "set" }
      .single()
    return declaration.addFunction {
      name = Name.identifier("initImpl")
      returnType = pluginContext.irBuiltIns.unitType
      modality = Modality.FINAL
      visibility = DescriptorVisibilities.PRIVATE
    }.also { initImpl ->
      initImpl.dispatchReceiverParameter = declaration.thisReceiver!!.copyTo(
        initImpl,
        IrDeclarationOrigin.DEFINED
      )
      val setFun = declaration.functions
        .filter {
          it.overrides(parentSetFun)
        }.single()
      initImpl.body = DeclarationIrBuilder(pluginContext, initImpl.symbol).irBlockBody {
        +irCall(setFun).also { callSet ->
          callSet.dispatchReceiver = irGet(initImpl.dispatchReceiverParameter!!)
          val name = "123".toIrConst(pluginContext.irBuiltIns.stringType)
          callSet.putValueArgument(0, name)
          callSet.putValueArgument(
            1, IrFunctionExpressionImpl(
              UNDEFINED_OFFSET, UNDEFINED_OFFSET,
              pluginContext.irBuiltIns.functionN(0).typeWith(pluginContext.irBuiltIns.anyType),
              IrFactoryImpl.createFunction(
                name = Name.special("<anonymous>"),
                startOffset = UNDEFINED_OFFSET,
                endOffset = UNDEFINED_OFFSET,
                origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA,
                symbol = IrSimpleFunctionSymbolImpl(null),
                visibility = DescriptorVisibilities.LOCAL,
                modality = Modality.FINAL,
                returnType = pluginContext.irBuiltIns.anyType,
                isInline = false,
                isExternal = false,
                isTailrec = false,
                isSuspend = false,
                isOperator = false,
                isInfix = false,
                isExpect = false,
                isFakeOverride = false,
                containerSource = null
              ).also { lambda ->
                lambda.setDeclarationsParent(initImpl)
                lambda.body = DeclarationIrBuilder(pluginContext, lambda.symbol).irBlockBody {
                  +irReturn(
                    irCall(constructor)
                  )
                }
              },
              IrStatementOrigin.LAMBDA
            )
          )
        }
      }
    }
  }
  
  private fun overrideInitFun(
    moduleFragment: IrModuleFragment,
    pluginContext: IrPluginContext,
    declaration: IrClass,
    initImplFun: IrSimpleFunction
  ) {
    val initFun = declaration.functions.single { it.name.asString() == "init" }
    declaration.declarations.remove(initFun)
    declaration.addFunction {
      name = initFun.name
      returnType = pluginContext.irBuiltIns.unitType
      modality = Modality.OPEN
      visibility = DescriptorVisibilities.PUBLIC
    }.also { newInitFun ->
      newInitFun.overriddenSymbols = initFun.overriddenSymbols
      newInitFun.dispatchReceiverParameter = declaration.thisReceiver!!.copyTo(
        newInitFun,
        IrDeclarationOrigin.DEFINED
      )
      newInitFun.body = DeclarationIrBuilder(pluginContext, newInitFun.symbol).irBlockBody {
        +irCall(initImplFun).also {
          it.dispatchReceiver = irGet(newInitFun.dispatchReceiverParameter!!)
        }
        initFun.body?.statements?.forEach {
          +it
        }
      }
    }
  }
  
  private val IrClass.superClass: IrClass?
    get() = superTypes
      .firstOrNull { !it.isInterface() && !it.isAny() }
      ?.classOrNull
      ?.owner
}