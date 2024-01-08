package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class MonoZipOfMonoVoidUsageTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(MonoZipOfMonoVoidUsage.class, getClass())
        .expectErrorMessage(
            "X",
            m ->
                m.contains(
                    "`Mono#zip` and `Mono#zipWith` should not be executed against "
                        + "`Mono#empty` or `Mono<Void>` parameter; please revisit the parameters used and make sure to "
                        + "supply correct publishers instead"))
        .addSourceLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "",
            "public final class A {",
            "  <T> void m(T t) {",
            "    // This line is needed to simulate the unwanted intrinsic operations, which are not intended to",
            "    // be",
            "    // processed by the rule but will be scanned anyway.",
            "    Class clazz = getClass();",
            "    A instance = new A();",
            "    Mono<Void> a = Mono.empty();",
            "    Mono<Integer> b = Mono.empty();",
            "    Mono<Integer> c = Mono.just(1);",
            "    Mono<Integer> d = this.publisher();",
            "    Mono<T> e = Mono.just(t);",
            "    // BUG: Diagnostic matches: X",
            "    Mono.zip(a, a);",
            "    Mono.zip(e, e);",
            "    e.zipWith(e);",
            "    // BUG: Diagnostic matches: X",
            "    Mono.zip(d, c, b, a);",
            "    Mono.zip(d, c, b);",
            "    b.zipWith(b).zipWith(c).map(entry -> entry);",
            "    // BUG: Diagnostic matches: X",
            "    Mono.zip(d, Mono.empty());",
            "    c.zipWith(b);",
            "    c.zipWith(d);",
            "    Mono.just(1).zipWith(Mono.just(1));",
            "    Mono.zip(Mono.just(1), Mono.just(1));",
            "    c.zipWith(c);",
            "    // BUG: Diagnostic matches: X",
            "    c.zipWith(a);",
            "    // BUG: Diagnostic matches: X",
            "    a.zipWith(c);",
            "    instance.zipWith(a);",
            "    c.zipWith(b, (first, second) -> first + second);",
            "    // BUG: Diagnostic matches: X",
            "    a.zipWith(c, (first, second) -> second);",
            "    // BUG: Diagnostic matches: X",
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
  void replacementSuggestedFix() {
    BugCheckerRefactoringTestHelper.newInstance(MonoZipOfMonoVoidUsage.class, getClass())
        .addInputLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "",
            "public final class A {",
            "  public void m() {",
            "    Mono<Void> a = Mono.empty();",
            "",
            "    Mono.zip(a, a);",
            "    a.zipWith(a);",
            "  }",
            "",
            "  public void m2() {",
            "    Class clazz = getClass();",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "",
            "public final class A {",
            "  @SuppressWarnings(\"MonoZipOfMonoVoidUsage\")",
            "  public void m() {",
            "    Mono<Void> a = Mono.empty();",
            "",
            "    Mono.zip(a, a);",
            "    a.zipWith(a);",
            "  }",
            "",
            "  public void m2() {",
            "    Class clazz = getClass();",
            "  }",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }
}
