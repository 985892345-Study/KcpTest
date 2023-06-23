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

        import TestAnnotation

        @TestAnnotation(clazz = A::class, name = NAME)
        class A(name: String = "123")

        const val NAME = "123"

      """.trimIndent()
    )
    val source2 = SourceFile.kotlin(
      "Test2.kt", """
        package com.a.b
      
        class B
      """.trimIndent()
    )
    val source3 = SourceFile.kotlin(
      "Annotation.kt", """
        import kotlin.reflect.KClass

        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.RUNTIME)
        annotation class TestAnnotation(val clazz: KClass<*> = Any::class, val name: String = "")
      """.trimIndent()
    )
    val result = KotlinCompilation().apply {
      sources = listOf(source1, source2, source3)
    
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