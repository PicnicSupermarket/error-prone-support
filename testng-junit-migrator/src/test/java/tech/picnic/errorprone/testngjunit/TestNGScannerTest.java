package tech.picnic.errorprone.testngjunit;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;

import com.google.common.collect.ImmutableMap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.util.Map;
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
            "// BUG: Diagnostic contains: Class: A attributes: {}",
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
            "  // BUG: Diagnostic contains: Class: A attributes: {}",
            "  @Test",
            "  public void localAnnotation() {}",
            "",
            "  // BUG: Diagnostic contains: Class: A attributes: {description=\"foo\"}",
            "  @Test(description = \"foo\")",
            "  public void singleArgument() {}",
            "",
            "  // BUG: Diagnostic contains: Class: A attributes: {priority=1, description=\"foo\"}",
            "  @Test(priority = 1, description = \"foo\")",
            "  public void multipleArguments() {}",
            "",
            "  // BUG: Diagnostic contains: Class: A attributes: {dataProvider=\"dataProviderTestCases\"}",
            "  @Test(dataProvider = \"dataProviderTestCases\")",
            "  public void dataProvider() {}",
            "",
            "  @SuppressWarnings(\"onlyMatchTestNGAnnotations\")",
            "  // BUG: Diagnostic contains: Class: B attributes: {description=\"nested\"}",
            "  @Test(description = \"nested\")",
            "  class B {",
            "    public void nestedTest() {}",
            "",
            "    // BUG: Diagnostic contains: Class: B attributes: {priority=1}",
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
            "",
            "  private static Object[][] notMigratableDataProvider2TestCases() {",
            "    Object[][] testCases = new Object[][] {{1}, {2}};",
            "    return testCases;",
            "  }",
            "",
            "  private static Object[] notMigratableDataProvider3TestCases() {",
            "    return new Object[] {new Object[] {1}, new Object[] {2}};",
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
  public static final class TestChecker extends BugChecker
      implements CompilationUnitTreeMatcher, ClassTreeMatcher, MethodTreeMatcher {
    private static final long serialVersionUID = 1L;
    // XXX: find better way to do this
    private ImmutableMap<ClassTree, TestNGMetadata> classMetaData = ImmutableMap.of();

    @Override
    public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
      TestNGScanner scanner = new TestNGScanner(state);
      classMetaData = scanner.collectMetadataForClasses(tree);

      return Description.NO_MATCH;
    }

    @Override
    public Description matchClass(ClassTree tree, VisitorState state) {
      Optional.ofNullable(classMetaData.get(tree))
          .flatMap(TestNGMetadata::getClassLevelAnnotationMetadata)
          .ifPresent(annotation -> reportAnnotationMessage(tree, annotation, state));
      return Description.NO_MATCH;
    }

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      ClassTree classTree = state.findEnclosing(ClassTree.class);
      Optional<TestNGMetadata> metadata = Optional.ofNullable(classTree).map(classMetaData::get);

      if (metadata.isEmpty()) {
        return Description.NO_MATCH;
      }

      reportClassLevelAnnotation(classTree, metadata.orElseThrow(), state);
      reportTestMethods(tree, classTree, metadata.orElseThrow(), state);
      reportDataProviderMethods(tree, classTree, metadata.orElseThrow(), state);
      reportSetupTeardownMethods(tree, classTree, metadata.orElseThrow(), state);

      return Description.NO_MATCH;
    }

    private void reportClassLevelAnnotation(
        ClassTree classTree, TestNGMetadata metadata, VisitorState state) {
      metadata
          .getClassLevelAnnotationMetadata()
          .ifPresent(annotation -> reportAnnotationMessage(classTree, annotation, state));
    }

    private void reportTestMethods(
        MethodTree tree, ClassTree classTree, TestNGMetadata metadata, VisitorState state) {
      metadata
          .getClassLevelAnnotationMetadata()
          .filter(not(isEqual(metadata.getMethodAnnotations().get(tree))))
          .map(unused -> metadata.getMethodAnnotations().get(tree))
          .ifPresent(annotation -> reportAnnotationMessage(classTree, annotation, state));
    }

    private void reportSetupTeardownMethods(
        MethodTree tree, ClassTree classTree, TestNGMetadata metadata, VisitorState state) {
      metadata.getSetupTeardown().entrySet().stream()
          .filter(entry -> entry.getKey().equals(tree))
          .findFirst()
          .ifPresent(
              entry ->
                  reportMethodMessage(
                      classTree.getSimpleName(),
                      "SetupTearDown",
                      entry.getValue().name(),
                      entry.getKey(),
                      state));
    }

    private void reportDataProviderMethods(
        MethodTree tree, ClassTree classTree, TestNGMetadata metadata, VisitorState state) {
      metadata.getDataProviderMetadata().entrySet().stream()
          .filter(entry -> entry.getValue().getMethodTree().equals(tree))
          .findFirst()
          .map(Map.Entry::getValue)
          .ifPresent(
              dataProvider -> {
                reportMethodMessage(
                    classTree.getSimpleName(),
                    "DataProvider",
                    dataProvider.getName(),
                    dataProvider.getMethodTree(),
                    state);
              });
    }

    private void reportAnnotationMessage(
        ClassTree classTree, AnnotationMetadata annotation, VisitorState state) {
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
          "Class: %s attributes: %s",
          classTree.getSimpleName(), annotationMetadata.getAttributes());
    }
  }
}
