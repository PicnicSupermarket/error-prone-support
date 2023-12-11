package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class MonoZipOfMonoVoidUsageTest {
  /**
   * Line 44 won't be reported as a bug. It's quite hard to catch this case as {@code Mono.empty()}
   * yields {@code Mono<Object>}, so matcher will be too wide. Additionally, it's not expected to
   * occur in the real production code.
   *
   * <p>Line 25 needed to simulate the unwanted intrinsic operations, which are not intended to be
   * processed by the rule.
   */
  @Test
  void identification() {
    CompilationTestHelper.newInstance(MonoZipOfMonoVoidUsage.class, getClass())
        .addSourceLines(
            "A.java",
            "import reactor.core.publisher.Mono;",
            "",
            "public final class A<T> {",
            "  void m(T t) {",
            "    Class clazz = getClass();",
            "    A<Integer> instance = new A();",
            "    Mono<Void> a = Mono.empty();",
            "    Mono<Integer> b = Mono.empty();",
            "    Mono<Integer> c = Mono.just(1);",
            "    Mono<Integer> d = this.publisher();",
            "    Mono<T> e = Mono.just(t);",
            "    // BUG: Diagnostic contains: `Mono#zip` and `Mono#zipWith` should not be executed against",
            "    // `Mono#empty` or `Mono<Void>` parameter; please revisit the parameters used and make sure to",
            "    // supply correct publishers instead",
            "    Mono.zip(a, a);",
            "    Mono.zip(e, e);",
            "    e.zipWith(e);",
            "    // BUG: Diagnostic contains: `Mono#zip` and `Mono#zipWith` should not be executed against",
            "    // `Mono#empty` or `Mono<Void>` parameter; please revisit the parameters used and make sure to",
            "    // supply correct publishers instead",
            "    Mono.zip(d, c, b, a);",
            "    Mono.zip(d, c, b);",
            "    b.zipWith(b).zipWith(c).map(entry -> entry);",
            "    Mono.zip(d, Mono.empty());",
            "    c.zipWith(b);",
            "    c.zipWith(d);",
            "    Mono.just(1).zipWith(Mono.just(1));",
            "    Mono.zip(Mono.just(1), Mono.just(1));",
            "    c.zipWith(c);",
            "    // BUG: Diagnostic contains: `Mono#zip` and `Mono#zipWith` should not be executed against",
            "    // `Mono#empty` or `Mono<Void>` parameter; please revisit the parameters used and make sure to",
            "    // supply correct publishers instead",
            "    c.zipWith(a);",
            "    // BUG: Diagnostic contains: `Mono#zip` and `Mono#zipWith` should not be executed against",
            "    // `Mono#empty` or `Mono<Void>` parameter; please revisit the parameters used and make sure to",
            "    // supply correct publishers instead",
            "    a.zipWith(c);",
            "    instance.zipWith(a);",
            "    c.zipWith(b, (first, second) -> first + second);",
            "    // BUG: Diagnostic contains: `Mono#zip` and `Mono#zipWith` should not be executed against",
            "    // `Mono#empty` or `Mono<Void>` parameter; please revisit the parameters used and make sure to",
            "    // supply correct publishers instead",
            "    a.zipWith(c, (first, second) -> second);",
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
