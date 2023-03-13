package tech.picnic.errorprone.testngjunit;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import org.junit.jupiter.api.Test;

final class TestNGMatchersTest {
  @Test
  void nGAnnotation() {
    CompilationTestHelper.newInstance(TestNGMatchersTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import org.testng.annotations.Test;",
            "",
            "// BUG: Diagnostic contains: TestNG annotation",
            "@Test",
            "class A {",
            "  // BUG: Diagnostic contains: TestNG annotation",
            "  @Test",
            "  void basic() {}",
            "",
            "  @org.junit.jupiter.api.Test",
            "  void junitTest() {}",
            "}")
        .doTest();
  }

  @Test
  void nGValueFactory() {
    CompilationTestHelper.newInstance(TestNGMatchersTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import org.testng.annotations.DataProvider;",
            "import org.testng.annotations.Test;",
            "",
            "// BUG: Diagnostic contains: TestNG annotation",
            "@Test",
            "class A {",
            "  // BUG: Diagnostic contains: TestNG annotation",
            "  @Test(dataProvider = \"dataProviderTestCases\")",
            "  void basic() {}",
            "",
            "  @DataProvider",
            "  // BUG: Diagnostic contains: TestNG value factory method",
            "  private static Object[][] dataProviderTestCases() {",
            "    return new Object[][] {};",
            "  }",
            "}")
        .doTest();
  }

  /**
   * A {@link com.google.errorprone.BugPattern} used to report TestNG annotations as errors for
   * testing purposes.
   */
  @BugPattern(summary = "Interacts with `TestNGMatchers` for testing purposes", severity = ERROR)
  public static final class TestNGMatchersTestChecker extends BugChecker
      implements MethodTreeMatcher, AnnotationTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchAnnotation(AnnotationTree annotationTree, VisitorState visitorState) {
      if (TestNGMatchers.TESTNG_TEST_ANNOTATION.matches(annotationTree, visitorState)) {
        return buildDescription(annotationTree).setMessage("TestNG annotation").build();
      }

      return Description.NO_MATCH;
    }

    @Override
    public Description matchMethod(MethodTree methodTree, VisitorState visitorState) {
      if (TestNGMatchers.TESTNG_VALUE_FACTORY_METHOD.matches(methodTree, visitorState)) {
        return buildDescription(methodTree).setMessage("TestNG value factory method").build();
      }

      return Description.NO_MATCH;
    }
  }
}
