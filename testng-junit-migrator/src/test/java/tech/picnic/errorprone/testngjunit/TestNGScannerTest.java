package tech.picnic.errorprone.testngjunit;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

import com.google.common.collect.ImmutableMap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

final class TestNGScannerTest {
  // XXX: We are missing some tests here.
  // - JUnit class that is already migrated.
  // - Normal class that shouldn't trigger? Or a negative example at least.
  // - Some not supported things as well.
  @Test
  void classLevelAndMethodLevel() {
    CompilationTestHelper.newInstance(TestChecker.class, getClass())
        .addSourceLines(
            "A.java",
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

  @Test
  void dataProvider() {
    CompilationTestHelper.newInstance(TestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.stream.Stream;",
            "import org.testng.annotations.DataProvider;",
            "",
            "class A {",
            "  @DataProvider",
            "  // BUG: Diagnostic contains: class: A dataProvider: dataProviderTestCases",
            "  private static Object[][] dataProviderTestCases() {",
            "    return new Object[][] {{1}, {2}};",
            "  }",
            "",
            "  private static Object[][] notMigratableDataProviderTestCases() {",
            "    return Stream.of(1, 2, 3).map(i -> new Object[] {i}).toArray(Object[][]::new);",
            "  }",
            "}");
  }

  @Test
  void junitTestClass() {
    CompilationTestHelper.newInstance(TestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import org.junit.jupiter.api.Test;",
            "",
            "class A {",
            "  @Test",
            "  private void foo() {}",
            "}");
  }

  /**
   * A {@link BugChecker} that flags classes with a diagnostics message that indicates, whether a
   * TestNG element was collected.
   */
  @BugPattern(severity = ERROR, summary = "Interacts with `TestNGScanner` for testing purposes")
  public static final class TestChecker extends BugChecker implements CompilationUnitTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
      TestNGScanner scanner = new TestNGScanner(state);
      scanner.scan(tree, null);
      ImmutableMap<ClassTree, TestNGMetadata> classMetaData =
          scanner.buildMetaDataForEachClassTree();

      classMetaData.forEach(
          (classTree, metaData) -> {
            metaData
                .getClassLevelAnnotationMetadata()
                .ifPresent(
                    annotation ->
                        state.reportMatch(
                            buildDescription(annotation.getAnnotationTree())
                                .setMessage(
                                    createAnnotationDiagnosticMessage(classTree, annotation, state))
                                .build()));

            metaData
                .getDataProviderMetadata()
                .values()
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
                .filter(not(isEqual(metaData.getClassLevelAnnotationMetadata().orElse(null))))
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
        ClassTree classTree,
        TestNGMetadata.AnnotationMetadata annotationMetadata,
        VisitorState state) {
      return String.format(
          "class: %s arguments: { %s }",
          classTree.getSimpleName(),
          annotationMetadata.getArguments().entrySet().stream()
              .map(
                  entry ->
                      String.join(
                          ": ", entry.getKey(), SourceCode.treeToString(entry.getValue(), state)))
              .collect(joining(", ")));
    }
  }
}
