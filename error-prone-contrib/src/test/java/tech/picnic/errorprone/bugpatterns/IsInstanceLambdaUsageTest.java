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
            "import java.util.ArrayList;",
            "import java.util.List;",
            "import java.util.stream.Stream;",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  <T> void m() {",
            "    Integer localVariable = 0;",
            "",
            "    Stream.of(0).map(i -> i);",
            "    Stream.of(1).map(i -> i + 1);",
            "    Stream.of(2).filter(Integer.class::isInstance);",
            "    Stream.of(3).filter(i -> i.getClass() instanceof Class);",
            "    Stream.of(4).filter(i -> localVariable instanceof Integer);",
            "    // XXX: Ideally this case is also flagged. Pick this up in the context of merging the",
            "    // `IsInstanceLambdaUsage` and `MethodReferenceUsage` checks, or introduce a separate check that",
            "    // simplifies unnecessary block lambda expressions.",
            "    Stream.of(5)",
            "        .filter(",
            "            i -> {",
            "              return i instanceof Integer;",
            "            });",
            "    Flux.just(6, \"foo\").distinctUntilChanged(v -> v, (a, b) -> a instanceof Integer);",
            "    Stream.<Object>of(7).filter(o -> o instanceof R(var i));",
            "",
            "    // BUG: Diagnostic contains:",
            "    Stream.of(8).filter(i -> i instanceof Integer);",
            "    // BUG: Diagnostic contains:",
            "    Stream.<Object>of(9).filter(o -> o instanceof List);",
            "    // BUG: Diagnostic contains:",
            "    Stream.<Object>of(10).filter(o -> o instanceof List<?>);",
            "    // BUG: Diagnostic contains:",
            "    Stream.of(List.of(11)).filter(l -> l instanceof ArrayList<Integer>);",
            "    // BUG: Diagnostic contains:",
            "    Stream.<T>of().filter(v -> v instanceof T);",
            "    // BUG: Diagnostic contains:",
            "    Stream.<T[]>of().filter(v -> v instanceof T[]);",
            "  }",
            "",
            "  record R(int i) {}",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(IsInstanceLambdaUsage.class, getClass())
        .addInputLines(
            "A.java",
            "import java.util.ArrayList;",
            "import java.util.List;",
            "import java.util.stream.Stream;",
            "import org.jspecify.annotations.Nullable;",
            "",
            "class A {",
            "  <T> void m() {",
            "    Stream.of(1).filter(i -> i instanceof Integer);",
            "    Stream.of(List.of(2)).filter(l -> l instanceof ArrayList<Integer>);",
            "    Stream.of(3).filter(o -> o instanceof @Nullable Number);",
            "    Stream.<T>of().filter(v -> v instanceof T);",
            "    Stream.<T[]>of().filter(v -> v instanceof T[]);",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import java.util.ArrayList;",
            "import java.util.List;",
            "import java.util.stream.Stream;",
            "import org.jspecify.annotations.Nullable;",
            "",
            "class A {",
            "  <T> void m() {",
            "    Stream.of(1).filter(Integer.class::isInstance);",
            "    Stream.of(List.of(2)).filter(ArrayList.class::isInstance);",
            "    Stream.of(3).filter(Number.class::isInstance);",
            "    Stream.<T>of().filter(Object.class::isInstance);",
            "    Stream.<T[]>of().filter(Object[].class::isInstance);",
            "  }",
            "}")
        .doTest();
  }
}
