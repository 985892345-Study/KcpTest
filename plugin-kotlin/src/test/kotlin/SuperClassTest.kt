import com.ndhzs.plugin.kotlin.KcpTestCommandLineProcessor
import com.ndhzs.plugin.kotlin.ir.superclass.SuperClassRegistrar
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

/**
 * .
 *
 * @author 985892345
 * 2023/6/14 14:53
 */
class SuperClassTest {
  
  @OptIn(ExperimentalCompilerApi::class)
  @Test
  fun `rewrite fun`() {
    val source1 = SourceFile.kotlin(
      "Parent.kt", """
        open class Parent {
          open fun init() {
          }

          open fun set(name: String, init: () -> Any) {
            val any = init.invoke()
            println(name + "   init: " + any)
          }
        }
      """.trimIndent()
    )
    val source2 = SourceFile.kotlin(
      "Child.kt", """
        open class Child1 : Parent() {
          override fun init() {
            println("abc")
          }
        }

        open class Child2 : Parent() {
          override fun init() {
            initImpl()
            println("abc")
          }

          private fun initImpl() {
            set("123") {
              Child4()
            }
          }
        }

        class Child3 : Child1() {
          override fun init() {
            super.init()
          }
        }

        class Child4 : Child2() {
          override fun init() {
            super.init()
          }
        }

      """.trimIndent()
    )
    val result = KotlinCompilation().apply {
      sources = listOf(source1, source2)
    
      // pass your own instance of a compiler plugin
      commandLineProcessors = listOf(KcpTestCommandLineProcessor())
      compilerPluginRegistrars = listOf(SuperClassRegistrar())
    
      inheritClassPath = true
      messageOutputStream = System.out // see diagnostics in real time
    }.compile()
  
    assert(result.exitCode == KotlinCompilation.ExitCode.OK) {
      result.messages
    }
  
    println("\n=============================================\n")
    
    val child1Clazz = result.classLoader.loadClass("Child1")
    val method = child1Clazz.getDeclaredMethod("init")
    method.invoke(child1Clazz.declaredConstructors[0].newInstance())
  
    /*
    * 子类不重写父类方法是将存在一个 FAKE_OVERRIDE 标志的方法，
    * 如果子类的子类重写时，则 super 指向的 子类中 FAKE_OVERRIDE 标志的方法
    * */
    
    println("\n=============================================\n")
  }
}