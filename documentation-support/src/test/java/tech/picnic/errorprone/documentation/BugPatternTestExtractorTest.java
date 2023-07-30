package tech.picnic.errorprone.documentation;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class BugPatternTestExtractorTest {
  @Test
  void noTestClass(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerWithoutAnnotation.java",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "public final class TestCheckerWithoutAnnotation extends BugChecker {}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void noDoTestInvocation(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addSourceLines(\"A.java\", \"// BUG: Diagnostic contains:\", \"class A {}\");",
        "",
        "    BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addInputLines(\"A.java\", \"class A {}\")",
        "        .addOutputLines(\"A.java\", \"class A { /* This is a change. */ }\");",
        "  }",
        "}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void nullBugCheckerInstance(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class TestCheckerTest {",
        "  void m() {",
        "    CompilationTestHelper.newInstance((Class<BugChecker>) null, getClass())",
        "        .addSourceLines(\"A.java\", \"// BUG: Diagnostic contains:\", \"class A {}\")",
        "        .doTest();",
        "",
        "    BugCheckerRefactoringTestHelper.newInstance((Class<BugChecker>) null, getClass())",
        "        .addInputLines(\"A.java\", \"class A {}\")",
        "        .addOutputLines(\"A.java\", \"class A { /* This is a change. */ }\")",
        "        .doTest();",
        "  }",
        "}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void rawBugCheckerInstance(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  @SuppressWarnings(\"unchecked\")",
        "  void m() {",
        "    @SuppressWarnings(\"rawtypes\")",
        "    Class bugChecker = TestChecker.class;",
        "",
        "    CompilationTestHelper.newInstance(bugChecker, getClass())",
        "        .addSourceLines(\"A.java\", \"// BUG: Diagnostic contains:\", \"class A {}\")",
        "        .doTest();",
        "",
        "    BugCheckerRefactoringTestHelper.newInstance(bugChecker, getClass())",
        "        .addInputLines(\"A.java\", \"class A {}\")",
        "        .addOutputLines(\"A.java\", \"class A { /* This is a change. */ }\")",
        "        .doTest();",
        "  }",
        "}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void scannerSupplierInstance(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import com.google.errorprone.scanner.ScannerSupplier;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    CompilationTestHelper.newInstance(",
        "            ScannerSupplier.fromBugCheckerClasses(TestChecker.class), getClass())",
        "        .addSourceLines(\"A.java\", \"// BUG: Diagnostic contains:\", \"class A {}\")",
        "        .doTest();",
        "",
        "    BugCheckerRefactoringTestHelper.newInstance(",
        "            ScannerSupplier.fromBugCheckerClasses(TestChecker.class), getClass())",
        "        .addInputLines(\"A.java\", \"class A {}\")",
        "        .addOutputLines(\"A.java\", \"class A { /* This is a change. */ }\")",
        "        .doTest();",
        "  }",
        "}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void nonCompileTimeConstantStrings(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addSourceLines(toString() + \"A.java\", \"// BUG: Diagnostic contains:\", \"class A {}\")",
        "        .addSourceLines(\"B.java\", \"// BUG: Diagnostic contains:\", \"class B {}\", toString())",
        "        .doTest();",
        "",
        "    BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addInputLines(toString() + \"A.java\", \"class A {}\")",
        "        .addOutputLines(\"A.java\", \"class A { /* This is a change. */ }\")",
        "        .addInputLines(\"B.java\", \"class B {}\", toString())",
        "        .addOutputLines(\"B.java\", \"class B { /* This is a change. */ }\")",
        "        .addInputLines(\"C.java\", \"class C {}\")",
        "        .addOutputLines(\"C.java\", \"class C { /* This is a change. */ }\", toString())",
        "        .doTest();",
        "  }",
        "}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void nonFluentTestHelperExpressions(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    CompilationTestHelper testHelper =",
        "        CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "            .addSourceLines(\"A.java\", \"class A {}\");",
        "    testHelper.doTest();",
        "",
        "    BugCheckerRefactoringTestHelper.ExpectOutput expectedOutput =",
        "        BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())",
        "            .addInputLines(\"A.java\", \"class A {}\");",
        "    expectedOutput.addOutputLines(\"A.java\", \"class A {}\").doTest();",
        "    expectedOutput.expectUnchanged().doTest();",
        "  }",
        "}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void noDiagnostics(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addSourceLines(\"A.java\", \"class A {}\")",
        "        .doTest();",
        "",
        "    BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addInputLines(\"A.java\", \"class A {}\")",
        "        .addOutputLines(\"A.java\", \"class A {}\")",
        "        .addInputLines(\"B.java\", \"class B {}\")",
        "        .expectUnchanged()",
        "        .doTest();",
        "  }",
        "}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  // XXX: Rename/drop/reorganize the tests below.
  // XXX: Make sure to test that different checker names yield different output files.
  // XXX: Make sure that the resource file names match the associated test names.

  @Test
  void differentBugPatternAsLocalVariable(@TempDir Path outputDirectory) throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "import com.google.errorprone.CompilationTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class TestCheckerTest {",
        "  private static class DifferentChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    CompilationTestHelper.newInstance(DifferentChecker.class, getClass())",
        "        .addSourceLines(\"A.java\", \"// BUG: Diagnostic contains:\", \"class A {}\")",
        "        .doTest();",
        "  }",
        "}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-test-TestCheckerTest.json",
        "bugpattern-test-documentation-as-local-variable.json");
  }

  @Test
  void bugPatternTestSingleIdentification(@TempDir Path outputDirectory) throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "import com.google.errorprone.CompilationTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addSourceLines(\"A.java\", \"// BUG: Diagnostic contains:\", \"class A {}\")",
        "        .doTest();",
        "  }",
        "}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-test-TestCheckerTest.json",
        "bugpattern-test-documentation-identification.json");
  }

  @Test
  void bugPatternTestIdentificationWithSetArgs(@TempDir Path outputDirectory) throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "import com.google.errorprone.CompilationTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "        .setArgs(\"-XepAllSuggestionsAsWarnings\")",
        "        .addSourceLines(\"A.java\", \"// BUG: Diagnostic contains:\", \"class A {}\")",
        "        .doTest();",
        "  }",
        "}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-test-TestCheckerTest.json",
        "bugpattern-test-documentation-identification.json");
  }

  @Test
  void bugPatternTestIdentificationMultipleSourceLines(@TempDir Path outputDirectory)
      throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "package pkg;",
        "",
        "import com.google.errorprone.CompilationTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addSourceLines(\"A.java\", \"// BUG: Diagnostic contains:\", \"class A {}\")",
        "        .addSourceLines(\"B.java\", \"// BUG: Diagnostic contains:\", \"class B {}\")",
        "        .doTest();",
        "  }",
        "}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-test-TestCheckerTest.json",
        "bugpattern-test-documentation-identification-two-sources.json");
  }

  @Test
  void bugPatternTestSingleReplacementWithChange(@TempDir Path outputDirectory) throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addInputLines(\"A.java\", \"class A {}\")",
        "        .addOutputLines(\"A.java\", \"class A { /* This is a change. */ }\")",
        "        .doTest(TestMode.TEXT_MATCH);",
        "  }",
        "}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-test-TestCheckerTest.json",
        "bugpattern-test-documentation-replacement.json");
  }

  @Test
  void bugPatternTestMultipleReplacementSources(@TempDir Path outputDirectory) throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addInputLines(\"A.java\", \"class A {}\")",
        "        .addOutputLines(\"A.java\", \"class A { /* This is a change. */ }\")",
        "        .addInputLines(\"B.java\", \"class B {}\")",
        "        .addOutputLines(\"B.java\", \"class B { /* This is a change. */ }\")",
        "        .doTest(TestMode.TEXT_MATCH);",
        "  }",
        "}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-test-TestCheckerTest.json",
        "bugpattern-test-documentation-replacement-two-sources.json");
  }

  @Test
  void bugPatternTestIdentificationAndReplacement(@TempDir Path outputDirectory)
      throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addSourceLines(\"A.java\", \"// BUG: Diagnostic contains:\", \"class A {}\")",
        "        .doTest();",
        "  }",
        "",
        "  void replacement() {",
        "    BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addInputLines(\"A.java\", \"class A {}\")",
        "        .addOutputLines(\"A.java\", \"class A { /* This is a change. */ }\")",
        "        .doTest(TestMode.TEXT_MATCH);",
        "  }",
        "}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-test-TestCheckerTest.json",
        "bugpattern-test-documentation-identification-and-replacement.json");
  }

  @Test
  void bugPatternTestMultipleIdentificationAndReplacement(@TempDir Path outputDirectory)
      throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "package pkg;",
        "",
        "import static com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers.SECOND;",
        "",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addSourceLines(\"A.java\", \"// BUG: Diagnostic contains:\", \"class A {}\")",
        "        .addSourceLines(\"B.java\", \"// BUG: Diagnostic contains:\", \"class B {}\")",
        "        .doTest();",
        "  }",
        "",
        "  void m2() {",
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addSourceLines(\"C.java\", \"// BUG: Diagnostic contains:\", \"class C {}\")",
        "        .doTest();",
        "  }",
        "",
        "  void replacementFirstSuggestedFix() {",
        "    BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addInputLines(\"A.java\", \"class A {}\")",
        "        .addOutputLines(\"A.java\", \"class A { /* This is a change. */ }\")",
        "        .doTest(TestMode.TEXT_MATCH);",
        "  }",
        "",
        "  void replacementSecondSuggestedFix() {",
        "    BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())",
        "        .setFixChooser(SECOND)",
        "        .addInputLines(\"B.java\", \"class B {}\")",
        "        .addOutputLines(\"B.java\", \"class B { /* This is a change. */ }\")",
        "        .doTest(TestMode.TEXT_MATCH);",
        "  }",
        "}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-test-TestCheckerTest.json",
        "bugpattern-test-documentation-multiple-identification-and-replacement.json");
  }

  private static void verifyFileMatchesResource(
      Path outputDirectory, String fileName, String resourceName) throws IOException {
    assertThat(outputDirectory.resolve(fileName))
        .content(UTF_8)
        .isEqualToIgnoringWhitespace(getResource(resourceName));
  }

  // XXX: Once we support only JDK 15+, drop this method in favour of including the resources as
  // text blocks in this class. (This also requires renaming the `verifyFileMatchesResource`
  // method.)
  private static String getResource(String resourceName) throws IOException {
    return Resources.toString(
        Resources.getResource(BugPatternTestExtractorTest.class, resourceName), UTF_8);
  }
}
