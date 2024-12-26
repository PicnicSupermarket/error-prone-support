package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class AssertJIsNullTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(AssertJIsNull.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import static org.assertj.core.api.Assertions.assertThat;

            class A {
              void m() {
                assertThat(1).isEqualTo(1);
                // BUG: Diagnostic contains:
                assertThat(1).isEqualTo(null);
                // BUG: Diagnostic contains:
                assertThat("foo").isEqualTo(null);
                isEqualTo(null);
              }

              private boolean isEqualTo(Object value) {
                return value.equals("bar");
              }
            }
            """)
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(AssertJIsNull.class, getClass())
        .addInputLines(
            "A.java",
            """
            import static org.assertj.core.api.Assertions.assertThat;

            class A {
              void m() {
                assertThat(1).isEqualTo(null);
                assertThat("foo").isEqualTo(null);
              }
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import static org.assertj.core.api.Assertions.assertThat;

            class A {
              void m() {
                assertThat(1).isNull();
                assertThat("foo").isNull();
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
