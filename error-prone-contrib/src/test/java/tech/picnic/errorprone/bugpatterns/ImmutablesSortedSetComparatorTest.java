package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class ImmutablesSortedSetComparatorTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(ImmutablesSortedSetComparator.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(ImmutablesSortedSetComparator.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import com.google.common.collect.ImmutableSortedSet;",
            "import org.immutables.value.Value;",
            "",
            "@Value.Immutable",
            "interface A {",
            "  // BUG: Diagnostic contains:",
            "  ImmutableSortedSet<String> sortedSet();",
            "",
            "  default ImmutableSortedSet<String> defaultSortedSet() {",
            "    return ImmutableSortedSet.of();",
            "  }",
            "",
            "  @Value.NaturalOrder",
            "  ImmutableSortedSet<String> defaultSortedSet2();",
            "}",
            "",
            "@Value.Modifiable",
            "interface B {",
            "  // BUG: Diagnostic contains:",
            "  ImmutableSortedSet<String> sortedSet();",
            "",
            "  default ImmutableSortedSet<String> defaultSortedSet() {",
            "    return ImmutableSortedSet.of();",
            "  }",
            "",
            "  @Value.NaturalOrder",
            "  ImmutableSortedSet<String> defaultSortedSet2();",
            "}",
            "",
            "@Value.Immutable",
            "abstract class C {",
            "  // BUG: Diagnostic contains:",
            "  abstract ImmutableSortedSet<String> sortedSet();",
            "",
            "  ImmutableSortedSet<String> defaultSortedSet() {",
            "    return ImmutableSortedSet.of();",
            "  }",
            "",
            "  @Value.NaturalOrder",
            "  abstract ImmutableSortedSet<String> defaultSortedSet2();",
            "}",
            "",
            "@Value.Modifiable",
            "abstract class D {",
            "  // BUG: Diagnostic contains:",
            "  abstract ImmutableSortedSet<String> sortedSet();",
            "",
            "  ImmutableSortedSet<String> defaultSortedSet() {",
            "    return ImmutableSortedSet.of();",
            "  }",
            "",
            "  @Value.NaturalOrder",
            "  abstract ImmutableSortedSet<String> defaultSortedSet2();",
            "}",
            "",
            "abstract class E {",
            "  abstract ImmutableSortedSet<String> sortedSet();",
            "}")
        .doTest();
  }

  @Test
  void replacementInImmutable() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import com.google.common.collect.ImmutableSortedSet;",
            "import org.immutables.value.Value;",
            "",
            "@Value.Immutable",
            "abstract class A {",
            "  abstract ImmutableSortedSet<String> sortedSet();",
            "",
            "  @Value.Immutable",
            "  interface B {",
            "    ImmutableSortedSet<String> sortedSet();",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import com.google.common.collect.ImmutableSortedSet;",
            "import org.immutables.value.Value;",
            "",
            "@Value.Immutable",
            "abstract class A {",
            "  @Value.NaturalOrder",
            "  abstract ImmutableSortedSet<String> sortedSet();",
            "",
            "  @Value.Immutable",
            "  interface B {",
            "    @Value.NaturalOrder",
            "    ImmutableSortedSet<String> sortedSet();",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementInModifiable() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import com.google.common.collect.ImmutableSortedSet;",
            "import org.immutables.value.Value;",
            "",
            "@Value.Modifiable",
            "abstract class A {",
            "  abstract ImmutableSortedSet<String> sortedSet();",
            "",
            "  @Value.Modifiable",
            "  interface B {",
            "    ImmutableSortedSet<String> sortedSet();",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import com.google.common.collect.ImmutableSortedSet;",
            "import org.immutables.value.Value;",
            "",
            "@Value.Modifiable",
            "abstract class A {",
            "  @Value.NaturalOrder",
            "  abstract ImmutableSortedSet<String> sortedSet();",
            "",
            "  @Value.Modifiable",
            "  interface B {",
            "    @Value.NaturalOrder",
            "    ImmutableSortedSet<String> sortedSet();",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
