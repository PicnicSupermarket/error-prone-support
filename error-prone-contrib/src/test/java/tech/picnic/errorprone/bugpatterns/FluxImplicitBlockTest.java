package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Predicates.and;
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

final class FluxImplicitBlockTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(FluxImplicitBlock.class, getClass())
        .expectErrorMessage(
            "X",
            and(
                containsPattern("SuppressWarnings"),
                containsPattern("toImmutableList"),
                containsPattern("toList")))
        .addSourceLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "import java.util.stream.Stream;",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic matches: X",
            "    Flux.just(1).toIterable();",
            "    // BUG: Diagnostic matches: X",
            "    Flux.just(2).toStream();",
            "    // BUG: Diagnostic matches: X",
            "    long count = Flux.just(3).toStream().count();",
            "",
            "    Flux.just(4).toIterable(1);",
            "    Flux.just(5).toIterable(2, null);",
            "    Flux.just(6).toStream(3);",
            "    new Foo().toIterable();",
            "    new Foo().toStream();",
            "  }",
            "",
            "  class Foo<T> {",
            "    Iterable<T> toIterable() {",
            "      return ImmutableList.of();",
            "    }",
            "",
            "    Stream<T> toStream() {",
            "      return Stream.empty();",
            "    }",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void identificationWithoutGuavaOnClasspath() {
    CompilationTestHelper.newInstance(FluxImplicitBlock.class, getClass())
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
    BugCheckerRefactoringTestHelper.newInstance(FluxImplicitBlock.class, getClass())
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
            "  @SuppressWarnings(\"FluxImplicitBlock\")",
            "  void m() {",
            "    Flux.just(1).toIterable();",
            "    Flux.just(2).toStream();",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementSecondSuggestedFix() {
    BugCheckerRefactoringTestHelper.newInstance(FluxImplicitBlock.class, getClass())
        .setFixChooser(SECOND)
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(1).toIterable();",
            "    Flux.just(2).toStream();",
            "    Flux.just(3).toIterable().iterator();",
            "    Flux.just(4).toStream().count();",
            "    Flux.just(5) /* a */./* b */ toIterable /* c */(/* d */ ) /* e */;",
            "    Flux.just(6) /* a */./* b */ toStream /* c */(/* d */ ) /* e */;",
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
            "    Flux.just(3).collect(toImmutableList()).block().iterator();",
            "    Flux.just(4).collect(toImmutableList()).block().stream().count();",
            "    Flux.just(5).collect(toImmutableList()).block() /* e */;",
            "    Flux.just(6).collect(toImmutableList()).block().stream() /* e */;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementThirdSuggestedFix() {
    BugCheckerRefactoringTestHelper.newInstance(FluxImplicitBlock.class, getClass())
        .setFixChooser(THIRD)
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(1).toIterable();",
            "    Flux.just(2).toStream();",
            "    Flux.just(3).toIterable().iterator();",
            "    Flux.just(4).toStream().count();",
            "    Flux.just(5) /* a */./* b */ toIterable /* c */(/* d */ ) /* e */;",
            "    Flux.just(6) /* a */./* b */ toStream /* c */(/* d */ ) /* e */;",
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
            "    Flux.just(3).collect(toList()).block().iterator();",
            "    Flux.just(4).collect(toList()).block().stream().count();",
            "    Flux.just(5).collect(toList()).block() /* e */;",
            "    Flux.just(6).collect(toList()).block().stream() /* e */;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
