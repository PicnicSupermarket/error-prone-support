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
            "import static java.util.Locale.ROOT;",
            "",
            "import java.util.Locale;",
            "",
            "class A {",
            "  void m() {",
            "    \"a\".toLowerCase(Locale.ROOT);",
            "    \"a\".toUpperCase(Locale.ROOT);",
            "    \"b\".toLowerCase(ROOT);",
            "    \"b\".toUpperCase(ROOT);",
            "    \"c\".toLowerCase(Locale.getDefault());",
            "    \"c\".toUpperCase(Locale.getDefault());",
            "    \"d\".toLowerCase(Locale.ENGLISH);",
            "    \"d\".toUpperCase(Locale.ENGLISH);",
            "    \"e\".toLowerCase(new Locale(\"foo\"));",
            "    \"e\".toUpperCase(new Locale(\"foo\"));",
            "",
            "    // BUG: Diagnostic contains:",
            "    \"f\".toLowerCase();",
            "",
            "    // BUG: Diagnostic contains:",
            "    \"g\".toUpperCase();",
            "",
            "    String h = \"h\";",
            "    // BUG: Diagnostic contains:",
            "    h.toLowerCase();",
            "",
            "    String i = \"i\";",
            "    // BUG: Diagnostic contains:",
            "    i.toUpperCase();",
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
            "    \"a\".toLowerCase();",
            "    \"b\".toUpperCase();",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import java.util.Locale;",
            "",
            "class A {",
            "  void m() {",
            "    \"a\".toLowerCase(Locale.ROOT);",
            "    \"b\".toUpperCase(Locale.ROOT);",
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
            "    \"a\".toLowerCase();",
            "    \"b\".toUpperCase();",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import java.util.Locale;",
            "",
            "class A {",
            "  void m() {",
            "    \"a\".toLowerCase(Locale.getDefault());",
            "    \"b\".toUpperCase(Locale.getDefault());",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
