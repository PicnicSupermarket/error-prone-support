package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class EmptyMethodTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(EmptyMethod.class, getClass())
        .addSourceLines(
            "A.java",
            """
            class A {
              Object m1() {
                return null;
              }

              void m2() {
                System.out.println(42);
              }

              void m3() {}

              // BUG: Diagnostic contains:
              static void m4() {}

              interface F {
                void fun();
              }

              final class MyTestClass {
                void helperMethod() {}
              }
            }
            """)
        .addSourceLines(
            "B.java",
            """
            import org.aspectj.lang.annotation.Pointcut;

            final class B implements A.F {
              @Override
              public void fun() {}

              // BUG: Diagnostic contains:
              void m3() {}

              /** Javadoc. */
              // BUG: Diagnostic contains:
              void m4() {}

              void m5() {
                // Single-line comment.
              }

              void m6() {
                /* Multi-line comment. */
              }

              @Pointcut
              void m7() {}
            }
            """)
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(EmptyMethod.class, getClass())
        .addInputLines(
            "A.java",
            """
            final class A {
              void instanceMethod() {}

              static void staticMethod() {}

              static void staticMethodWithComment() {
                /* Foo. */
              }
            }
            """)
        .addOutputLines(
            "A.java",
            """
            final class A {
              static void staticMethodWithComment() {
                /* Foo. */
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
