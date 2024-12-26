package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.bugpatterns.BugChecker;
import org.junit.jupiter.api.Test;

final class ThrowsCheckedExceptionTest {
  @Test
  void matches() {
    CompilationTestHelper.newInstance(MatcherTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.concurrent.Callable;",
            "import java.util.function.Supplier;",
            "",
            "class A {",
            "  void negative1() {",
            "    callableSink(null);",
            "  }",
            "",
            "  void negative2() {",
            "    supplierSink(null);",
            "  }",
            "",
            "  void negative3() {",
            "    supplierSink((Supplier<?> & Iterable<?>) null);",
            "  }",
            "",
            "  void negative4() {",
            "    callableSink(() -> toString());",
            "  }",
            "",
            "  void negative5() {",
            "    supplierSink(() -> toString());",
            "  }",
            "",
            "  void negative6() {",
            "    callableSink(this::toString);",
            "  }",
            "",
            "  void negative7() {",
            "    supplierSink(this::toString);",
            "  }",
            "",
            "  void negative8() {",
            "    callableSink(A::throwsRuntimeException);",
            "  }",
            "",
            "  void negative9() {",
            "    supplierSink(A::throwsRuntimeException);",
            "  }",
            "",
            "  void negative10() {",
            "    callableSink(A::throwsError);",
            "  }",
            "",
            "  void negative11() {",
            "    supplierSink(A::throwsError);",
            "  }",
            "",
            "  void negative12() {",
            "    supplierSink(",
            "        new Supplier<>() {",
            "          @Override",
            "          public Object get() {",
            "            return getClass();",
            "          }",
            "        });",
            "  }",
            "",
            "  void positive1() {",
            "    // BUG: Diagnostic contains:",
            "    callableSink((Callable<?>) null);",
            "  }",
            "",
            "  void positive2() {",
            "    // BUG: Diagnostic contains:",
            "    callableSink((Callable<?> & Iterable<?>) null);",
            "  }",
            "",
            "  void positive3() {",
            "    // BUG: Diagnostic contains:",
            "    callableSink(() -> getClass().getDeclaredConstructor());",
            "  }",
            "",
            "  void positive4() {",
            "    // BUG: Diagnostic contains:",
            "    callableSink(getClass()::getDeclaredConstructor);",
            "  }",
            "",
            "  void positive5() {",
            "    callableSink(",
            "        // BUG: Diagnostic contains:",
            "        new Callable<>() {",
            "          @Override",
            "          public Object call() throws NoSuchMethodException {",
            "            return getClass().getDeclaredConstructor();",
            "          }",
            "        });",
            "  }",
            "",
            "  private static Object throwsRuntimeException() throws IllegalStateException {",
            "    return null;",
            "  }",
            "",
            "  private static Object throwsError() throws AssertionError {",
            "    return null;",
            "  }",
            "",
            "  private static void callableSink(Callable<?> callable) {}",
            "",
            "  private static void supplierSink(Supplier<?> supplier) {}",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} that simply delegates to {@link ThrowsCheckedException}. */
  @BugPattern(summary = "Flags expressions matched by `ThrowsCheckedException`", severity = ERROR)
  public static final class MatcherTestChecker extends AbstractMatcherTestChecker {
    private static final long serialVersionUID = 1L;

    // XXX: This is a false positive reported by Checkstyle. See
    // https://github.com/checkstyle/checkstyle/issues/10161#issuecomment-1242732120.
    @SuppressWarnings("RedundantModifier")
    public MatcherTestChecker() {
      super(new ThrowsCheckedException());
    }
  }
}
