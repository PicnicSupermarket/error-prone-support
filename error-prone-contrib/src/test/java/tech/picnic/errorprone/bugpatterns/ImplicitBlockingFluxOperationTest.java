package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.CorePublisher;
import reactor.core.publisher.Flux;

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
            "    Flux.just(1).toIterable();",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(2).toStream();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void identificationWithoutGuavaOnClasspath() {
    CompilationTestHelper.newInstance(ImplicitBlockingFluxOperation.class, getClass())
        .withClasspath(Publisher.class, CorePublisher.class, Flux.class)
        .addSourceLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).toIterable();",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(2).toStream();",
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
            "    Flux.just(1).toIterable();",
            "    Flux.just(2).toStream();",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  @SuppressWarnings(\"ImplicitBlockingFluxOperation\")",
            "  void m() {",
            "    Flux.just(1).toIterable();",
            "    Flux.just(2).toStream();",
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
            "    Flux.just(1).toIterable();",
            "    Flux.just(2).toStream();",
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
            "    Flux.just(1).collect(toImmutableList()).block();",
            "    Flux.just(2).collect(toImmutableList()).block().stream();",
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
            "    Flux.just(1).toIterable();",
            "    Flux.just(2).toStream();",
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
            "    Flux.just(1).collect(toUnmodifiableList()).block();",
            "    Flux.just(2).collect(toUnmodifiableList()).block().stream();",
            "  }",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }
}
