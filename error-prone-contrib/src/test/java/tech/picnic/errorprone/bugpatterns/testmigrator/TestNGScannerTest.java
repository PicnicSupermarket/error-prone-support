package tech.picnic.errorprone.bugpatterns.testmigrator;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;

import com.google.common.collect.ImmutableMap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tech.picnic.errorprone.bugpatterns.TestNGMetadata;

final class TestNGScannerTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(TestNGScannerTest.TestChecker.class, getClass());

  @Test
  void classLevelAndMethodLevel() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import org.testng.annotations.DataProvider;",
            "import org.testng.annotations.Test;",
            "",
            "// BUG: Diagnostic contains:",
            "@Test",
            "class A {",
            "",
            "  void inferClassLevelAnnotation () {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Test(description = \"foo\")",
            "  void localAnnotation () {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Test(dataProvider = \"dataProviderTestCases\")",
            "  void dataProvider () {}",
            "",
            "  @DataProvider",
            "  // BUG: Diagnostic contains:",
            "  public static Object[][] dataProviderTestCases () {",
            "     return new Object[][] {{1},{2}};",
            "  }",
            "}")
        .doTest();
  }

  /**
   * Flags classes with a diagnostics message that indicates, whether a TestNG element was
   * collected.
   */
  @BugPattern(severity = ERROR, summary = "Interacts with `TestNGScanner` for testing purposes")
  public static final class TestChecker extends BugChecker
      implements BugChecker.CompilationUnitTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
      TestNGScanner scanner = new TestNGScanner(state);
      scanner.scan(tree, null);
      ImmutableMap<ClassTree, TestNGMetadata> metaDataMap = scanner.buildMetaDataTree();

      metaDataMap.forEach(
          (classTree, metaData) -> {
            metaData
                .getClassLevelAnnotation()
                .ifPresent(
                    annotation ->
                        state.reportMatch(
                            describeMatch(
                                annotation.getAnnotationTree(), SuggestedFix.emptyFix())));

            metaData
                .getDataProviders()
                .forEach(
                    dataProvider -> {
                      state.reportMatch(
                          describeMatch(dataProvider.getMethodTree(), SuggestedFix.emptyFix()));
                    });

            classTree.getMembers().stream()
                .filter(MethodTree.class::isInstance)
                .map(MethodTree.class::cast)
                .map(metaData::getAnnotation)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(not(isEqual(metaData.getClassLevelAnnotation().orElse(null))))
                .forEach(
                    annotation -> {
                      state.reportMatch(
                          describeMatch(annotation.getAnnotationTree(), SuggestedFix.emptyFix()));
                    });
          });

      return Description.NO_MATCH;
    }
  }
}
