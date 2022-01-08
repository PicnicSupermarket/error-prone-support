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
            "import com.google.common.collect.ImmutableSortedMap;",
            "import com.google.common.collect.ImmutableSortedMultiset;",
            "import com.google.common.collect.ImmutableSortedSet;",
            "import com.google.common.collect.ImmutableTable;",
            "",
            "public final class Foo {",
            "  public void foo() {",
            "    // BUG: Diagnostic contains:",
            "    Byte b1 = Byte.valueOf((Byte) Byte.MIN_VALUE);",
            "    Byte b2 = Byte.valueOf(Byte.MIN_VALUE);",
            "    byte b3 = Byte.valueOf((Byte) Byte.MIN_VALUE);",
            "    // BUG: Diagnostic contains:",
            "    byte b4 = Byte.valueOf(Byte.MIN_VALUE);",
            "",
            "    // BUG: Diagnostic contains:",
            "    Character c1 = Character.valueOf((Character) 'a');",
            "    Character c2 = Character.valueOf('a');",
            "    char c3 = Character.valueOf((Character)'a');",
            "    // BUG: Diagnostic contains:",
            "    char c4 = Character.valueOf('a');",
            "",
            "    // BUG: Diagnostic contains:",
            "    Integer int1 = Integer.valueOf((Integer) 1);",
            "    Integer int2 = Integer.valueOf(1);",
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
            "    ImmutableSortedMap<Object, Object> i12 = ImmutableSortedMap.copyOf(ImmutableSortedMap.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableSortedMultiset<Object> i13 = ImmutableSortedMultiset.copyOf(ImmutableSortedMultiset.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableSortedSet<Object> i14 = ImmutableSortedSet.copyOf(ImmutableSortedSet.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableTable<Object, Object, Object> i15 = ImmutableTable.copyOf(ImmutableTable.of());",
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
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.util.ArrayList;",
            "",
            "public final class Foo {",
            "  public void foo() {",
            "    ImmutableSet<Object> set1 = ImmutableSet.copyOf(ImmutableSet.of());",
            "    ImmutableSet<Object> set2 = ImmutableSet.copyOf(ImmutableList.of());",
            "",
            "    ImmutableList<Integer> list1 = ImmutableList.copyOf(ImmutableList.of(1));",
            "    ImmutableList<Integer> list2 = ImmutableList.copyOf(new ArrayList<>(ImmutableList.of(1)));",
            "  }",
            "}")
        .addOutputLines(
            "Foo.java",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.util.ArrayList;",
            "",
            "public final class Foo {",
            "  public void foo() {",
            "    ImmutableSet<Object> set1 = ImmutableSet.of();",
            "    ImmutableSet<Object> set2 = ImmutableSet.copyOf(ImmutableList.of());",
            "",
            "    ImmutableList<Integer> list1 = ImmutableList.of(1);",
            "    ImmutableList<Integer> list2 = ImmutableList.copyOf(new ArrayList<>(ImmutableList.of(1)));",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementSecondSuggestedFix() {
    refactoringTestHelper
        .setFixChooser(FixChoosers.SECOND)
        .addInputLines(
            "Foo.java",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.util.ArrayList;",
            "",
            "public final class Foo {",
            "  public void foo() {",
            "    ImmutableSet<Object> set1 = ImmutableSet.copyOf(ImmutableSet.of());",
            "    ImmutableSet<Object> set2 = ImmutableSet.copyOf(ImmutableList.of());",
            "",
            "    ImmutableList<Integer> list1 = ImmutableList.copyOf(ImmutableList.of(1));",
            "    ImmutableList<Integer> list2 = ImmutableList.copyOf(new ArrayList<>(ImmutableList.of(1)));",
            "  }",
            "}")
        .addOutputLines(
            "Foo.java",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.util.ArrayList;",
            "",
            "public final class Foo {",
            "  public void foo() {",
            "    @SuppressWarnings(\"IdentityConversion\")",
            "    ImmutableSet<Object> set1 = ImmutableSet.copyOf(ImmutableSet.of());",
            "    ImmutableSet<Object> set2 = ImmutableSet.copyOf(ImmutableList.of());",
            "",
            "    @SuppressWarnings(\"IdentityConversion\")",
            "    ImmutableList<Integer> list1 = ImmutableList.copyOf(ImmutableList.of(1));",
            "    ImmutableList<Integer> list2 = ImmutableList.copyOf(new ArrayList<>(ImmutableList.of(1)));",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
