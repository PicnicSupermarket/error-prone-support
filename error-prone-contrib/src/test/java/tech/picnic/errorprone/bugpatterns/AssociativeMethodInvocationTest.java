package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class AssociativeMethodInvocationTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(AssociativeMethodInvocation.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import com.google.common.collect.ImmutableList;
            import com.google.errorprone.matchers.Matchers;
            import com.google.errorprone.refaster.Refaster;

            class A {
              void m() {
                Matchers.allOf();
                Matchers.anyOf();
                Refaster.anyOf();

                Matchers.allOf((t, s) -> true);
                Matchers.anyOf((t, s) -> true);
                Refaster.anyOf(0);

                Matchers.allOf(Matchers.anyOf((t, s) -> true));
                Matchers.anyOf(Matchers.allOf((t, s) -> true));
                Refaster.anyOf(Matchers.allOf((t, s) -> true));

                // BUG: Diagnostic contains:
                Matchers.allOf(Matchers.allOf((t, s) -> true));
                // BUG: Diagnostic contains:
                Matchers.anyOf(Matchers.anyOf((t, s) -> true));
                // BUG: Diagnostic contains:
                Refaster.anyOf(Refaster.anyOf(0));

                Matchers.allOf(Matchers.allOf(ImmutableList.of((t, s) -> true)));
                Matchers.anyOf(Matchers.anyOf(ImmutableList.of((t, s) -> true)));

                // BUG: Diagnostic contains:
                Matchers.allOf(
                    (t, s) -> true, Matchers.allOf((t, s) -> false, (t, s) -> true), (t, s) -> false);
                // BUG: Diagnostic contains:
                Matchers.anyOf(
                    (t, s) -> true, Matchers.anyOf((t, s) -> false, (t, s) -> true), (t, s) -> false);
                // BUG: Diagnostic contains:
                Refaster.anyOf(0, Refaster.anyOf(1, 2), 3);
              }
            }
            """)
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(AssociativeMethodInvocation.class, getClass())
        .addInputLines(
            "A.java",
            """
            import com.google.errorprone.matchers.Matchers;
            import com.google.errorprone.refaster.Refaster;

            class A {
              void m() {
                Matchers.allOf(Matchers.allOf());
                Matchers.anyOf(Matchers.anyOf());
                Refaster.anyOf(Refaster.anyOf());

                Matchers.allOf(Matchers.allOf((t, s) -> true));
                Matchers.anyOf(Matchers.anyOf((t, s) -> true));
                Refaster.anyOf(Refaster.anyOf(0));

                Matchers.allOf(
                    Matchers.anyOf(),
                    Matchers.allOf((t, s) -> false, (t, s) -> true),
                    Matchers.allOf(),
                    Matchers.anyOf((t, s) -> false));
                Matchers.anyOf(
                    Matchers.allOf(),
                    Matchers.anyOf((t, s) -> false, (t, s) -> true),
                    Matchers.anyOf(),
                    Matchers.allOf((t, s) -> false));
                Refaster.anyOf(Matchers.allOf(), Refaster.anyOf(1, 2), Matchers.anyOf());
              }
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import com.google.errorprone.matchers.Matchers;
            import com.google.errorprone.refaster.Refaster;

            class A {
              void m() {
                Matchers.allOf();
                Matchers.anyOf();
                Refaster.anyOf();

                Matchers.allOf((t, s) -> true);
                Matchers.anyOf((t, s) -> true);
                Refaster.anyOf(0);

                Matchers.allOf(
                    Matchers.anyOf(), (t, s) -> false, (t, s) -> true, Matchers.anyOf((t, s) -> false));
                Matchers.anyOf(
                    Matchers.allOf(), (t, s) -> false, (t, s) -> true, Matchers.allOf((t, s) -> false));
                Refaster.anyOf(Matchers.allOf(), 1, 2, Matchers.anyOf());
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
