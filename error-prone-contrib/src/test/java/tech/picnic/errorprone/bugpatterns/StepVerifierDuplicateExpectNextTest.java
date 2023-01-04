package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugCheckerRefactoringTestHelper.newInstance;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public class StepVerifierDuplicateExpectNextTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(StepVerifierDuplicateExpectNext.class, getClass());
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
            "     Flux.just(0, 1).as(StepVerifier::create).expectNext(0).expectNext(1);",
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
            "     Flux.just(0, 1).as(StepVerifier::create).expectNext(0).expectNext(1).verifyComplete();",
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
                    "     Flux.just(0, 1, 2).as(StepVerifier::create).expectNext(0).expectNext(1).expectNext(2).verifyComplete();",
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
  void dontRefactorSingleCall() {
    refactoringTestHelper
            .addInputLines(
                    "A.java",
                    "import reactor.core.publisher.Flux;",
                    "import reactor.test.StepVerifier;",
                    "",
                    "class A {",
                    "  void m() {",
                    "     Flux.just(0, 1).as(StepVerifier::create).expectNext(0).verifyComplete();",
                    "  }",
                    "}")
            .addOutputLines(
                    "A.java",
                    "import reactor.core.publisher.Flux;",
                    "import reactor.test.StepVerifier;",
                    "",
                    "class A {",
                    "  void m() {",
                    "    Flux.just(0, 1).as(StepVerifier::create).expectNext(0).verifyComplete();",
                    "  }",
                    "}")
            .doTest(TestMode.TEXT_MATCH);
  }
}
