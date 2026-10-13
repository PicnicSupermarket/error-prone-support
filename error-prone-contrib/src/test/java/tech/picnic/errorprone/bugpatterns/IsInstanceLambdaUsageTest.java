package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class IsInstanceLambdaUsageTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(IsInstanceLambdaUsage.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.List;",
            "import java.util.stream.Stream;",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  record R(int i) {}",
            "",
            "  <T> void m(T value) {",
            "    Integer localVariable = 0;",
            "",
            "    Stream.of(0).map(i -> i);",
            "    Stream.of(1).map(i -> i + 1);",
            "    Stream.of(2).filter(Integer.class::isInstance);",
            "    Stream.<Object>of(3).filter(o -> o instanceof R(var i));",
            "    Stream.<Object>of(4).filter(o -> o instanceof List<?>);",
            "    Stream.of(value).filter(v -> v instanceof T);",
            "    Stream.of(5).filter(i -> i.getClass() instanceof Class);",
            "    Stream.of(6).filter(i -> localVariable instanceof Integer);",
            "    // XXX: Ideally this case is also flagged. Pick this up in the context of merging the",
            "    // `IsInstanceLambdaUsage` and `MethodReferenceUsage` checks, or introduce a separate check that",
            "    // simplifies unnecessary block lambda expressions.",
            "    Stream.of(7)",
            "        .filter(",
            "            i -> {",
            "              return i instanceof Integer;",
            "            });",
            "    Flux.just(8, \"foo\").distinctUntilChanged(v -> v, (a, b) -> a instanceof Integer);",
            "",
            "    // BUG: Diagnostic contains:",
            "    Stream.of(9).filter(i -> i instanceof Integer);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(IsInstanceLambdaUsage.class, getClass())
        .addInputLines(
            "A.java",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  void m() {",
            "    Stream.of(1).filter(i -> i instanceof Integer);",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  void m() {",
            "    Stream.of(1).filter(Integer.class::isInstance);",
            "  }",
            "}")
        .doTest();
  }
}
