package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class ClassCastLambdaUsageTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(ClassCastLambdaUsage.class, getClass())
        .addSourceLines(
            "A.java",
            "import com.google.common.collect.ImmutableSet;",
            "import java.util.stream.IntStream;",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  <T> void m() {",
            "    Number localVariable = 0;",
            "",
            "    Stream.of(0).map(i -> i);",
            "    Stream.of(1).map(i -> i + 1);",
            "    Stream.of(2).map(Integer.class::cast);",
            "    Stream.of(3).map(i -> (Integer) 2);",
            "    Stream.of(4).map(i -> (Integer) localVariable);",
            "    // XXX: Ideally this case is also flagged. Pick this up in the context of merging the",
            "    // `ClassCastLambdaUsage` and `MethodReferenceUsage` checks, or introduce a separate check that",
            "    // simplifies unnecessary block lambda expressions.",
            "    Stream.of(5)",
            "        .map(",
            "            i -> {",
            "              return (Integer) i;",
            "            });",
            "    Stream.<ImmutableSet>of(ImmutableSet.of(6)).map(s -> (ImmutableSet<Number>) s);",
            "    Stream.of(ImmutableSet.of(7)).map(s -> (ImmutableSet<?>) s);",
            "    Stream.of(8).reduce((a, b) -> (Integer) a);",
            "    IntStream.of(9).mapToObj(i -> (char) i);",
            "    Stream.of(10).map(i -> (T) i);",
            "",
            "    // BUG: Diagnostic contains:",
            "    Stream.of(11).map(i -> (Integer) i);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(ClassCastLambdaUsage.class, getClass())
        .addInputLines(
            "A.java",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  void m() {",
            "    Stream.of(1).map(i -> (Integer) i);",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  void m() {",
            "    Stream.of(1).map(Integer.class::cast);",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
