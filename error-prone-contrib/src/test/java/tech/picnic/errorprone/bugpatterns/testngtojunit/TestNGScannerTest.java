package tech.picnic.errorprone.bugpatterns.testngtojunit;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

import com.google.common.collect.ImmutableMap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

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
            "// BUG: Diagnostic contains: class: A arguments: {  }",
            "@Test",
            "class A {",
            "",
            "  void inferClassLevelAnnotation() {}",
            "",
            "  // BUG: Diagnostic contains: class: A arguments: {  }",
            "  @Test",
            "  void localAnnotation() {}",
            "",
            "  // BUG: Diagnostic contains: class: A arguments: { description: \"foo\" }",
            "  @Test(description = \"foo\")",
            "  void singleArgument() {}",
            "",
            "  // BUG: Diagnostic contains: class: A arguments: { priority: 1, description: \"foo\" }",
            "  @Test(priority = 1, description = \"foo\")",
            "  void multipleArguments() {}",
            "",
            "  // BUG: Diagnostic contains: class: A arguments: { dataProvider: \"dataProviderTestCases\" }",
            "  @Test(dataProvider = \"dataProviderTestCases\")",
            "  void dataProvider() {}",
            "",
            "  @DataProvider",
            "  // BUG: Diagnostic contains: class: A dataProvider: dataProviderTestCases",
            "  private static Object[][] dataProviderTestCases() {",
            "    return new Object[][] {{1}, {2}};",
            "  }",
            "",
            "  // BUG: Diagnostic contains: class: B arguments: { description: \"nested\" }",
            "  @Test(description = \"nested\")",
            "  class B {",
            "    public void nestedTest() {}",
            "",
            "    // BUG: Diagnostic contains: class: B arguments: { priority: 1 }",
            "    @Test(priority = 1)",
            "    public void nestedTestWithArguments() {}",
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
      ImmutableMap<ClassTree, TestNGMetadata> classMetaData = scanner.buildMetaDataTree();

      classMetaData.forEach(
          (classTree, metaData) -> {
            metaData
                .getClassLevelAnnotation()
                .ifPresent(
                    annotation ->
                        state.reportMatch(
                            buildDescription(annotation.getAnnotationTree())
                                .setMessage(
                                    createAnnotationDiagnosticMessage(classTree, annotation, state))
                                .build()));

            metaData
                .getDataProviders()
                .forEach(
                    dataProvider -> {
                      state.reportMatch(
                          buildDescription(dataProvider.getMethodTree())
                              .setMessage(
                                  String.format(
                                      "class: %s dataProvider: %s",
                                      classTree.getSimpleName(), dataProvider.getName()))
                              .build());
                    });

            classTree.getMembers().stream()
                .filter(MethodTree.class::isInstance)
                .map(MethodTree.class::cast)
                .map(metaData::getAnnotation)
                .flatMap(Optional::stream)
                .filter(not(isEqual(metaData.getClassLevelAnnotation().orElse(null))))
                .forEach(
                    annotation -> {
                      state.reportMatch(
                          buildDescription(annotation.getAnnotationTree())
                              .setMessage(
                                  createAnnotationDiagnosticMessage(classTree, annotation, state))
                              .build());
                    });
          });

      return Description.NO_MATCH;
    }

    private static String createAnnotationDiagnosticMessage(
        ClassTree classTree, TestNGMetadata.Annotation annotation, VisitorState state) {
      return String.format(
          "class: %s arguments: { %s }",
          classTree.getSimpleName(),
          annotation.getArguments().entrySet().stream()
              .map(
                  entry ->
                      String.join(
                          ": ", entry.getKey(), SourceCode.treeToString(entry.getValue(), state)))
              .collect(joining(", ")));
    }
  }
}