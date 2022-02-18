package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class IdentityConversionCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(IdentityConversionCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(IdentityConversionCheck.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "Foo.java",
            "import com.google.common.collect.ImmutableBiMap;",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableListMultimap;",
            "import com.google.common.collect.ImmutableMap;",
            "import com.google.common.collect.ImmutableMultimap;",
            "import com.google.common.collect.ImmutableMultiset;",
            "import com.google.common.collect.ImmutableRangeMap;",
            "import com.google.common.collect.ImmutableRangeSet;",
            "import com.google.common.collect.ImmutableSet;",
            "import com.google.common.collect.ImmutableSetMultimap;",
            "import com.google.common.collect.ImmutableTable;",
            "import reactor.adapter.rxjava.RxJava2Adapter;",
            "import reactor.core.publisher.Flux;",
            "import reactor.core.publisher.Mono;",
            "",
            "public final class Foo {",
            "  public void foo() {",
            "    // BUG: Diagnostic contains:",
            "    Byte b1 = Byte.valueOf((Byte) Byte.MIN_VALUE);",
            "    Byte b2 = Byte.valueOf(Byte.MIN_VALUE);",
            "    // BUG: Diagnostic contains:",
            "    byte b3 = Byte.valueOf((Byte) Byte.MIN_VALUE);",
            "    // BUG: Diagnostic contains:",
            "    byte b4 = Byte.valueOf(Byte.MIN_VALUE);",
            "",
            "    // BUG: Diagnostic contains:",
            "    Character c1 = Character.valueOf((Character) 'a');",
            "    Character c2 = Character.valueOf('a');",
            "    // BUG: Diagnostic contains:",
            "    char c3 = Character.valueOf((Character)'a');",
            "    // BUG: Diagnostic contains:",
            "    char c4 = Character.valueOf('a');",
            "",
            "    // BUG: Diagnostic contains:",
            "    Integer int1 = Integer.valueOf((Integer) 1);",
            "    Integer int2 = Integer.valueOf(1);",
            "    // BUG: Diagnostic contains:",
            "    int int3 = Integer.valueOf((Integer) 1);",
            "    // BUG: Diagnostic contains:",
            "    int int4 = Integer.valueOf(1);",
            "",
            "    String s1 = String.valueOf(0);",
            "    // BUG: Diagnostic contains:",
            "    String s2 = String.valueOf(\"1\");",
            "",
            "    // BUG: Diagnostic contains:",
            "    ImmutableBiMap<Object, Object> i2 = ImmutableBiMap.copyOf(ImmutableBiMap.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableList<Object> i3 = ImmutableList.copyOf(ImmutableList.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableListMultimap<Object, Object> i4 = ImmutableListMultimap.copyOf(ImmutableListMultimap.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableMap<Object, Object> i5 = ImmutableMap.copyOf(ImmutableMap.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableMultimap<Object, Object> i6 = ImmutableMultimap.copyOf(ImmutableMultimap.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableMultiset<Object> i7 = ImmutableMultiset.copyOf(ImmutableMultiset.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableRangeMap<String, Object> i8 = ImmutableRangeMap.copyOf(ImmutableRangeMap.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableRangeSet<String> i9 = ImmutableRangeSet.copyOf(ImmutableRangeSet.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableSet<Object> i10 = ImmutableSet.copyOf(ImmutableSet.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableSetMultimap<Object, Object> i11 = ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableTable<Object, Object, Object> i12 = ImmutableTable.copyOf(ImmutableTable.of());",
            "",
            "    // BUG: Diagnostic contains:",
            "    Flux<Integer> f1 = Flux.just(1).flatMap(e -> RxJava2Adapter.fluxToFlowable(Flux.just(2)));",
            "    // BUG: Diagnostic contains:",
            "    Flux<Integer> f2 = Flux.concat(Flux.just(1));",
            "    // BUG: Diagnostic contains:",
            "    Flux<Integer> f3 = Flux.firstWithSignal(Flux.just(1));",
            "    // BUG: Diagnostic contains:",
            "    Flux<Integer> f4 = Flux.from(Flux.just(1));",
            "    // BUG: Diagnostic contains:",
            "    Flux<Integer> f5 = Flux.merge(Flux.just(1));",
            "",
            "    // BUG: Diagnostic contains:",
            "    Mono<Integer> m1 = Mono.from(Mono.just(1));",
            "    // BUG: Diagnostic contains:",
            "    Mono<Integer> m2 = Mono.fromDirect(Mono.just(1));",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacementFirstSuggestedFix() {
    refactoringTestHelper
        .setFixChooser(FixChoosers.FIRST)
        .addInputLines(
            "Foo.java",
            "import static org.mockito.Mockito.when;",
            "",
            "import com.google.common.collect.ImmutableCollection;",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.util.Collection;",
            "import java.util.ArrayList;",
            "import org.reactivestreams.Publisher;",
            "import reactor.adapter.rxjava.RxJava2Adapter;",
            "import reactor.core.publisher.Flux;",
            "import reactor.core.publisher.Mono;",
            "",
            "public final class Foo {",
            "  public void foo() {",
            "    ImmutableSet<Object> set1 = ImmutableSet.copyOf(ImmutableSet.of());",
            "    ImmutableSet<Object> set2 = ImmutableSet.copyOf(ImmutableList.of());",
            "",
            "    ImmutableCollection<Integer> list1 = ImmutableList.copyOf(ImmutableList.of(1));",
            "    ImmutableCollection<Integer> list2 = ImmutableList.copyOf(new ArrayList<>(ImmutableList.of(1)));",
            "",
            "    Collection<Integer> c1 = ImmutableSet.copyOf(ImmutableSet.of(1));",
            "    Collection<Integer> c2 = ImmutableList.copyOf(new ArrayList<>(ImmutableList.of(1)));",
            "",
            "    Flux<Integer> f1 = Flux.just(1).flatMap(e -> RxJava2Adapter.fluxToFlowable(Flux.just(2)));",
            "    Flux<Integer> f2 = Flux.concat(Flux.just(3));",
            "    Publisher<Integer> f3 = Flux.firstWithSignal(Flux.just(4));",
            "    Publisher<Integer> f4 = Flux.from(Flux.just(5));",
            "    Publisher<Integer> f5 = Flux.merge(Flux.just(6));",
            "",
            "    Mono<Integer> m1 = Mono.from(Mono.just(7));",
            "    Publisher<Integer> m2 = Mono.fromDirect(Mono.just(8));",
            "",
            "    bar(Flux.concat(Flux.just(9)));",
            "    bar(Mono.from(Mono.just(10)));",
            "",
            "    Object o1 = ImmutableSet.copyOf(ImmutableList.of());",
            "    Object o2 = ImmutableSet.copyOf(ImmutableSet.of());",
            "",
            "    when(\"foo\".contains(\"f\"))",
            "        .thenAnswer(inv-> ImmutableSet.copyOf(ImmutableList.of(1)));",
            "  }",
            "",
            "  void bar(Publisher<Integer> publisher) {}",
            "}")
        .addOutputLines(
            "Foo.java",
            "import static org.mockito.Mockito.when;",
            "",
            "import com.google.common.collect.ImmutableCollection;",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.util.Collection;",
            "import java.util.ArrayList;",
            "import org.reactivestreams.Publisher;",
            "import reactor.adapter.rxjava.RxJava2Adapter;",
            "import reactor.core.publisher.Flux;",
            "import reactor.core.publisher.Mono;",
            "",
            "public final class Foo {",
            "  public void foo() {",
            "    ImmutableSet<Object> set1 = ImmutableSet.of();",
            "    ImmutableSet<Object> set2 = ImmutableSet.copyOf(ImmutableList.of());",
            "",
            "    ImmutableCollection<Integer> list1 = ImmutableList.of(1);",
            "    ImmutableCollection<Integer> list2 = ImmutableList.copyOf(new ArrayList<>(ImmutableList.of(1)));",
            "",
            "    Collection<Integer> c1 = ImmutableSet.of(1);",
            "    Collection<Integer> c2 = ImmutableList.copyOf(new ArrayList<>(ImmutableList.of(1)));",
            "",
            "    Flux<Integer> f1 = Flux.just(1).flatMap(e -> Flux.just(2));",
            "    Flux<Integer> f2 = Flux.just(3);",
            "    Publisher<Integer> f3 = Flux.just(4);",
            "    Publisher<Integer> f4 = Flux.just(5);",
            "    Publisher<Integer> f5 = Flux.just(6);",
            "",
            "    Mono<Integer> m1 = Mono.just(7);",
            "    Publisher<Integer> m2 = Mono.just(8);",
            "",
            "    bar(Flux.just(9));",
            "    bar(Mono.just(10));",
            "",
            "    Object o1 = ImmutableSet.copyOf(ImmutableList.of());",
            "    Object o2 = ImmutableSet.of();",
            "",
            "    when(\"foo\".contains(\"f\"))",
            "        .thenAnswer(inv-> ImmutableSet.copyOf(ImmutableList.of(1)));",
            "  }",
            "",
            "  void bar(Publisher<Integer> publisher) {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementSecondSuggestedFix() {
    refactoringTestHelper
        .setFixChooser(FixChoosers.SECOND)
        .addInputLines(
            "Foo.java",
            "import com.google.common.collect.ImmutableCollection;",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.util.ArrayList;",
            "import reactor.adapter.rxjava.RxJava2Adapter;",
            "import reactor.core.publisher.Flux;",
            "import reactor.core.publisher.Mono;",
            "",
            "public final class Foo {",
            "  public void foo() {",
            "    ImmutableSet<Object> set1 = ImmutableSet.copyOf(ImmutableSet.of());",
            "    ImmutableSet<Object> set2 = ImmutableSet.copyOf(ImmutableList.of());",
            "",
            "    ImmutableCollection<Integer> list1 = ImmutableList.copyOf(ImmutableList.of(1));",
            "    ImmutableCollection<Integer> list2 = ImmutableList.copyOf(new ArrayList<>(ImmutableList.of(1)));",
            "  }",
            "}")
        .addOutputLines(
            "Foo.java",
            "import com.google.common.collect.ImmutableCollection;",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.util.ArrayList;",
            "import reactor.adapter.rxjava.RxJava2Adapter;",
            "import reactor.core.publisher.Flux;",
            "import reactor.core.publisher.Mono;",
            "",
            "public final class Foo {",
            "  public void foo() {",
            "    @SuppressWarnings(\"IdentityConversion\")",
            "    ImmutableSet<Object> set1 = ImmutableSet.copyOf(ImmutableSet.of());",
            "    ImmutableSet<Object> set2 = ImmutableSet.copyOf(ImmutableList.of());",
            "",
            "    @SuppressWarnings(\"IdentityConversion\")",
            "    ImmutableCollection<Integer> list1 = ImmutableList.copyOf(ImmutableList.of(1));",
            "    ImmutableCollection<Integer> list2 = ImmutableList.copyOf(new ArrayList<>(ImmutableList.of(1)));",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
