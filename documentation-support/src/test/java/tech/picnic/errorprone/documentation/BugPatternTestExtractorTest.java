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
  void noBugPatternTest(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerWithoutAnnotation.java",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "public final class TestCheckerWithoutAnnotation extends BugChecker {}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void bugPatternTestFileWithoutTestSuffix(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerWithWrongSuffix.java",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import com.google.errorprone.CompilationTestHelper;",
        "",
        "final class TestCheckerWithoutTestSuffix {",
        "  private static class TestCheckerWithout extends BugChecker {}",
        "",
        "  CompilationTestHelper compilationTestHelper = CompilationTestHelper.newInstance(TestCheckerWithout.class, getClass());",
        "}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void minimalBugPatternTest(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import com.google.errorprone.CompilationTestHelper;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  CompilationTestHelper compilationTestHelper = CompilationTestHelper.newInstance(TestChecker.class, getClass());",
        "}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void differentBugPatternAsClassVariableTest(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import com.google.errorprone.CompilationTestHelper;",
        "",
        "final class TestCheckerTest {",
        "  private static class DifferentChecker extends BugChecker {}",
        "",
        "  CompilationTestHelper compilationTestHelper = CompilationTestHelper.newInstance(DifferentChecker.class, getClass());",
        "}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void differentBugPatternAsLocalVariable(@TempDir Path outputDirectory) throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import org.junit.jupiter.api.Test;",
        "",
        "final class TestCheckerTest {",
        "  private static class DifferentChecker extends BugChecker {}",
        "",
        "  @Test",
        "  void identification() {",
        "    CompilationTestHelper.newInstance(DifferentChecker.class, getClass())",
        "        .addSourceLines(\"A.java\", \"class A {}\")",
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
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import org.junit.jupiter.api.Test;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  @Test",
        "  void identification() {",
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addSourceLines(\"A.java\", \"class A {}\")",
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
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import org.junit.jupiter.api.Test;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  @Test",
        "  void identification() {",
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addSourceLines(\"A.java\", \"class A {}\")",
        "        .addSourceLines(\"B.java\", \"class B {}\")",
        "        .doTest();",
        "  }",
        "}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-test-TestCheckerTest.json",
        "bugpattern-test-documentation-identification-two-sources.json");
  }

  @Test
  void bugPatternTestSingleReplacement(@TempDir Path outputDirectory) throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import org.junit.jupiter.api.Test;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  @Test",
        "  void replacement() {",
        "    BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addInputLines(\"A.java\", \"class A {}\")",
        "        .addOutputLines(\"A.java\", \"class A {}\")",
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
        "import org.junit.jupiter.api.Test;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  @Test",
        "  void replacement() {",
        "    BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addInputLines(\"A.java\", \"class A {}\")",
        "        .addOutputLines(\"A.java\", \"class A {}\")",
        "        .addInputLines(\"B.java\", \"class B {}\")",
        "        .addOutputLines(\"B.java\", \"class B {}\")",
        "        .doTest(TestMode.TEXT_MATCH);",
        "  }",
        "}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-test-TestCheckerTest.json",
        "bugpattern-test-documentation-replacement-two-sources.json");
  }

  @Test
  void bugPatternReplacementExpectUnchanged(@TempDir Path outputDirectory) throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import org.junit.jupiter.api.Test;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  @Test",
        "  void replacement() {",
        "    BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addInputLines(\"A.java\", \"class A {}\")",
        "        .expectUnchanged()",
        "        .doTest(TestMode.TEXT_MATCH);",
        "  }",
        "}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-test-TestCheckerTest.json",
        "bugpattern-test-documentation-replacement-expect-unchanged.json");
  }

  @Test
  void bugPatternTestIdentificationAndReplacement(@TempDir Path outputDirectory)
      throws IOException {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import org.junit.jupiter.api.Test;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  @Test",
        "  void identification() {",
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addSourceLines(\"A.java\", \"class A {}\")",
        "        .doTest();",
        "  }",
        "",
        "  @Test",
        "  void replacement() {",
        "    BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addInputLines(\"A.java\", \"class A {}\")",
        "        .addOutputLines(\"A.java\", \"class A {}\")",
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
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import org.junit.jupiter.api.Test;",
        "",
        "final class TestCheckerTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  @Test",
        "  void identification() {",
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addSourceLines(\"A.java\", \"class A {}\")",
        "        .addSourceLines(\"B.java\", \"class B {}\")",
        "        .doTest();",
        "  }",
        "",
        "  @Test",
        "  void identification2() {",
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addSourceLines(\"C.java\", \"class C {}\")",
        "        .doTest();",
        "  }",
        "",
        "  @Test",
        "  void replacementFirstSuggestedFix() {",
        "    BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addInputLines(\"A.java\", \"class A {}\")",
        "        .addOutputLines(\"A.java\", \"class A {}\")",
        "        .doTest(TestMode.TEXT_MATCH);",
        "  }",
        "",
        "  @Test",
        "  void replacementSecondSuggestedFix() {",
        "    BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass())",
        "        .setFixChooser(SECOND)",
        "        .addInputLines(\"B.java\", \"class B {}\")",
        "        .addOutputLines(\"B.java\", \"class B {}\")",
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
