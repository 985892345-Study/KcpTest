import com.ndhzs.plugin.kotlin.KcpTestCommandLineProcessor
import com.ndhzs.plugin.kotlin.KcpTestCompilerPluginRegistrar
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

/**
 * .
 *
 * @author 985892345
 * 2023/5/26 18:07
 */
class KcpTest {
  
  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `add annotation`() {
    // https://github.com/tschuchortdev/kotlin-compile-testing
    val kotlinSource = SourceFile.kotlin(
      "Test.kt", """
        @TestA(path = "123")
        class ATest

        @TestB(path = "456")
        class BTest
        
        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.RUNTIME)
        annotation class TestA(val path: String)
      """.trimIndent()
    )
    val javaSource = SourceFile.java(
      "TestB.java", """
        import java.lang.annotation.ElementType;
        import java.lang.annotation.Retention;
        import java.lang.annotation.RetentionPolicy;
        import java.lang.annotation.Target;
        
        @Target(ElementType.TYPE)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface TestB {
          String path();
        }
      """.trimIndent()
    )
    val result = KotlinCompilation().apply {
      sources = listOf(kotlinSource, javaSource)
      
      // pass your own instance of a compiler plugin
      commandLineProcessors = listOf(KcpTestCommandLineProcessor())
      compilerPluginRegistrars = listOf(KcpTestCompilerPluginRegistrar())
      
      inheritClassPath = true
      messageOutputStream = System.out // see diagnostics in real time
    }.compile()
    
    assert(result.exitCode == KotlinCompilation.ExitCode.OK) {
      result.messages
    }
    
    val testClazz = result.classLoader.loadClass("ATest")!!
    val testBClazz = result.classLoader.loadClass("TestB")!!
    @Suppress("UNCHECKED_CAST")
    testBClazz as Class<out Annotation>
    assert(testClazz.isAnnotationPresent(testBClazz))
    val testB = testClazz.getAnnotation(testBClazz)
    println(testB)
  }
}