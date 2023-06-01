package com.ndhzs.plugin.kotlin.app

import com.google.auto.service.AutoService
import com.ndhzs.plugin.kotlin.app.clazz.AppClassExtension
import com.ndhzs.plugin.kotlin.app.ir.AppIrExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

/**
 * .
 *
 * @author 985892345
 * 2023/5/29 21:49
 */
@AutoService(CompilerPluginRegistrar::class)
@OptIn(ExperimentalCompilerApi::class)
class AppRegistrar : CompilerPluginRegistrar() {
  
  override val supportsK2: Boolean
    get() = false
  
  override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
    val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
    IrGenerationExtension.registerExtension(AppIrExtension(messageCollector))
    ClassBuilderInterceptorExtension.registerExtension(AppClassExtension(messageCollector))
  }
}