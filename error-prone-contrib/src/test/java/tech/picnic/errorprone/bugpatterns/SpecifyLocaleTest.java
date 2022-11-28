package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugCheckerRefactoringTestHelper.newInstance;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class SpecifyLocaleTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(SpecifyLocale.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      newInstance(SpecifyLocale.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic contains:",
            "    \"a\".toUpperCase();",
            "",
            "    // BUG: Diagnostic contains:",
            "    \"b\".toLowerCase();",
            "",
            "    String c = \"c\";",
            "    // BUG: Diagnostic contains:",
            "    c.toUpperCase();",
            "",
            "    String d = \"d\";",
            "    // BUG: Diagnostic contains:",
            "    d.toLowerCase();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacementFirstSuggestedFix() {
    refactoringTestHelper
        .setFixChooser(FixChoosers.FIRST)
        .addInputLines(
            "A.java",
            "",
            "class A {",
            "  void m() {",
            "    \"a\".toUpperCase();",
            "    \"b\".toLowerCase();",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import java.util.Locale;",
            "",
            "class A {",
            "  void m() {",
            "    \"a\".toUpperCase(Locale.ROOT);",
            "    \"b\".toLowerCase(Locale.ROOT);",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementSecondSuggestedFix() {
    refactoringTestHelper
        .setFixChooser(FixChoosers.SECOND)
        .addInputLines(
            "A.java",
            "",
            "class A {",
            "  void m() {",
            "    \"a\".toUpperCase();",
            "    \"b\".toLowerCase();",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import java.util.Locale;",
            "",
            "class A {",
            "  void m() {",
            "    \"a\".toUpperCase(Locale.getDefault());",
            "    \"b\".toLowerCase(Locale.getDefault());",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
