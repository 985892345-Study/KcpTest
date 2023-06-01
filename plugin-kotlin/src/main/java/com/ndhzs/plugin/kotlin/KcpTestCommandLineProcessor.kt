package com.ndhzs.plugin.kotlin

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

/**
 * .
 *
 * @author 985892345
 * 2023/5/26 17:00
 */
@AutoService(CommandLineProcessor::class)
@OptIn(ExperimentalCompilerApi::class)
class KcpTestCommandLineProcessor : CommandLineProcessor {
  override val pluginId: String = "kcpTest"
  override val pluginOptions: Collection<AbstractCliOption> = emptyList()
}