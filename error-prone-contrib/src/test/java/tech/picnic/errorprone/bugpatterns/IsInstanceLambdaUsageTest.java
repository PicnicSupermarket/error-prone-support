package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class IsInstanceLambdaUsageTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(IsInstanceLambdaUsage.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(IsInstanceLambdaUsage.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import com.google.common.collect.ImmutableSet;",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic contains:",
            "    Stream.of(1).filter(i -> i instanceof Integer);",
            "    Stream.of(2).filter(Integer.class::isInstance);",
            "    Stream.of(1).filter(i -> i.getClass() instanceof Class);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
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
        .doTest(TestMode.TEXT_MATCH);
  }
}
