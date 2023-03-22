package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class StringCaseLocaleUsageTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(StringCaseLocaleUsage.class, getClass())
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
            "    // BUG: Diagnostic contains:",
            "    \"g\".toUpperCase();",
            "",
            "    String h = \"h\";",
            "    // BUG: Diagnostic contains:",
            "    h.toLowerCase();",
            "    String i = \"i\";",
            "    // BUG: Diagnostic contains:",
            "    i.toUpperCase();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacementFirstSuggestedFix() {
    BugCheckerRefactoringTestHelper.newInstance(StringCaseLocaleUsage.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "  void m() {",
            "    \"a\".toLowerCase(/* Comment with parens: (). */ );",
            "    \"b\".toUpperCase();",
            "    \"c\".toLowerCase().toString();",
            "",
            "    toString().toLowerCase();",
            "    toString().toUpperCase /* Comment with parens: (). */();",
            "",
            "    this.toString().toLowerCase() /* Comment with parens: (). */;",
            "    this.toString().toUpperCase();",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import java.util.Locale;",
            "",
            "class A {",
            "  void m() {",
            "    \"a\".toLowerCase(/* Comment with parens: (). */ Locale.ROOT);",
            "    \"b\".toUpperCase(Locale.ROOT);",
            "    \"c\".toLowerCase(Locale.ROOT).toString();",
            "",
            "    toString().toLowerCase(Locale.ROOT);",
            "    toString().toUpperCase /* Comment with parens: (). */(Locale.ROOT);",
            "",
            "    this.toString().toLowerCase(Locale.ROOT) /* Comment with parens: (). */;",
            "    this.toString().toUpperCase(Locale.ROOT);",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementSecondSuggestedFix() {
    BugCheckerRefactoringTestHelper.newInstance(StringCaseLocaleUsage.class, getClass())
        .setFixChooser(FixChoosers.SECOND)
        .addInputLines(
            "A.java",
            "class A {",
            "  void m() {",
            "    \"a\".toLowerCase();",
            "    \"b\".toUpperCase(/* Comment with parens: (). */ );",
            "    \"c\".toLowerCase().toString();",
            "",
            "    toString().toLowerCase();",
            "    toString().toUpperCase /* Comment with parens: (). */();",
            "",
            "    this.toString().toLowerCase() /* Comment with parens: (). */;",
            "    this.toString().toUpperCase();",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import java.util.Locale;",
            "",
            "class A {",
            "  void m() {",
            "    \"a\".toLowerCase(Locale.getDefault());",
            "    \"b\".toUpperCase(/* Comment with parens: (). */ Locale.getDefault());",
            "    \"c\".toLowerCase(Locale.getDefault()).toString();",
            "",
            "    toString().toLowerCase(Locale.getDefault());",
            "    toString().toUpperCase /* Comment with parens: (). */(Locale.getDefault());",
            "",
            "    this.toString().toLowerCase(Locale.getDefault()) /* Comment with parens: (). */;",
            "    this.toString().toUpperCase(Locale.getDefault());",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
