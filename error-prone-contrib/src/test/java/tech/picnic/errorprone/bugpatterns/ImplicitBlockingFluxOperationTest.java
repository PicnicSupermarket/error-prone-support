package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class ImplicitBlockingFluxOperationTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(ImplicitBlockingFluxOperation.class, getClass())
        .addSourceLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic contains:",
            "    flux().toIterable();",
            "    // BUG: Diagnostic contains:",
            "    flux().toStream();",
            "  }",
            "",
            "  Flux<Integer> flux() {",
            "    return Flux.just(1, 2, 3);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacementFirstSuggestedFix() {
    BugCheckerRefactoringTestHelper.newInstance(ImplicitBlockingFluxOperation.class, getClass())
        .setFixChooser(FixChoosers.FIRST)
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    flux().toIterable();",
            "    flux().toStream();",
            "  }",
            "",
            "  Flux<Integer> flux() {",
            "    return Flux.just(1, 2, 3);",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  @SuppressWarnings(\"ImplicitBlockingFluxOperation\")",
            "  void m() {",
            "    flux().toIterable();",
            "    flux().toStream();",
            "  }",
            "",
            "  Flux<Integer> flux() {",
            "    return Flux.just(1, 2, 3);",
            "  }",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }

  @Test
  void replacementSecondSuggestedFix() {
    BugCheckerRefactoringTestHelper.newInstance(ImplicitBlockingFluxOperation.class, getClass())
        .setFixChooser(FixChoosers.SECOND)
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    flux().toIterable();",
            "    flux().toStream();",
            "  }",
            "",
            "  Flux<Integer> flux() {",
            "    return Flux.just(1, 2, 3);",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static com.google.common.collect.ImmutableList.toImmutableList;",
            "",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    flux().collect(toImmutableList()).block();",
            "    flux().collect(toImmutableList()).block().stream();",
            "  }",
            "",
            "  Flux<Integer> flux() {",
            "    return Flux.just(1, 2, 3);",
            "  }",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }

  @Test
  void replacementThirdSuggestedFix() {
    BugCheckerRefactoringTestHelper.newInstance(ImplicitBlockingFluxOperation.class, getClass())
        .setFixChooser(FixChoosers.THIRD)
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    flux().toIterable();",
            "    flux().toStream();",
            "  }",
            "",
            "  Flux<Integer> flux() {",
            "    return Flux.just(1, 2, 3);",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static java.util.stream.Collectors.toUnmodifiableList;",
            "",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    flux().collect(toUnmodifiableList()).block();",
            "    flux().collect(toUnmodifiableList()).block().stream();",
            "  }",
            "",
            "  Flux<Integer> flux() {",
            "    return Flux.just(1, 2, 3);",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
