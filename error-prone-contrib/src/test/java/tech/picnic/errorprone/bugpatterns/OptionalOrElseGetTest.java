package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class OptionalOrElseGetTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(OptionalOrElseGet.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import com.google.errorprone.refaster.Refaster;
            import java.util.Optional;
            import java.util.function.Supplier;

            class A {
              private final Optional<Object> optional = Optional.empty();
              private final String string = optional.toString();

              void m() {
                Optional.empty().orElse(null);
                optional.orElse(null);
                optional.orElse("constant");
                optional.orElse("constant" + 0);
                optional.orElse(Boolean.TRUE);
                optional.orElse(string);
                optional.orElse(this.string);
                optional.orElse(Refaster.anyOf("constant", "another"));
                Optional.<Supplier<String>>empty().orElse(() -> "constant");

                // BUG: Diagnostic contains:
                Optional.empty().orElse(string + "constant");
                // BUG: Diagnostic contains:
                optional.orElse(string + "constant");
                // BUG: Diagnostic contains:
                optional.orElse("constant".toString());
                // BUG: Diagnostic contains:
                optional.orElse(string.toString());
                // BUG: Diagnostic contains:
                optional.orElse(this.string.toString());
                // BUG: Diagnostic contains:
                optional.orElse(String.valueOf(42));
                // BUG: Diagnostic contains:
                optional.orElse(string.toString().length());
                // BUG: Diagnostic contains:
                optional.orElse("constant".equals(string));
                // BUG: Diagnostic contains:
                optional.orElse(string.equals(string));
                // BUG: Diagnostic contains:
                optional.orElse(this.string.equals(string));
                // BUG: Diagnostic contains:
                optional.orElse(foo());
                // BUG: Diagnostic contains:
                optional.orElse(this.foo());
                // BUG: Diagnostic contains:
                optional.orElse(new Object() {});
                // BUG: Diagnostic contains:
                optional.orElse(new int[0].length);
              }

              private <T> T foo() {
                return null;
              }
            }
            """)
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(OptionalOrElseGet.class, getClass())
        .addInputLines(
            "A.java",
            """
            import java.util.Optional;

            class A {
              private final Optional<Object> optional = Optional.empty();
              private final String string = optional.toString();

              void m() {
                optional.orElse(string + "constant");
                optional.orElse("constant".toString());
                optional.orElse(string.toString());
                optional.orElse(this.string.toString());
                optional.orElse(String.valueOf(42));
                optional.orElse(string.toString().length());
                optional.orElse(string.equals(string));
                optional.orElse(foo());
                optional.orElse(this.<Number>foo());
                optional.orElse(this.<String, Integer>bar());
                optional.orElse(new Object() {});
                optional.orElse(new int[0].length);
              }

              private <T> T foo() {
                return null;
              }

              private <S, T> T bar() {
                return null;
              }
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import java.util.Optional;

            class A {
              private final Optional<Object> optional = Optional.empty();
              private final String string = optional.toString();

              void m() {
                optional.orElseGet(() -> string + "constant");
                optional.orElseGet("constant"::toString);
                optional.orElseGet(string::toString);
                optional.orElseGet(this.string::toString);
                optional.orElseGet(() -> String.valueOf(42));
                optional.orElseGet(() -> string.toString().length());
                optional.orElseGet(() -> string.equals(string));
                optional.orElseGet(() -> foo());
                optional.orElseGet(this::<Number>foo);
                optional.orElseGet(this::<String, Integer>bar);
                optional.orElseGet(() -> new Object() {});
                optional.orElseGet(() -> new int[0].length);
              }

              private <T> T foo() {
                return null;
              }

              private <S, T> T bar() {
                return null;
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
