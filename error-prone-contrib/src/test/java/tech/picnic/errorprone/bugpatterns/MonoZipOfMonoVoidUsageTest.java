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
            "final class A {",
            "  <T> void m(T t) {",
            "    Class clazz = getClass();",
            "    A instance = new A();",
            "",
            "    Mono<Void> a = Mono.empty();",
            "    Mono<Integer> b = Mono.empty();",
            "    Mono<Integer> c = Mono.just(1);",
            "    Mono<Integer> d = this.publisher();",
            "    Mono<T> e = Mono.just(t);",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(a, a);",
            "    Mono.zip(e, e);",
            "    e.zipWith(e);",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(d, c, b, a);",
            "    Mono.zip(d, c, b);",
            "    b.zipWith(b).zipWith(c).map(entry -> entry);",
            "    // BUG: Diagnostic contains:",
            "    Mono.zip(d, Mono.empty());",
            "    c.zipWith(b);",
            "    c.zipWith(d);",
            "    Mono.just(1).zipWith(Mono.just(1));",
            "    Mono.zip(Mono.just(1), Mono.just(1));",
            "    c.zipWith(c);",
            "    // BUG: Diagnostic contains:",
            "    c.zipWith(a);",
            "    // BUG: Diagnostic contains:",
            "    a.zipWith(c);",
            "    instance.zipWith(a);",
            "    c.zipWith(b, (first, second) -> first + second);",
            "    // BUG: Diagnostic contains:",
            "    a.zipWith(c, (first, second) -> second);",
            "    // BUG: Diagnostic contains:",
            "    c.zipWith(Mono.empty());",
            "  }",
            "",
            "  private Mono<Integer> publisher() {",
            "    return Mono.empty();",
            "  }",
            "",
            "  private Mono<Integer> zipWith(Mono<Void> param) {",
            "    return Mono.empty();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(MonoZipOfMonoVoidUsage.class, getClass())
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "",
            "final class A {",
            "  public void m() {",
            "    Mono<Void> a = Mono.empty();",
            "",
            "    Mono.zip(a, a);",
            "    a.zipWith(a);",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "",
            "final class A {",
            "  @SuppressWarnings(\"MonoZipOfMonoVoidUsage\")",
            "  public void m() {",
            "    Mono<Void> a = Mono.empty();",
            "",
            "    Mono.zip(a, a);",
            "    a.zipWith(a);",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
