package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Predicates.containsPattern;
import static com.google.common.base.Predicates.not;
import static com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers.SECOND;
import static com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers.THIRD;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.CorePublisher;
import reactor.core.publisher.Flux;

final class ImplicitBlockingFluxTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(ImplicitBlockingFlux.class, getClass())
        .addSourceLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "import java.util.stream.Stream;",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).toIterable();",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(2).toStream();",
            "    // BUG: Diagnostic contains:",
            "    long count = Flux.just(3).toStream().count();",
            "",
            "    Flux.just(3).toStream(16);",
            "    new Foo().toIterable();",
            "    new Foo().toStream();",
            "  }",
            "",
            "  public final class Foo<T> {",
            "    public Iterable<T> toIterable() {",
            "      return ImmutableList.of();",
            "    }",
            "",
            "    public Stream<T> toStream() {",
            "      return Stream.empty();",
            "    }",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void identificationWithoutGuavaOnClasspath() {
    CompilationTestHelper.newInstance(ImplicitBlockingFlux.class, getClass())
        .withClasspath(CorePublisher.class, Flux.class, Publisher.class)
        .expectErrorMessage("X", not(containsPattern("toImmutableList")))
        .addSourceLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic matches: X",
            "    Flux.just(1).toIterable();",
            "    // BUG: Diagnostic matches: X",
            "    Flux.just(2).toStream();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacementFirstSuggestedFix() {
    BugCheckerRefactoringTestHelper.newInstance(ImplicitBlockingFlux.class, getClass())
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
            "  @SuppressWarnings(\"ImplicitBlockingFlux\")",
            "  void m() {",
            "    Flux.just(1).toIterable();",
            "    Flux.just(2).toStream();",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementSecondSuggestedFix() {
    BugCheckerRefactoringTestHelper.newInstance(ImplicitBlockingFlux.class, getClass())
        .setFixChooser(SECOND)
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(1).toIterable();",
            "    Flux.just(2).toStream().count();",
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
            "    Flux.just(2).collect(toImmutableList()).block().stream().count();",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementThirdSuggestedFix() {
    BugCheckerRefactoringTestHelper.newInstance(ImplicitBlockingFlux.class, getClass())
        .setFixChooser(THIRD)
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(1).toIterable();",
            "    Flux.just(2).toStream();",
            "    Flux.just(3).toStream().findAny();",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static java.util.stream.Collectors.toList;",
            "",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(1).collect(toList()).block();",
            "    Flux.just(2).collect(toList()).block().stream();",
            "    Flux.just(3).collect(toList()).block().stream().findAny();",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
