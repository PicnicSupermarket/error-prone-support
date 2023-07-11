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
            "    Mono.empty().zipWith(a);",
            "    Mono<Void> b = Mono.empty();",
            "    Mono<Integer> c = Mono.just(1);",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(a, b);",
            "    // BUG: Diagnostic contains:",
            "    b.zipWith(c);",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(a, Mono.empty());",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(Mono.empty(), Mono.empty());",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(a, c, b);",
            "",
            "  }",
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
            "    Mono.zip(a, a);",
            "    Mono.zip(Mono.empty(), a);",
            "    Mono.zip(Mono.empty(), Mono.empty());",
            "",
            "    Mono.empty().zipWith(a);",
            "    Mono.empty().zipWith(Mono.empty());",
            "    b.zipWith(b).zipWith(c).map(entry -> entry);",
            "    c.zipWith(Mono.empty());",
            "    c.zipWith(b);",
            "    c.zipWith(c);",
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
            "    Mono<Integer> c = Mono.just(1);",
            "    a.then(a);",
            "    Mono.empty().then(a);",
            "    Mono.empty().then(Mono.empty());",
            "",
            "    Mono.empty().concatWith(a);",
            "    Mono.empty().concatWith(Mono.empty());",
            "    b.concatWith(b).concatWith(c).map(entry -> entry);",
            "    c.concatWith(Mono.empty());",
            "    c.concatWith(b);",
            "    c.concatWith(c);",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
