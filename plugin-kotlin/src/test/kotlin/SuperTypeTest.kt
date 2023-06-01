import com.ndhzs.plugin.kotlin.KcpTestCommandLineProcessor
import com.ndhzs.plugin.kotlin.ir.supertype.SuperTypeRegistrar
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

/**
 * .
 *
 * @author 985892345
 * 2023/5/29 11:07
 */
class SuperTypeTest {
  
  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `add superType`() {
    val source1 = SourceFile.kotlin(
      "Test.kt", """
        interface ITest

        class TestImpl : ITest

        interface ITest1 {
          fun get(str: String): String
        }

        class TestImpl2 : ITest1, ITest2 {
          override fun get(str: String): String {
            return "123"
          }

          override fun abc(iTest1: ITest1): String {
            return get(iTest1.toString())
          }
        }
      """.trimIndent()
    )
    val source2 = SourceFile.java(
      "ITest2.java", """
        import org.jetbrains.annotations.NotNull;
        public interface ITest2 {
          public String abc(@NotNull ITest1 iTest1);
        }
      """.trimIndent()
    )
    val result = KotlinCompilation().apply {
      sources = listOf(source1, source2)
    
      // pass your own instance of a compiler plugin
      commandLineProcessors = listOf(KcpTestCommandLineProcessor())
      compilerPluginRegistrars = listOf(SuperTypeRegistrar())
    
      inheritClassPath = true
      messageOutputStream = System.out // see diagnostics in real time
    }.compile()
  
    assert(result.exitCode == KotlinCompilation.ExitCode.OK) {
      result.messages
    }
  
    println("\n=============================================\n")
  
    val iTestClass = result.classLoader.loadClass("ITest")
    val iTest1Class = result.classLoader.loadClass("ITest1")
    val iTest2Class = result.classLoader.loadClass("ITest2")
    val testImplClass = result.classLoader.loadClass("TestImpl")
    
    // ITest 实现 ITest1 和 ITest1.get 方法
    println("ITest.interfaces = ${iTestClass.interfaces.contentToString()}")
    assert(iTestClass.interfaces.contains(iTest1Class))
    val testImpl = testImplClass.declaredConstructors[0].newInstance()
    val invoke = iTest1Class.getMethod("get", String::class.java).invoke(testImpl, "123")
    assert(invoke == "123")
    
    
    // TestImpl 实现 ITest2 接口和 ITest2.get 方法
    println("TestImpl.interfaces = ${testImplClass.interfaces.contentToString()}")
    assert(testImplClass.interfaces.contains(iTest2Class))
    val invoke2 = iTest2Class.getMethod("abc", iTest1Class).invoke(testImpl, testImpl)
    println(invoke2)
    
    /*
    * 如果是给接口添加默认方法，则所有实现类都需要添加方法，不然会报未实现异常
    * */
  
    println("\n=============================================\n")
  }
}