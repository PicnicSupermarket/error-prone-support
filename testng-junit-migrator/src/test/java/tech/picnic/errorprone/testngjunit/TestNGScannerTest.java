package tech.picnic.errorprone.testngjunit;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;

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
import com.sun.source.tree.Tree;
import java.util.Optional;
import javax.lang.model.element.Name;
import org.junit.jupiter.api.Test;
import tech.picnic.errorprone.testngjunit.TestNGMetadata.AnnotationMetadata;

final class TestNGScannerTest {
  @Test
  void classLevelAndMethodLevel() {
    CompilationTestHelper.newInstance(TestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import org.testng.annotations.Test;",
            "",
            "// BUG: Diagnostic contains: Class: A arguments: {}",
            "@Test",
            "class A {",
            "",
            "  public void inferClassLevelAnnotation() {}",
            "",
            "  void packagePrivateNotATest() {}",
            "",
            "  private void privateNotATest() {}",
            "",
            "  static void notATest() {}",
            "",
            "  public static void staticNotATest() {}",
            "",
            "  // BUG: Diagnostic contains: Class: A arguments: {}",
            "  @Test",
            "  public void localAnnotation() {}",
            "",
            "  // BUG: Diagnostic contains: Class: A arguments: {description=\"foo\"}",
            "  @Test(description = \"foo\")",
            "  public void singleArgument() {}",
            "",
            "  // BUG: Diagnostic contains: Class: A arguments: {priority=1, description=\"foo\"}",
            "  @Test(priority = 1, description = \"foo\")",
            "  public void multipleArguments() {}",
            "",
            "  // BUG: Diagnostic contains: Class: A arguments: {dataProvider=\"dataProviderTestCases\"}",
            "  @Test(dataProvider = \"dataProviderTestCases\")",
            "  public void dataProvider() {}",
            "",
            "  // BUG: Diagnostic contains: Class: B arguments: {description=\"nested\"}",
            "  @Test(description = \"nested\")",
            "  class B {",
            "    public void nestedTest() {}",
            "",
            "    // BUG: Diagnostic contains: Class: B arguments: {priority=1}",
            "    @Test(priority = 1)",
            "    public void nestedTestWithArguments() {}",
            "  }",
            "}")
        .doTest();
  }

  // XXX: Here we need to add some edge cases for the DataProvider probably?
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
            "  // BUG: Diagnostic contains: Class: A DataProvider: dataProviderTestCases",
            "  private static Object[][] dataProviderTestCases() {",
            "    return new Object[][] {{1}, {2}};",
            "  }",
            "",
            "  private static Object[][] notMigratableDataProviderTestCases() {",
            "    return Stream.of(1, 2, 3).map(i -> new Object[] {i}).toArray(Object[][]::new);",
            "  }",
            "}")
        .doTest();
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
            "}")
        .doTest();
  }

  @Test
  void normalClass() {
    CompilationTestHelper.newInstance(TestChecker.class, getClass())
        .addSourceLines("A.java", "class A {", "  private void foo() {}", "}")
        .doTest();
  }

  @Test
  void teardownAndSetupMethods() {
    CompilationTestHelper.newInstance(TestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import org.testng.annotations.AfterClass;",
            "import org.testng.annotations.AfterMethod;",
            "import org.testng.annotations.BeforeClass;",
            "import org.testng.annotations.BeforeMethod;",
            "",
            "class A {",
            "  @BeforeClass",
            "  // BUG: Diagnostic contains: Class: A SetupTearDown: BEFORE_CLASS",
            "  private static void beforeClass() {}",
            "",
            "  @BeforeMethod",
            "  // BUG: Diagnostic contains: Class: A SetupTearDown: BEFORE_METHOD",
            "  private void beforeMethod() {}",
            "",
            "  @AfterClass",
            "  // BUG: Diagnostic contains: Class: A SetupTearDown: AFTER_CLASS",
            "  private static void afterClass() {}",
            "",
            "  @AfterMethod",
            "  // BUG: Diagnostic contains: Class: A SetupTearDown: AFTER_METHOD",
            "  private void afterMethod() {}",
            "}")
        .doTest();
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
      ImmutableMap<ClassTree, TestNGMetadata> classMetaData =
          scanner.collectMetadataForClasses(tree);

      classMetaData.forEach(
          (classTree, metaData) -> {
            metaData
                .getClassLevelAnnotationMetadata()
                .ifPresent(annotation -> reportAnnotationMessage(state, classTree, annotation));

            metaData
                .getDataProviderMetadata()
                .values()
                .forEach(
                    dataProvider ->
                        reportMethodMessage(
                            classTree.getSimpleName(),
                            "DataProvider",
                            dataProvider.getName(),
                            dataProvider.getMethodTree(),
                            state));

            metaData
                .getSetupTeardown()
                .forEach(
                    (method, setupTearDownType) ->
                        reportMethodMessage(
                            classTree.getSimpleName(),
                            "SetupTearDown",
                            setupTearDownType.name(),
                            method,
                            state));

            classTree.getMembers().stream()
                .filter(MethodTree.class::isInstance)
                .map(MethodTree.class::cast)
                .map(metaData::getAnnotation)
                .flatMap(Optional::stream)
                .filter(not(isEqual(metaData.getClassLevelAnnotationMetadata().orElse(null))))
                .forEach(annotation -> reportAnnotationMessage(state, classTree, annotation));
          });

      return Description.NO_MATCH;
    }

    private void reportAnnotationMessage(
        VisitorState state, ClassTree classTree, AnnotationMetadata annotation) {
      state.reportMatch(
          buildDescription(annotation.getAnnotationTree())
              .setMessage(createMetaDataMessage(classTree, annotation))
              .build());
    }

    private void reportMethodMessage(
        Name className, String message, String name, Tree tree, VisitorState state) {
      state.reportMatch(
          buildDescription(tree)
              .setMessage(String.format("Class: %s %s: %s", className, message, name))
              .build());
    }

    private static String createMetaDataMessage(
        ClassTree classTree, AnnotationMetadata annotationMetadata) {
      return String.format(
          "Class: %s arguments: %s", classTree.getSimpleName(), annotationMetadata.getArguments());
    }
  }
}
