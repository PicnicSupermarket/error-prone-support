package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class MissingRefasterAnnotationsCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(MissingRefasterAnnotationsCheck.class, getClass());
  //  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
  //      BugCheckerRefactoringTestHelper.newInstance(new AutowiredConstructorCheck(), getClass());

  @Test
  public void testIdentification() {
    compilationTestHelper
        .addSourceLines(
            "RefasterTemplateStringIsEmpty.java",
            "import com.google.errorprone.refaster.annotation.AfterTemplate;",
            "import com.google.errorprone.refaster.annotation.AlsoNegation;",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "",
            "final class RefasterTemplateStringIsEmpty {",
            "  private RefasterTemplateStringIsEmpty() {}",
            "",
            "  static final class StringIsEmpty {",
            "    @BeforeTemplate",
            "    boolean equalsEmptyString(String string) {",
            "      return string.equals(\"\");",
            "    }",
            "",
            "    // @BeforeTemplate, line is empty now, this should say bug!",
            "    boolean lengthEquals0(String string) {",
            "      return string.length() == 0;",
            "    }",
            "",
            "    @AfterTemplate",
//            "    @AlsoNegation",
            "    boolean optimizedMethod(String string) {",
            "      return string.isEmpty();",
            "    }",
            "  }",
            "}")
        .doTest();
  }

  //  @Test
  //  public void testReplacement() {
  //    refactoringTestHelper
  //        .addInputLines(
  //            "in/Container.java",
  //            "import org.springframework.beans.factory.annotation.Autowired;",
  //            "",
  //            "interface Container {",
  //            "  class A {",
  //            "    @Autowired @Deprecated A() {}",
  //            "  }",
  //            "",
  //            "  class B {",
  //            "    @Autowired B(String x) {}",
  //            "  }",
  //            "}")
  //        .addOutputLines(
  //            "out/Container.java",
  //            "import org.springframework.beans.factory.annotation.Autowired;",
  //            "",
  //            "interface Container {",
  //            "  class A {",
  //            "    @Deprecated A() {}",
  //            "  }",
  //            "",
  //            "  class B {",
  //            "    B(String x) {}",
  //            "  }",
  //            "}")
  //        .doTest(TestMode.TEXT_MATCH);
  //  }
}
