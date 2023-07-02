package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class IllogicalReactorOperatorsTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(IllogicalReactorOperators.class, getClass())
        .addSourceLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "  void m() {",
            "    Mono.just(1).then(Mono.just(2));",
            "    Mono.just(1).filter(i -> i != 1).when(Mono.just(2));",
            "    // BUG: Diagnostic contains:",
            "    Mono.just(1).filter(i -> i != 1).then();",
            "    // BUG: Diagnostic contains:",
            "    Mono.just(1).filter(i -> i != 1).then(Mono.just(2));",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void suggestedFix() {
    BugCheckerRefactoringTestHelper.newInstance(IllogicalReactorOperators.class, getClass())
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "",
            "  void m() {",
            "    Mono.just(1).filter(i -> i != 1).then(Mono.just(2));",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "",
            "  void m() {",
            "    Mono.just(1).then(Mono.just(2));",
            "  }",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }
}
