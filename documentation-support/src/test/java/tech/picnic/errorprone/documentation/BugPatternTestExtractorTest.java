package tech.picnic.errorprone.documentation;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class BugPatternTestExtractorTest {
  @Test
  void noBugPatternTest(@TempDir Path outputDirectory) {
    JavacTaskCompilation.compile(
        outputDirectory,
        "TestCheckerWithoutAnnotation.java",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "public final class TestCheckerWithoutAnnotation extends BugChecker {}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void minimalBugPatternTest(@TempDir Path outputDirectory) throws IOException {
    JavacTaskCompilation.compile(
        outputDirectory,
        "IdentityConversionTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import com.google.errorprone.CompilationTestHelper;",
        "",
        "final class IdentityConversionTest {",
        "  private static class IdentityConversion extends BugChecker {}",
        "",
        "  CompilationTestHelper compilationTestHelper = CompilationTestHelper.newInstance(IdentityConversion.class, getClass());",
        "}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-test-IdentityConversionTest.json",
        "bugpattern-test-documentation-minimal.json");
  }

  @Test
  void differentBugPatternTest(@TempDir Path outputDirectory) {
    JavacTaskCompilation.compile(
        outputDirectory,
        "IdentityConversionTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import com.google.errorprone.CompilationTestHelper;",
        "",
        "final class IdentityConversionTest {",
        "  private static class DifferentBugPattern extends BugChecker {}",
        "",
        "  CompilationTestHelper compilationTestHelper = CompilationTestHelper.newInstance(DifferentBugPattern.class, getClass());",
        "}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void differentBugPatternWithTest(@TempDir Path outputDirectory) {
    JavacTaskCompilation.compile(
        outputDirectory,
        "IdentityConversionTest.java",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import org.junit.jupiter.api.Test;",
        "",
        "final class IdentityConversionTest {",
        "  private static class TestChecker extends BugChecker {}",
        "",
        "  @Test",
        "  void identification() {",
        "    CompilationTestHelper.newInstance(TestChecker.class, getClass())",
        "        .addSourceLines(\"A.java\", \"class A {}\")",
        "        .doTest();",
        "  }",
        "}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void bugPatternTestSingleIdentification(@TempDir Path outputDirectory) throws IOException {
    JavacTaskCompilation.compile(
        outputDirectory,
        "IdentityConversionTest.java",
        "package pkg;",
        "",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import org.junit.jupiter.api.Test;",
        "",
        "final class IdentityConversionTest {",
        "  private static class IdentityConversion extends BugChecker {}",
        "",
        "  @Test",
        "  void identification() {",
        "    CompilationTestHelper.newInstance(IdentityConversion.class, getClass())",
        "        .addSourceLines(",
        "            \"A.java\",",
        "            \"public final class A {\",",
        "            \"  public void m() {\",",
        "            \"    // BUG: Diagnostic contains:\",",
        "            \"    Boolean b = Boolean.valueOf(Boolean.FALSE);\",",
        "            \"  }\",",
        "            \"}\")",
        "        .doTest();",
        "  }",
        "}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-test-IdentityConversionTest.json",
        "bugpattern-test-documentation-identification.json");
  }

  @Test
  void bugPatternTestIdentificationMultipleSourceLines(@TempDir Path outputDirectory)
      throws IOException {
    JavacTaskCompilation.compile(
        outputDirectory,
        "IdentityConversionTest.java",
        "package pkg;",
        "",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import org.junit.jupiter.api.Test;",
        "",
        "final class IdentityConversionTest {",
        "  private static class IdentityConversion extends BugChecker {}",
        "",
        "  @Test",
        "  void identification() {",
        "    CompilationTestHelper.newInstance(IdentityConversion.class, getClass())",
        "        .addSourceLines(",
        "            \"A.java\",",
        "            \"public final class A {}\")",
        "        .addSourceLines(",
        "            \"B.java\",",
        "            \"public final class B {}\")",
        "        .doTest();",
        "  }",
        "}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-test-IdentityConversionTest.json",
        "bugpattern-test-documentation-identification-two-sources.json");
  }

  @Test
  void bugPatternTestSingleReplacement(@TempDir Path outputDirectory) throws IOException {
    JavacTaskCompilation.compile(
        outputDirectory,
        "IdentityConversionTest.java",
        "package pkg;",
        "",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import org.junit.jupiter.api.Test;",
        "",
        "final class IdentityConversionTest {",
        "  private static class IdentityConversion extends BugChecker {}",
        "",
        "  @Test",
        "  void replacementFirstSuggestedFix() {",
        "    BugCheckerRefactoringTestHelper.newInstance(IdentityConversion.class, getClass())",
        "            .setFixChooser(FixChoosers.FIRST)",
        "            .addInputLines(",
        "                    \"A.java\",",
        "                    \"import com.google.common.collect.ImmutableSet;\",",
        "                    \"\",",
        "                    \"public final class A {\",",
        "                    \"  public void m() {\",",
        "                    \"    ImmutableSet<Object> set = ImmutableSet.copyOf(ImmutableSet.of());\",",
        "                    \"  }\",",
        "                    \"}\")",
        "            .addOutputLines(",
        "                    \"A.java\",",
        "                    \"import com.google.common.collect.ImmutableSet;\",",
        "                    \"\",",
        "                    \"public final class A {\",",
        "                    \"  public void m() {\",",
        "                    \"    ImmutableSet<Object> set = ImmutableSet.of();\",",
        "                    \"  }\",",
        "                    \"}\")",
        "            .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);",
        "  }",
        "}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-test-IdentityConversionTest.json",
        "bugpattern-test-documentation-replacement.json");
  }

  @Test
  void bugPatternTestMultipleReplacements(@TempDir Path outputDirectory) throws IOException {
    JavacTaskCompilation.compile(
        outputDirectory,
        "IdentityConversionTest.java",
        "package pkg;",
        "",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import org.junit.jupiter.api.Test;",
        "",
        "final class IdentityConversionTest {",
        "  private static class IdentityConversion extends BugChecker {}",
        "",
        "  @Test",
        "  void replacementFirstSuggestedFix() {",
        "    BugCheckerRefactoringTestHelper.newInstance(IdentityConversion.class, getClass())",
        "            .setFixChooser(FixChoosers.FIRST)",
        "            .addInputLines(",
        "                \"A.java\",",
        "                \"public final class A {}\")",
        "            .addOutputLines(",
        "                    \"A.java\",",
        "                    \"public final class A {}\")",
        "            .addInputLines(",
        "                    \"B.java\",",
        "                    \"public final class B {}\")",
        "            .addOutputLines(",
        "                    \"B.java\",",
        "                    \"public final class B {}\")",
        "            .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);",
        "  }",
        "}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-test-IdentityConversionTest.json",
        "bugpattern-test-documentation-replacement-two-sources.json");
  }

  @Test
  void bugPatternReplacementExpectUnchanged(@TempDir Path outputDirectory) throws IOException {
    JavacTaskCompilation.compile(
        outputDirectory,
        "IdentityConversionTest.java",
        "package pkg;",
        "",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import org.junit.jupiter.api.Test;",
        "",
        "final class IdentityConversionTest {",
        "  private static class IdentityConversion extends BugChecker {}",
        "",
        "  @Test",
        "  void replacementFirstSuggestedFix() {",
        "    BugCheckerRefactoringTestHelper.newInstance(IdentityConversion.class, getClass())",
        "            .addInputLines(",
        "                    \"A.java\",",
        "                    \"public final class A {}\")",
        "            .expectUnchanged()",
        "            .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);",
        "  }",
        "}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-test-IdentityConversionTest.json",
        "bugpattern-test-documentation-replacement-expect-unchanged.json");
  }

  @Test
  void bugPatternTestIdentificationAndReplacement(@TempDir Path outputDirectory)
      throws IOException {
    JavacTaskCompilation.compile(
        outputDirectory,
        "IdentityConversionTest.java",
        "package pkg;",
        "",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import org.junit.jupiter.api.Test;",
        "",
        "final class IdentityConversionTest {",
        "  private static class IdentityConversion extends BugChecker {}",
        "",
        "  @Test",
        "  void identification() {",
        "    CompilationTestHelper.newInstance(IdentityConversion.class, getClass())",
        "        .addSourceLines(",
        "            \"A.java\",",
        "            \"public final class A {\",",
        "            \"  public void m() {\",",
        "            \"    // BUG: Diagnostic contains:\",",
        "            \"    Boolean b = Boolean.valueOf(Boolean.FALSE);\",",
        "            \"  }\",",
        "            \"}\")",
        "        .doTest();",
        "  }",
        "",
        "  @Test",
        "  void replacementFirstSuggestedFix() {",
        "    BugCheckerRefactoringTestHelper.newInstance(IdentityConversion.class, getClass())",
        "            .setFixChooser(FixChoosers.FIRST)",
        "            .addInputLines(",
        "                    \"A.java\",",
        "                    \"import com.google.common.collect.ImmutableSet;\",",
        "                    \"\",",
        "                    \"public final class A {\",",
        "                    \"  public void m() {\",",
        "                    \"    ImmutableSet<Object> set = ImmutableSet.copyOf(ImmutableSet.of());\",",
        "                    \"  }\",",
        "                    \"}\")",
        "            .addOutputLines(",
        "                    \"A.java\",",
        "                    \"import com.google.common.collect.ImmutableSet;\",",
        "                    \"\",",
        "                    \"public final class A {\",",
        "                    \"  public void m() {\",",
        "                    \"    ImmutableSet<Object> set = ImmutableSet.of();\",",
        "                    \"  }\",",
        "                    \"}\")",
        "            .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);",
        "  }",
        "}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-test-IdentityConversionTest.json",
        "bugpattern-test-documentation-identification-and-replacement.json");
  }

  @Test
  void bugPatternTestMultipleIdentificationAndReplacement(@TempDir Path outputDirectory)
      throws IOException {
    JavacTaskCompilation.compile(
        outputDirectory,
        "IdentityConversionTest.java",
        "package pkg;",
        "",
        "import static com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers.SECOND;",
        "",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
        "import com.google.errorprone.CompilationTestHelper;",
        "import org.junit.jupiter.api.Test;",
        "",
        "final class IdentityConversionTest {",
        "  private static class IdentityConversion extends BugChecker {}",
        "",
        "  @Test",
        "  void identification() {",
        "    CompilationTestHelper.newInstance(IdentityConversion.class, getClass())",
        "        .addSourceLines(",
        "            \"A.java\",",
        "            \"public final class A {\",",
        "            \"  public void m() {\",",
        "            \"    // BUG: Diagnostic contains:\",",
        "            \"    Boolean b = Boolean.valueOf(Boolean.FALSE);\",",
        "            \"  }\",",
        "            \"}\")",
        "        .doTest();",
        "  }",
        "",
        "  @Test",
        "  void identification2() {",
        "    CompilationTestHelper.newInstance(IdentityConversion.class, getClass())",
        "        .addSourceLines(",
        "            \"B.java\",",
        "            \"public final class B {\",",
        "            \"  public void m() {\",",
        "            \"    // BUG: Diagnostic contains:\",",
        "            \"    Boolean b = Boolean.valueOf(Boolean.FALSE);\",",
        "            \"  }\",",
        "            \"}\")",
        "        .doTest();",
        "  }",
        "",
        "  @Test",
        "  void replacementFirstSuggestedFix() {",
        "    BugCheckerRefactoringTestHelper.newInstance(IdentityConversion.class, getClass())",
        "        .addInputLines(",
        "            \"A.java\",",
        "            \"import com.google.common.collect.ImmutableSet;\",",
        "            \"\",",
        "            \"public final class A {\",",
        "            \"  public void m() {\",",
        "            \"    ImmutableSet<Object> set = ImmutableSet.copyOf(ImmutableSet.of());\",",
        "            \"  }\",",
        "            \"}\")",
        "        .addOutputLines(",
        "            \"A.java\",",
        "            \"import com.google.common.collect.ImmutableSet;\",",
        "            \"\",",
        "            \"public final class A {\",",
        "            \"  public void m() {\",",
        "            \"    ImmutableSet<Object> set = ImmutableSet.of();\",",
        "            \"  }\",",
        "            \"}\")",
        "        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);",
        "  }",
        "",
        "  @Test",
        "  void replacementSecondSuggestedFix() {",
        "    BugCheckerRefactoringTestHelper.newInstance(IdentityConversion.class, getClass())",
        "            .setFixChooser(SECOND)",
        "            .addInputLines(",
        "                    \"B.java\",",
        "                    \"import com.google.common.collect.ImmutableSet;\",",
        "                    \"\",",
        "                    \"public final class B {\",",
        "                    \"  public void m() {\",",
        "                    \"    ImmutableSet<Object> set = ImmutableSet.copyOf(ImmutableSet.of());\",",
        "                    \"  }\",",
        "                    \"}\")",
        "            .addOutputLines(",
        "                    \"B.java\",",
        "                    \"import com.google.common.collect.ImmutableSet;\",",
        "                    \"\",",
        "                    \"public final class B {\",",
        "                    \"  public void m() {\",",
        "                    \"    ImmutableSet<Object> set = ImmutableSet.of();\",",
        "                    \"  }\",",
        "                    \"}\")",
        "            .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);",
        "  }",
        "}");

    verifyFileMatchesResource(
        outputDirectory,
        "bugpattern-test-IdentityConversionTest.json",
        "bugpattern-test-documentation-multiple-identification-and-replacement.json");
  }

  private static void verifyFileMatchesResource(
      Path outputDirectory, String fileName, String resourceName) throws IOException {
    assertThat(Files.readString(outputDirectory.resolve(fileName)))
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
