import com.ndhzs.plugin.kotlin.KcpTestCommandLineProcessor
import com.ndhzs.plugin.kotlin.annotation.AnnotationRegistrar
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
class AnnotationTest {
  
  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `add annotation`() {
    // https://github.com/tschuchortdev/kotlin-compile-testing
    val kotlinSource = SourceFile.kotlin(
      "Test.kt", """
        class TestClass1

        @Annotation1(path = "123")
        class TestClass2
        
        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.RUNTIME)
        annotation class Annotation1(val path: String)
  
        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.RUNTIME)
        annotation class Annotation2(val name: String)
      """.trimIndent()
    )
    val javaSource = SourceFile.java(
      "JavaAnnotation.java", """
        import java.lang.annotation.ElementType;
        import java.lang.annotation.Retention;
        import java.lang.annotation.RetentionPolicy;
        import java.lang.annotation.Target;
        
        @Target(ElementType.TYPE)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface JavaAnnotation {
          String path();
          String group() default "";
        }
      """.trimIndent()
    )
    val result = KotlinCompilation().apply {
      sources = listOf(kotlinSource, javaSource)
      
      // pass your own instance of a compiler plugin
      commandLineProcessors = listOf(KcpTestCommandLineProcessor())
      compilerPluginRegistrars = listOf(AnnotationRegistrar())
      
      inheritClassPath = true
      messageOutputStream = System.out // see diagnostics in real time
    }.compile()
    
    assert(result.exitCode == KotlinCompilation.ExitCode.OK) {
      result.messages
    }
  
    println("\n=============================================\n")
  
    val testClass1 = result.classLoader.loadClass("TestClass1")
    val testClass2 = result.classLoader.loadClass("TestClass2")
    println("TestClass1.annotation = " + testClass1.annotations.contentToString())
    println("TestClass2.annotation = " + testClass2.annotations.contentToString())
    
    
    // TestClass1 编译器添加 @Annotation1
    val annotation1 = result.classLoader.loadClass("Annotation1")
    @Suppress("UNCHECKED_CAST")
    annotation1 as Class<out Annotation>
    assert(testClass1.isAnnotationPresent(annotation1))
    val annotation1PathMethod = annotation1.getDeclaredMethod("path")
    var annotation1Path = annotation1PathMethod.invoke(testClass1.getAnnotation(annotation1))
    println("annotation1Path = $annotation1Path")
    assert(annotation1Path == "自定义填充的参数") // 参数检查
  
    
    // TestClass1 编译器添加 @JavaAnnotation
    val javaAnnotation = result.classLoader.loadClass("JavaAnnotation")
    @Suppress("UNCHECKED_CAST")
    javaAnnotation as Class<out Annotation>
    assert(testClass1.isAnnotationPresent(javaAnnotation))
    val javaAnnotationPath = javaAnnotation.getDeclaredMethod("path")
      .invoke(testClass1.getAnnotation(javaAnnotation))
    println("javaAnnotationPath = $javaAnnotationPath")
    assert(javaAnnotationPath == "自定义填充的参数")
    
    
    // TestClass2 添加 @Annotation2，并复制 @Annotation1 上原先的参数
    val annotation2 = result.classLoader.loadClass("Annotation2")
    @Suppress("UNCHECKED_CAST")
    annotation2 as Class<out Annotation>
    assert(testClass2.isAnnotationPresent(annotation2))
    annotation1Path = annotation1PathMethod.invoke(testClass2.getAnnotation(annotation1))
    val annotation2NameMethod = annotation2.getDeclaredMethod("name")
    val annotation2Name = annotation2NameMethod.invoke(testClass2.getAnnotation(annotation2))
    println("annotation1Path = $annotation1Path   annotation2Name = $annotation2Name")
    assert(annotation1Path == annotation2Name)
  
  
    println("\n=============================================\n")
  }
}