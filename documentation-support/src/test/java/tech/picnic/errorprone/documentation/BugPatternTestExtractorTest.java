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
        """
        import com.google.errorprone.bugpatterns.BugChecker;

        public final class TestCheckerWithoutAnnotation extends BugChecker {}
        """);

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void noDoTestInvocation(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        """
        import com.google.errorprone.BugCheckerRefactoringTestHelper;
        import com.google.errorprone.CompilationTestHelper;
        import com.google.errorprone.bugpatterns.BugChecker;

        final class TestCheckerTest {
          private static class TestChecker extends BugChecker {}

          void m() {
            CompilationTestHelper.newInstance(TestChecker.class, getClass())
                .addSourceLines("A.java", "// BUG: Diagnostic contains:", "class A {}");

            BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())
                .addInputLines("A.java", "class A {}")
                .addOutputLines("A.java", "class A { /* This is a change. */ }");
          }
        }
        """);

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void nullBugCheckerInstance(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        """
        import com.google.errorprone.BugCheckerRefactoringTestHelper;
        import com.google.errorprone.CompilationTestHelper;
        import com.google.errorprone.bugpatterns.BugChecker;

        final class TestCheckerTest {
          void m() {
            CompilationTestHelper.newInstance((Class<BugChecker>) null, getClass())
                .addSourceLines("A.java", "// BUG: Diagnostic contains:", "class A {}")
                .doTest();

            BugCheckerRefactoringTestHelper.newInstance((Class<BugChecker>) null, getClass())
                .addInputLines("A.java", "class A {}")
                .addOutputLines("A.java", "class A { /* This is a change. */ }")
                .doTest();
          }
        }
        """);

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void rawBugCheckerInstance(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        """
        import com.google.errorprone.BugCheckerRefactoringTestHelper;
        import com.google.errorprone.CompilationTestHelper;
        import com.google.errorprone.bugpatterns.BugChecker;

        final class TestCheckerTest {
          private static class TestChecker extends BugChecker {}

          @SuppressWarnings("unchecked")
          void m() {
            @SuppressWarnings("rawtypes")
            Class bugChecker = TestChecker.class;

            CompilationTestHelper.newInstance(bugChecker, getClass())
                .addSourceLines("A.java", "// BUG: Diagnostic contains:", "class A {}")
                .doTest();

            BugCheckerRefactoringTestHelper.newInstance(bugChecker, getClass())
                .addInputLines("A.java", "class A {}")
                .addOutputLines("A.java", "class A { /* This is a change. */ }")
                .doTest();
          }
        }
        """);

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void scannerSupplierInstance(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        """
        import com.google.errorprone.BugCheckerRefactoringTestHelper;
        import com.google.errorprone.CompilationTestHelper;
        import com.google.errorprone.bugpatterns.BugChecker;
        import com.google.errorprone.scanner.ScannerSupplier;

        final class TestCheckerTest {
          private static class TestChecker extends BugChecker {}

          void m() {
            CompilationTestHelper.newInstance(
                    ScannerSupplier.fromBugCheckerClasses(TestChecker.class), getClass())
                .addSourceLines("A.java", "// BUG: Diagnostic contains:", "class A {}")
                .doTest();

            BugCheckerRefactoringTestHelper.newInstance(
                    ScannerSupplier.fromBugCheckerClasses(TestChecker.class), getClass())
                .addInputLines("A.java", "class A {}")
                .addOutputLines("A.java", "class A { /* This is a change. */ }")
                .doTest();
          }
        }
        """);

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void nonCompileTimeConstantStrings(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        """
        import com.google.errorprone.BugCheckerRefactoringTestHelper;
        import com.google.errorprone.CompilationTestHelper;
        import com.google.errorprone.bugpatterns.BugChecker;

        final class TestCheckerTest {
          private static class TestChecker extends BugChecker {}

          void m() {
            CompilationTestHelper.newInstance(TestChecker.class, getClass())
                .addSourceLines(toString() + "A.java", "// BUG: Diagnostic contains:", "class A {}")
                .addSourceLines("B.java", "// BUG: Diagnostic contains:", "class B {}", toString())
                .doTest();

            BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())
                .addInputLines(toString() + "A.java", "class A {}")
                .addOutputLines("A.java", "class A { /* This is a change. */ }")
                .addInputLines("B.java", "class B {}", toString())
                .addOutputLines("B.java", "class B { /* This is a change. */ }")
                .addInputLines("C.java", "class C {}")
                .addOutputLines("C.java", "class C { /* This is a change. */ }", toString())
                .doTest();
          }
        }
        """);

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void nonFluentTestHelperExpressions(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        """
        import com.google.errorprone.BugCheckerRefactoringTestHelper;
        import com.google.errorprone.CompilationTestHelper;
        import com.google.errorprone.bugpatterns.BugChecker;

        final class TestCheckerTest {
          private static class TestChecker extends BugChecker {}

          void m() {
            CompilationTestHelper testHelper =
                CompilationTestHelper.newInstance(TestChecker.class, getClass())
                    .addSourceLines("A.java", "class A {}");
            testHelper.doTest();

            BugCheckerRefactoringTestHelper.ExpectOutput expectedOutput =
                BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())
                    .addInputLines("A.java", "class A {}");
            expectedOutput.addOutputLines("A.java", "class A {}").doTest();
            expectedOutput.expectUnchanged().doTest();
          }
        }
        """);

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void noSource(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        """
        import com.google.errorprone.BugCheckerRefactoringTestHelper;
        import com.google.errorprone.CompilationTestHelper;
        import com.google.errorprone.bugpatterns.BugChecker;

        final class TestCheckerTest {
          private static class TestChecker extends BugChecker {}

          void m() {
            CompilationTestHelper.newInstance(TestChecker.class, getClass()).doTest();

            BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass()).doTest();
          }
        }
        """);

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void noDiagnostics(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        """
        import com.google.errorprone.BugCheckerRefactoringTestHelper;
        import com.google.errorprone.CompilationTestHelper;
        import com.google.errorprone.bugpatterns.BugChecker;

        final class TestCheckerTest {
          private static class TestChecker extends BugChecker {}

          void m() {
            CompilationTestHelper.newInstance(TestChecker.class, getClass())
                .addSourceLines("A.java", "class A {}")
                .doTest();

            BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())
                .addInputLines("A.java", "class A {}")
                .addOutputLines("A.java", "class A {}")
                .addInputLines("B.java", "class B {}")
                .expectUnchanged()
                .doTest();
          }
        }
        """);

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void singleFileCompilationTestHelper(@TempDir Path outputDirectory) throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "SingleFileCompilationTestHelperTest.java",
        """
        import com.google.errorprone.CompilationTestHelper;
        import com.google.errorprone.bugpatterns.BugChecker;

        final class SingleFileCompilationTestHelperTest {
          private static class TestChecker extends BugChecker {}

          void m() {
            CompilationTestHelper.newInstance(TestChecker.class, getClass())
                .addSourceLines("A.java", "// BUG: Diagnostic contains:", "class A {}")
                .doTest();
          }
        }
        """);

    verifyGeneratedFileContent(outputDirectory, "SingleFileCompilationTestHelperTest");
  }

  @Test
  void singleFileCompilationTestHelperWithSetArgs(@TempDir Path outputDirectory)
      throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "SingleFileCompilationTestHelperWithSetArgsTest.java",
        """
        import com.google.errorprone.CompilationTestHelper;
        import com.google.errorprone.bugpatterns.BugChecker;

        final class SingleFileCompilationTestHelperWithSetArgsTest {
          private static class TestChecker extends BugChecker {}

          void m() {
            CompilationTestHelper.newInstance(TestChecker.class, getClass())
                .setArgs("-XepAllSuggestionsAsWarnings")
                .addSourceLines("A.java", "// BUG: Diagnostic contains:", "class A {}")
                .doTest();
          }
        }
        """);

    verifyGeneratedFileContent(outputDirectory, "SingleFileCompilationTestHelperWithSetArgsTest");
  }

  @Test
  void multiFileCompilationTestHelper(@TempDir Path outputDirectory) throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "MultiFileCompilationTestHelperTest.java",
        """
        import com.google.errorprone.CompilationTestHelper;
        import com.google.errorprone.bugpatterns.BugChecker;

        final class MultiFileCompilationTestHelperTest {
          private static class TestChecker extends BugChecker {}

          void m() {
            CompilationTestHelper.newInstance(TestChecker.class, getClass())
                .addSourceLines("A.java", "// BUG: Diagnostic contains:", "class A {}")
                .addSourceLines("B.java", "// BUG: Diagnostic contains:", "class B {}")
                .doTest();
          }
        }
        """);

    verifyGeneratedFileContent(outputDirectory, "MultiFileCompilationTestHelperTest");
  }

  @Test
  void singleFileBugCheckerRefactoringTestHelper(@TempDir Path outputDirectory) throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "SingleFileBugCheckerRefactoringTestHelperTest.java",
        """
        import com.google.errorprone.BugCheckerRefactoringTestHelper;
        import com.google.errorprone.bugpatterns.BugChecker;

        final class SingleFileBugCheckerRefactoringTestHelperTest {
          private static class TestChecker extends BugChecker {}

          void m() {
            BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())
                .addInputLines("A.java", "class A {}")
                .addOutputLines("A.java", "class A { /* This is a change. */ }")
                .doTest();
          }
        }
        """);

    verifyGeneratedFileContent(outputDirectory, "SingleFileBugCheckerRefactoringTestHelperTest");
  }

  @Test
  void singleFileBugCheckerRefactoringTestHelperWithSetArgsFixChooserAndCustomTestMode(
      @TempDir Path outputDirectory) throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "SingleFileBugCheckerRefactoringTestHelperWithSetArgsFixChooserAndCustomTestModeTest.java",
        """
        import com.google.errorprone.BugCheckerRefactoringTestHelper;
        import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;
        import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
        import com.google.errorprone.bugpatterns.BugChecker;

        final class SingleFileBugCheckerRefactoringTestHelperWithSetArgsFixChooserAndCustomTestModeTest {
          private static class TestChecker extends BugChecker {}

          void m() {
            BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())
                .setArgs("-XepAllSuggestionsAsWarnings")
                .setFixChooser(FixChoosers.SECOND)
                .addInputLines("A.java", "class A {}")
                .addOutputLines("A.java", "class A { /* This is a change. */ }")
                .doTest(TestMode.TEXT_MATCH);
          }
        }
        """);

    verifyGeneratedFileContent(
        outputDirectory,
        "SingleFileBugCheckerRefactoringTestHelperWithSetArgsFixChooserAndCustomTestModeTest");
  }

  @Test
  void multiFileBugCheckerRefactoringTestHelper(@TempDir Path outputDirectory) throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "MultiFileBugCheckerRefactoringTestHelperTest.java",
        """
        import com.google.errorprone.BugCheckerRefactoringTestHelper;
        import com.google.errorprone.bugpatterns.BugChecker;

        final class MultiFileBugCheckerRefactoringTestHelperTest {
          private static class TestChecker extends BugChecker {}

          void m() {
            BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())
                .addInputLines("A.java", "class A {}")
                .addOutputLines("A.java", "class A { /* This is a change. */ }")
                .addInputLines("B.java", "class B {}")
                .addOutputLines("B.java", "class B { /* This is a change. */ }")
                .doTest();
          }
        }
        """);

    verifyGeneratedFileContent(outputDirectory, "MultiFileBugCheckerRefactoringTestHelperTest");
  }

  @Test
  void compilationAndBugCheckerRefactoringTestHelpers(@TempDir Path outputDirectory)
      throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "CompilationAndBugCheckerRefactoringTestHelpersTest.java",
        """
        import com.google.errorprone.BugCheckerRefactoringTestHelper;
        import com.google.errorprone.CompilationTestHelper;
        import com.google.errorprone.bugpatterns.BugChecker;

        final class CompilationAndBugCheckerRefactoringTestHelpersTest {
          private static class TestChecker extends BugChecker {}

          void m() {
            CompilationTestHelper.newInstance(TestChecker.class, getClass())
                .addSourceLines("A.java", "// BUG: Diagnostic contains:", "class A {}")
                .doTest();

            BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())
                .addInputLines("A.java", "class A {}")
                .addOutputLines("A.java", "class A { /* This is a change. */ }")
                .doTest();
          }
        }
        """);

    verifyGeneratedFileContent(
        outputDirectory, "CompilationAndBugCheckerRefactoringTestHelpersTest");
  }

  @Test
  void compilationAndBugCheckerRefactoringTestHelpersWithCustomCheckerPackageAndNames(
      @TempDir Path outputDirectory) throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "CompilationAndBugCheckerRefactoringTestHelpersWithCustomCheckerPackageAndNamesTest.java",
        """
        package pkg;

        import com.google.errorprone.BugCheckerRefactoringTestHelper;
        import com.google.errorprone.CompilationTestHelper;
        import com.google.errorprone.bugpatterns.BugChecker;

        final class CompilationAndBugCheckerRefactoringTestHelpersWithCustomCheckerPackageAndNamesTest {
          private static class CustomTestChecker extends BugChecker {}

          private static class CustomTestChecker2 extends BugChecker {}

          void m() {
            CompilationTestHelper.newInstance(CustomTestChecker.class, getClass())
                .addSourceLines("A.java", "// BUG: Diagnostic contains:", "class A {}")
                .doTest();

            BugCheckerRefactoringTestHelper.newInstance(CustomTestChecker2.class, getClass())
                .addInputLines("A.java", "class A {}")
                .addOutputLines("A.java", "class A { /* This is a change. */ }")
                .doTest();
          }
        }
        """);

    verifyGeneratedFileContent(
        outputDirectory,
        "CompilationAndBugCheckerRefactoringTestHelpersWithCustomCheckerPackageAndNamesTest");
  }

  private static void verifyGeneratedFileContent(Path outputDirectory, String testClass)
      throws IOException {
    String resourceName = String.format("bugpattern-test-%s.json", testClass);
    assertThat(outputDirectory.resolve(resourceName))
        .content(UTF_8)
        .isEqualToIgnoringWhitespace(
            getResource(
                String.join("-", BugPatternTestExtractorTest.class.getSimpleName(), resourceName)));
  }

  // XXX: Once we support only JDK 15+, drop this method in favour of including the resources as
  // text blocks in this class.
  private static String getResource(String resourceName) throws IOException {
    return Resources.toString(
        Resources.getResource(BugPatternTestExtractorTest.class, resourceName), UTF_8);
  }
}
