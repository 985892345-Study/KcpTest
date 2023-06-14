package com.ndhzs.plugin.kotlin.ir.superclass

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

/**
 * .
 *
 * @author 985892345
 * 2023/6/14 14:50
 */
@AutoService(CompilerPluginRegistrar::class)
@OptIn(ExperimentalCompilerApi::class)
class SuperClassRegistrar : CompilerPluginRegistrar() {
  override val supportsK2: Boolean
    get() = true
  
  override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
    val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
    IrGenerationExtension.registerExtension(SuperClassExtension(messageCollector))
    set { any ->
    
    }
  }
  
  fun set(a: () -> Unit) {
  }
  
  fun set(b: (Any) -> Any) {
  
  }
}