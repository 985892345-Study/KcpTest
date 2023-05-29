import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.Test

/**
 * .
 *
 * @author 985892345
 * 2023/5/29 11:07
 */
class SuperTypeTest {
  
  @Test
  fun `add superType`() {
    val source1 = SourceFile.kotlin(
      "IAbc.kt", """
        package com.abc
      
        interface IAbc
      """.trimIndent()
    )
    val source2 = SourceFile.java(
      "ITest.java", """
        public interface ITest {
          public String get();
        }
      """.trimIndent()
    )
    
  }
}