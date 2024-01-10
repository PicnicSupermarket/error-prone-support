package tech.picnic.errorprone.documentation;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tech.picnic.errorprone.documentation.BugPatternTestExtractor.IdentificationTestEntry;
import tech.picnic.errorprone.documentation.BugPatternTestExtractor.ReplacementTestEntry;
import tech.picnic.errorprone.documentation.BugPatternTestExtractor.TestCase;
import tech.picnic.errorprone.documentation.BugPatternTestExtractor.TestCases;

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
  void noSource(@TempDir Path outputDirectory) {
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
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass()).doTest();",
        "",
        "    BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass()).doTest();",
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

  @Test
  void singleFileCompilationTestHelper(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "SingleFileCompilationTestHelperTest.java",
        "import com.google.errorprone.CompilationTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class SingleFileCompilationTestHelperTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addSourceLines(\"A.java\", \"// BUG: Diagnostic contains:\", \"class A {}\")",
        "        .doTest();",
        "  }",
        "}");

    verifyGeneratedFileContent(
        outputDirectory,
        "SingleFileCompilationTestHelperTest",
        TestCases.create(
            URI.create("file:///SingleFileCompilationTestHelperTest.java"),
            "SingleFileCompilationTestHelperTest",
            ImmutableList.of(
                TestCase.create(
                    "SingleFileCompilationTestHelperTest.TestChecker",
                    ImmutableList.of(
                        IdentificationTestEntry.create(
                            "A.java", "// BUG: Diagnostic contains:\nclass A {}\n"))))));
  }

  @Test
  void singleFileCompilationTestHelperWithSetArgs(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "SingleFileCompilationTestHelperWithSetArgsTest.java",
        "import com.google.errorprone.CompilationTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class SingleFileCompilationTestHelperWithSetArgsTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "        .setArgs(\"-XepAllSuggestionsAsWarnings\")",
        "        .addSourceLines(\"A.java\", \"// BUG: Diagnostic contains:\", \"class A {}\")",
        "        .doTest();",
        "  }",
        "}");

    verifyGeneratedFileContent(
        outputDirectory,
        "SingleFileCompilationTestHelperWithSetArgsTest",
        TestCases.create(
            URI.create("file:///SingleFileCompilationTestHelperWithSetArgsTest.java"),
            "SingleFileCompilationTestHelperWithSetArgsTest",
            ImmutableList.of(
                TestCase.create(
                    "SingleFileCompilationTestHelperWithSetArgsTest.TestChecker",
                    ImmutableList.of(
                        IdentificationTestEntry.create(
                            "A.java", "// BUG: Diagnostic contains:\nclass A {}\n"))))));
  }

  @Test
  void multiFileCompilationTestHelper(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "MultiFileCompilationTestHelperTest.java",
        "import com.google.errorprone.CompilationTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class MultiFileCompilationTestHelperTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addSourceLines(\"A.java\", \"// BUG: Diagnostic contains:\", \"class A {}\")",
        "        .addSourceLines(\"B.java\", \"// BUG: Diagnostic contains:\", \"class B {}\")",
        "        .doTest();",
        "  }",
        "}");

    verifyGeneratedFileContent(
        outputDirectory,
        "MultiFileCompilationTestHelperTest",
        TestCases.create(
            URI.create("file:///MultiFileCompilationTestHelperTest.java"),
            "MultiFileCompilationTestHelperTest",
            ImmutableList.of(
                TestCase.create(
                    "MultiFileCompilationTestHelperTest.TestChecker",
                    ImmutableList.of(
                        IdentificationTestEntry.create(
                            "A.java", "// BUG: Diagnostic contains:\nclass A {}\n"),
                        IdentificationTestEntry.create(
                            "B.java", "// BUG: Diagnostic contains:\nclass B {}\n"))))));
  }

  @Test
  void singleFileBugCheckerRefactoringTestHelper(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "SingleFileBugCheckerRefactoringTestHelperTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class SingleFileBugCheckerRefactoringTestHelperTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addInputLines(\"A.java\", \"class A {}\")",
        "        .addOutputLines(\"A.java\", \"class A { /* This is a change. */ }\")",
        "        .doTest();",
        "  }",
        "}");

    verifyGeneratedFileContent(
        outputDirectory,
        "SingleFileBugCheckerRefactoringTestHelperTest",
        TestCases.create(
            URI.create("file:///SingleFileBugCheckerRefactoringTestHelperTest.java"),
            "SingleFileBugCheckerRefactoringTestHelperTest",
            ImmutableList.of(
                TestCase.create(
                    "SingleFileBugCheckerRefactoringTestHelperTest.TestChecker",
                    ImmutableList.of(
                        ReplacementTestEntry.create(
                            "A.java", "class A {}\n", "class A { /* This is a change. */ }\n"))))));
  }

  @Test
  void singleFileBugCheckerRefactoringTestHelperWithSetArgsFixChooserAndCustomTestMode(
      @TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "SingleFileBugCheckerRefactoringTestHelperWithSetArgsFixChooserAndCustomTestModeTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class SingleFileBugCheckerRefactoringTestHelperWithSetArgsFixChooserAndCustomTestModeTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())",
        "        .setArgs(\"-XepAllSuggestionsAsWarnings\")",
        "        .setFixChooser(FixChoosers.SECOND)",
        "        .addInputLines(\"A.java\", \"class A {}\")",
        "        .addOutputLines(\"A.java\", \"class A { /* This is a change. */ }\")",
        "        .doTest(TestMode.TEXT_MATCH);",
        "  }",
        "}");

    verifyGeneratedFileContent(
        outputDirectory,
        "SingleFileBugCheckerRefactoringTestHelperWithSetArgsFixChooserAndCustomTestModeTest",
        TestCases.create(
            URI.create(
                "file:///SingleFileBugCheckerRefactoringTestHelperWithSetArgsFixChooserAndCustomTestModeTest.java"),
            "SingleFileBugCheckerRefactoringTestHelperWithSetArgsFixChooserAndCustomTestModeTest",
            ImmutableList.of(
                TestCase.create(
                    "SingleFileBugCheckerRefactoringTestHelperWithSetArgsFixChooserAndCustomTestModeTest.TestChecker",
                    ImmutableList.of(
                        ReplacementTestEntry.create(
                            "A.java", "class A {}\n", "class A { /* This is a change. */ }\n"))))));
  }

  @Test
  void multiFileBugCheckerRefactoringTestHelper(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "MultiFileBugCheckerRefactoringTestHelperTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class MultiFileBugCheckerRefactoringTestHelperTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addInputLines(\"A.java\", \"class A {}\")",
        "        .addOutputLines(\"A.java\", \"class A { /* This is a change. */ }\")",
        "        .addInputLines(\"B.java\", \"class B {}\")",
        "        .addOutputLines(\"B.java\", \"class B { /* This is a change. */ }\")",
        "        .doTest();",
        "  }",
        "}");

    verifyGeneratedFileContent(
        outputDirectory,
        "MultiFileBugCheckerRefactoringTestHelperTest",
        TestCases.create(
            URI.create("file:///MultiFileBugCheckerRefactoringTestHelperTest.java"),
            "MultiFileBugCheckerRefactoringTestHelperTest",
            ImmutableList.of(
                TestCase.create(
                    "MultiFileBugCheckerRefactoringTestHelperTest.TestChecker",
                    ImmutableList.of(
                        ReplacementTestEntry.create(
                            "A.java", "class A {}\n", "class A { /* This is a change. */ }\n"),
                        ReplacementTestEntry.create(
                            "B.java", "class B {}\n", "class B { /* This is a change. */ }\n"))))));
  }

  @Test
  void compilationAndBugCheckerRefactoringTestHelpers(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "CompilationAndBugCheckerRefactoringTestHelpersTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class CompilationAndBugCheckerRefactoringTestHelpersTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  void m() {",
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addSourceLines(\"A.java\", \"// BUG: Diagnostic contains:\", \"class A {}\")",
        "        .doTest();",
        "",
        "    BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addInputLines(\"A.java\", \"class A {}\")",
        "        .addOutputLines(\"A.java\", \"class A { /* This is a change. */ }\")",
        "        .doTest();",
        "  }",
        "}");

    verifyGeneratedFileContent(
        outputDirectory,
        "CompilationAndBugCheckerRefactoringTestHelpersTest",
        TestCases.create(
            URI.create("file:///CompilationAndBugCheckerRefactoringTestHelpersTest.java"),
            "CompilationAndBugCheckerRefactoringTestHelpersTest",
            ImmutableList.of(
                TestCase.create(
                    "CompilationAndBugCheckerRefactoringTestHelpersTest.TestChecker",
                    ImmutableList.of(
                        IdentificationTestEntry.create(
                            "A.java", "// BUG: Diagnostic contains:\nclass A {}\n"))),
                TestCase.create(
                    "CompilationAndBugCheckerRefactoringTestHelpersTest.TestChecker",
                    ImmutableList.of(
                        ReplacementTestEntry.create(
                            "A.java", "class A {}\n", "class A { /* This is a change. */ }\n"))))));
  }

  @Test
  void compilationAndBugCheckerRefactoringTestHelpersWithCustomCheckerPackageAndNames(
      @TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "CompilationAndBugCheckerRefactoringTestHelpersWithCustomCheckerPackageAndNamesTest.java",
        "package pkg;",
        "",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "final class CompilationAndBugCheckerRefactoringTestHelpersWithCustomCheckerPackageAndNamesTest {",
        "  private static class CustomTestChecker extends BugChecker {}",
        "",
        "  private static class CustomTestChecker2 extends BugChecker {}",
        "",
        "  void m() {",
        "    CompilationTestHelper.newInstance(CustomTestChecker.class, getClass())",
        "        .addSourceLines(\"A.java\", \"// BUG: Diagnostic contains:\", \"class A {}\")",
        "        .doTest();",
        "",
        "    BugCheckerRefactoringTestHelper.newInstance(CustomTestChecker2.class, getClass())",
        "        .addInputLines(\"A.java\", \"class A {}\")",
        "        .addOutputLines(\"A.java\", \"class A { /* This is a change. */ }\")",
        "        .doTest();",
        "  }",
        "}");

    verifyGeneratedFileContent(
        outputDirectory,
        "CompilationAndBugCheckerRefactoringTestHelpersWithCustomCheckerPackageAndNamesTest",
        TestCases.create(
            URI.create(
                "file:///CompilationAndBugCheckerRefactoringTestHelpersWithCustomCheckerPackageAndNamesTest.java"),
            "pkg.CompilationAndBugCheckerRefactoringTestHelpersWithCustomCheckerPackageAndNamesTest",
            ImmutableList.of(
                TestCase.create(
                    "pkg.CompilationAndBugCheckerRefactoringTestHelpersWithCustomCheckerPackageAndNamesTest.CustomTestChecker",
                    ImmutableList.of(
                        IdentificationTestEntry.create(
                            "A.java", "// BUG: Diagnostic contains:\nclass A {}\n"))),
                TestCase.create(
                    "pkg.CompilationAndBugCheckerRefactoringTestHelpersWithCustomCheckerPackageAndNamesTest.CustomTestChecker2",
                    ImmutableList.of(
                        ReplacementTestEntry.create(
                            "A.java", "class A {}\n", "class A { /* This is a change. */ }\n"))))));
  }

  private static void verifyGeneratedFileContent(
      Path outputDirectory, String testClass, TestCases expected) {
    assertThat(outputDirectory.resolve(String.format("bugpattern-test-%s.json", testClass)))
        .exists()
        .returns(expected, path -> Json.read(path, TestCases.class));
  }
}
