package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class RedundantStringEscapeTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(RedundantStringEscape.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.Arrays;",
            "import java.util.List;",
            "",
            "class A {",
            "  List<String> m() {",
            "    return Arrays.asList(",
            "        \"foo\",",
            "        \"ß\",",
            "        \"'\",",
            "        \"\\\"\",",
            "        \"\\\\\",",
            "        \"\\\\'\",",
            "        \"'\\\\\",",
            "        // BUG: Diagnostic contains:",
            "        \"\\\\\\'\",",
            "        // BUG: Diagnostic contains:",
            "        \"\\'\\\\\",",
            "        // BUG: Diagnostic contains:",
            "        \"\\'\",",
            "        // BUG: Diagnostic contains:",
            "        \"'\\'\",",
            "        // BUG: Diagnostic contains:",
            "        \"\\''\",",
            "        // BUG: Diagnostic contains:",
            "        \"\\'\\'\",",
            "        (",
            "        // BUG: Diagnostic contains:",
            "        /* Leading comment. */ \"\\'\" /* Trailing comment. */),",
            "        // BUG: Diagnostic contains:",
            "        \"\\'foo\\\"bar\\'baz\\\"qux\\'\");",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(RedundantStringEscape.class, getClass())
        .addInputLines(
            "A.java",
            "import java.util.Arrays;",
            "import java.util.List;",
            "",
            "class A {",
            "  List<String> m() {",
            "    return Arrays.asList(",
            "        \"\\'\",",
            "        \"'\\'\",",
            "        \"\\''\",",
            "        \"\\'\\'\",",
            "        \"\\'ß\\'\",",
            "        (",
            "        /* Leading comment. */ \"\\'\" /* Trailing comment. */),",
            "        \"\\'foo\\\"bar\\'baz\\\"qux\\'\");",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import java.util.Arrays;",
            "import java.util.List;",
            "",
            "class A {",
            "  List<String> m() {",
            "    return Arrays.asList(",
            "        \"'\",",
            "        \"''\",",
            "        \"''\",",
            "        \"''\",",
            "        \"'ß'\",",
            "        (",
            "        /* Leading comment. */ \"'\" /* Trailing comment. */),",
            "        \"'foo\\\"bar'baz\\\"qux'\");",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
