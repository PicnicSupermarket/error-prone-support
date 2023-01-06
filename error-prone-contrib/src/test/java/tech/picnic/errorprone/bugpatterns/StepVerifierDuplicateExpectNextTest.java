package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugCheckerRefactoringTestHelper.newInstance;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import org.junit.jupiter.api.Test;

final class StepVerifierDuplicateExpectNextTest {
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      newInstance(StepVerifierDuplicateExpectNext.class, getClass());

  @Test
  void refactorDuplicateCalls() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "import reactor.test.StepVerifier;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(0, 1).as(StepVerifier::create).expectNext(0).expectNext(1);",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "import reactor.test.StepVerifier;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(0, 1).as(StepVerifier::create).expectNext(0, 1);",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void refactorDuplicatesWithSucceedingStatement() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "import reactor.test.StepVerifier;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(0, 1).as(StepVerifier::create).expectNext(0).expectNext(1).verifyComplete();",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "import reactor.test.StepVerifier;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(0, 1).as(StepVerifier::create).expectNext(0, 1).verifyComplete();",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void refactorThreeDuplicates() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "import reactor.test.StepVerifier;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(0, 1, 2)",
            "        .as(StepVerifier::create)",
            "        .expectNext(0)",
            "        .expectNext(1)",
            "        .expectNext(2)",
            "        .verifyComplete();",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "import reactor.test.StepVerifier;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(0, 1, 2).as(StepVerifier::create).expectNext(0, 1, 2).verifyComplete();",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void refactorManyDuplicates() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "import reactor.test.StepVerifier;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(0, 1, 2, 3, 4, 5, 6)",
            "        .as(StepVerifier::create)",
            "        .expectNext(0, 1)",
            "        .expectNext(2, 3)",
            "        .expectNext(4, 5, 6)",
            "        .verifyComplete();",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import reactor.core.publisher.Flux;",
            "import reactor.test.StepVerifier;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(0, 1, 2, 3, 4, 5, 6)",
            "        .as(StepVerifier::create)",
            "        .expectNext(0, 1, 2, 3, 4, 5, 6)",
            "        .verifyComplete();",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void refactorComplexDuplicates() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import java.util.Map;",
            "import reactor.core.publisher.Flux;",
            "import reactor.test.StepVerifier;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(Map.of(\"a\", \"b\".toUpperCase()), Map.of(\"c\", \"d\".toUpperCase()))",
            "        .as(StepVerifier::create)",
            "        .expectNext(Map.of(\"a\", \"b\".toUpperCase()))",
            "        .expectNext(Map.of(\"c\", \"d\".toUpperCase()))",
            "        .verifyComplete();",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import java.util.Map;",
            "import reactor.core.publisher.Flux;",
            "import reactor.test.StepVerifier;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(Map.of(\"a\", \"b\".toUpperCase()), Map.of(\"c\", \"d\".toUpperCase()))",
            "        .as(StepVerifier::create)",
            "        .expectNext(Map.of(\"a\", \"b\".toUpperCase()), Map.of(\"c\", \"d\".toUpperCase()))",
            "        .verifyComplete();",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void dontRefactorSingleCall() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "import reactor.test.StepVerifier;",
            "",
            "class A {",
            "  void m() {",
            "    Mono.just(0).as(StepVerifier::create).expectNext(0).verifyComplete();",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "import reactor.test.StepVerifier;",
            "",
            "class A {",
            "  void m() {",
            "    Mono.just(0).as(StepVerifier::create).expectNext(0).verifyComplete();",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void dontRefactorParent() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "import reactor.test.StepVerifier;",
            "",
            "class A {",
            "  void m() {",
            "    Mono.just(",
            "            Mono.just(Mono.just(0).as(StepVerifier::create).expectNext(0))",
            "                .as(StepVerifier::create)",
            "                .expectNext(Mono.just(0).as(StepVerifier::create).expectNext(0)))",
            "        .as(StepVerifier::create)",
            "        .expectNext(",
            "            Mono.just(Mono.just(0).as(StepVerifier::create).expectNext(0))",
            "                .as(StepVerifier::create)",
            "                .expectNext(Mono.just(0).as(StepVerifier::create).expectNext(0)));",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "import reactor.test.StepVerifier;",
            "",
            "class A {",
            "  void m() {",
            "    Mono.just(",
            "            Mono.just(Mono.just(0).as(StepVerifier::create).expectNext(0))",
            "                .as(StepVerifier::create)",
            "                .expectNext(Mono.just(0).as(StepVerifier::create).expectNext(0)))",
            "        .as(StepVerifier::create)",
            "        .expectNext(",
            "            Mono.just(Mono.just(0).as(StepVerifier::create).expectNext(0))",
            "                .as(StepVerifier::create)",
            "                .expectNext(Mono.just(0).as(StepVerifier::create).expectNext(0)));",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
