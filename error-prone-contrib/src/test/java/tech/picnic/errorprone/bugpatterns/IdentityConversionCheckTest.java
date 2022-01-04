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
            "    Integer i1 = Integer.valueOf((Integer) 1);",
            "    Integer i2 = Integer.valueOf(1);",
            "    int i3 = Integer.valueOf((Integer) 1);",
            "    // BUG: Diagnostic contains:",
            "    int i4 = Integer.valueOf(1);",
            "",
            "    String s1 = String.valueOf(0);",
            "    // BUG: Diagnostic contains:",
            "    String s2 = String.valueOf(\"1\");",
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
