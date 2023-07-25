package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class MonoZipOfMonoVoidUsageTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(MonoZipOfMonoVoidUsage.class, getClass())
        .addSourceLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "  void m() {",
            "    Mono<Void> a = Mono.empty();",
            "    Mono<Integer> b = Mono.empty();",
            "    Mono<Integer> c = Mono.just(1);",
            "    Mono<Integer> d = this.publisher();",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(a, a);",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(Mono.empty(), a);",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(Mono.empty(), Mono.empty());",
            "    // BUG: Diagnostic contains:",
            "    Mono.empty().zipWith(a);",
            "    // BUG: Diagnostic contains:",
            "    Mono.empty().zipWith(Mono.empty());",
            "    // BUG: Diagnostic contains:",
            "    b.zipWith(b).zipWith(c).map(entry -> entry);",
            "    // BUG: Diagnostic contains:",
            "    c.zipWith(Mono.empty());",
            "    // BUG: Diagnostic contains:",
            "    c.zipWith(b);",
            "    c.zipWith(d);",
            "    Mono.just(1).zipWith(Mono.just(1));",
            "    c.zipWith(c);",
            "  }",
            "",
            "  private Mono<Integer> publisher() {return Mono.empty();}",
            "}")
        .doTest();
  }

  @Test
  void replacementFirstSuggestedFix() {
    BugCheckerRefactoringTestHelper.newInstance(MonoZipOfMonoVoidUsage.class, getClass())
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "  void m() {",
            "    Mono<Void> a = Mono.empty();",
            "    Mono<Integer> b = Mono.empty();",
            "    Mono<Integer> c = Mono.just(1);",
            "    Mono<Integer> d = this.publisher();",
            "    Mono.zip(a, a);",
            "    Mono.zip(Mono.empty(), a);",
            "    Mono.zip(Mono.empty(), Mono.empty());",
            "",
            "    Mono.empty().zipWith(a);",
            "    Mono.empty().zipWith(Mono.empty());",
            "    b.zipWith(b).zipWith(c).map(entry -> entry);",
            "    c.zipWith(Mono.empty());",
            "    c.zipWith(b);",
            "    c.zipWith(d);",
            "    Mono.just(1).zipWith(Mono.just(1));",
            "    c.zipWith(c);",
            "  }",
            "",
            "  private Mono<Integer> publisher() {return Mono.empty();}",
            "}")
        .addOutputLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "  void m() {",
            "    Mono<Void> a = Mono.empty();",
            "    Mono<Integer> b = Mono.empty();",
            "    Mono<Integer> c = Mono.just(1);",
            "    Mono<Integer> d = this.publisher();",
            "    a.then(a);",
            "    Mono.empty().then(a);",
            "    Mono.empty().then(Mono.empty());",
            "",
            "    Mono.empty().concatWith(a);",
            "    Mono.empty().concatWith(Mono.empty());",
            "    b.concatWith(b).concatWith(c).map(entry -> entry);",
            "    c.concatWith(Mono.empty());",
            "    c.concatWith(b);",
            "    c.zipWith(d);",
            "    Mono.just(1).zipWith(Mono.just(1));",
            "    c.zipWith(c);",
            "  }",
            "",
            "  private Mono<Integer> publisher() {return Mono.empty();}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementFirstSuggestedFixWithCompilationErrors() {
    BugCheckerRefactoringTestHelper.newInstance(MonoZipOfMonoVoidUsage.class, getClass())
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "  void m() {",
            "    Mono<Void> a = Mono.empty();",
            "    Mono<Integer> b = Mono.empty();",
            "    Mono.zip(a, b);",
            "    a.zipWith(b);",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "",
            "class A {",
            "  void m() {",
            "    Mono<Void> a = Mono.empty();",
            "    Mono<Integer> b = Mono.empty();",
            "    a.then(b);",
            "    a.concatWith(b);",
            "  }",
            "}")
        .allowBreakingChanges()
        .doTest(TestMode.TEXT_MATCH);
  }
}
