package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class RefasterAnyOfUsageCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(RefasterAnyOfUsageCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(RefasterAnyOfUsageCheck.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import com.google.errorprone.refaster.Refaster;",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "",
            "class A {",
            "  @BeforeTemplate",
            "  String before(String str) {",
            "    // BUG: Diagnostic contains:",
            "    Refaster.anyOf();",
            "    // BUG: Diagnostic contains:",
            "    return Refaster.anyOf(str);",
            "  }",
            "",
            "  @BeforeTemplate",
            "  Object before2(String str, Object obj) {",
            "    return Refaster.anyOf(str, obj);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "import com.google.errorprone.refaster.Refaster;",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "",
            "class A {",
            "  @BeforeTemplate",
            "  String before(String str) {",
            "    Refaster.anyOf();",
            "    return Refaster.anyOf(str);",
            "  }",
            "}")
        .addOutputLines(
            "out/A.java",
            "import com.google.errorprone.refaster.Refaster;",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "",
            "class A {",
            "  @BeforeTemplate",
            "  String before(String str) {",
            "    Refaster.anyOf();",
            "    return str;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
