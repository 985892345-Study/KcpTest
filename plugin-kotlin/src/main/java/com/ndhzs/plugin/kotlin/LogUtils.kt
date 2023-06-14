package com.ndhzs.plugin.kotlin

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

/**
 * .
 *
 * @author 985892345
 * 2023/5/29 11:01
 */
interface LogUtils {
  
  val messageCollector: MessageCollector
  
  fun log(msg: Any?) {
    messageCollector.report(
      CompilerMessageSeverity.WARNING,
      "${this.javaClass.simpleName} -> \n${msg.toString()}"
    )
  }
  
  fun <T> T.log2(msg: String = ""): T {
    if (msg.isEmpty()) log(this) else log("$msg$this")
    return this
  }
}