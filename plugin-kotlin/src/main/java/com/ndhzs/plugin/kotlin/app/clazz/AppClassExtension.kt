package com.ndhzs.plugin.kotlin.app.clazz

import com.ndhzs.plugin.kotlin.LogUtils
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin

/**
 * .
 *
 * @author 985892345
 * 2023/6/1 21:33
 */
class AppClassExtension(
  override val messageCollector: MessageCollector
) : ClassBuilderInterceptorExtension, LogUtils {
  override fun interceptClassBuilderFactory(
    interceptedFactory: ClassBuilderFactory,
    bindingContext: BindingContext,
    diagnostics: DiagnosticSink
  ): ClassBuilderFactory {
    return object : ClassBuilderFactory by interceptedFactory {
      override fun newClassBuilder(origin: JvmDeclarationOrigin): ClassBuilder {
        return AppClassBuilder(interceptedFactory.newClassBuilder(origin))
      }
    }
  }
  
  private inner class AppClassBuilder(
    val classBuilder: ClassBuilder
  ) : DelegatingClassBuilder() {
    override fun getDelegate(): ClassBuilder = classBuilder
    override fun defineClass(
      origin: PsiElement?,
      version: Int,
      access: Int,
      name: String,
      signature: String?,
      superName: String,
      interfaces: Array<out String>
    ) {
      super.defineClass(origin, version, access, name, signature, superName, interfaces)
      log("defineClass = $name")
    }
  }
}