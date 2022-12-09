package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class RefasterRuleNamingTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(RefasterRuleNaming.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(RefasterRuleNaming.class, getClass());

  // XXX: We should instead look whether we flag a Refaster rule COLLECTION.
  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "",
            "final class A {",
            "  @BeforeTemplate",
            "  String before(String str) {",
            "    return str;",
            "  }",
            "",
            "  String nonRefasterMethod(String str) {",
            "    return str;",
            "  }",
            "",
            "  static final class Inner {",
            "    @BeforeTemplate",
            "    String before(String str) {",
            "      return str;",
            "    }",
            "  }",
            "}")
        .doTest();
  }
}
