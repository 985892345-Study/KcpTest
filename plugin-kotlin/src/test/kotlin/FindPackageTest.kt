import com.ndhzs.plugin.kotlin.KcpTestCommandLineProcessor
import com.ndhzs.plugin.kotlin.ir.findpackage.FindPackageRegistrar
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

/**
 * .
 *
 * @author 985892345
 * 2023/6/13 18:56
 */
class FindPackageTest {
  
  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `find package`() {
    // https://github.com/tschuchortdev/kotlin-compile-testing
    val source1 = SourceFile.kotlin(
      "Test1.kt", """
        package com.a
      
        class A
      """.trimIndent()
    )
    val source2 = SourceFile.kotlin(
      "Test2.kt", """
        package com.b
      
        class B
      """.trimIndent()
    )
    val result = KotlinCompilation().apply {
      sources = listOf(source1, source2)
    
      // pass your own instance of a compiler plugin
      commandLineProcessors = listOf(KcpTestCommandLineProcessor())
      compilerPluginRegistrars = listOf(FindPackageRegistrar())
    
      inheritClassPath = true
      messageOutputStream = System.out // see diagnostics in real time
    }.compile()
  
    assert(result.exitCode == KotlinCompilation.ExitCode.OK) {
      result.messages
    }
  
    println("\n=============================================\n")
  
  
  
    println("\n=============================================\n")
  }
}