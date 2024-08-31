package tech.picnic.errorprone.documentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tech.picnic.errorprone.documentation.RefasterRuleCollectionTestExtractor.RefasterTestCase;
import tech.picnic.errorprone.documentation.RefasterRuleCollectionTestExtractor.RefasterTestCases;

final class RefasterRuleCollectionTestExtractorTest {
  @Test
  void noRefasterRuleTest(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory, "NoRefasterRuleTest.java", "public final class NoRefasterRuleTest {}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void invalidTestClassName(@TempDir Path outputDirectory) {
    assertThatThrownBy(
            () ->
                Compilation.compileWithDocumentationGenerator(
                    outputDirectory,
                    "InvalidTestClassNameInput.java",
                    "import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;",
                    "",
                    "final class InvalidTestClassName implements RefasterRuleCollectionTestCase {}"))
        .cause()
        .isInstanceOf(VerifyException.class)
        .hasMessage(
            "Refaster rule collection test class name 'InvalidTestClassName' does not match '(.*)Test'");
  }

  @Test
  void invalidFileName(@TempDir Path outputDirectory) {
    assertThatThrownBy(
            () ->
                Compilation.compileWithDocumentationGenerator(
                    outputDirectory,
                    "InvalidFileNameTest.java",
                    "import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;",
                    "",
                    "final class InvalidFileNameTest implements RefasterRuleCollectionTestCase {}"))
        .cause()
        .isInstanceOf(VerifyException.class)
        .hasMessage(
            "Refaster rule collection test file name '/InvalidFileNameTest.java' does not match '.*(Input|Output)\\.java'");
  }

  @Test
  void emptyRefasterRuleCollectionTestInput(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "EmptyRefasterRuleCollectionTestInput.java",
        "import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;",
        "",
        "final class EmptyRefasterRuleCollectionTest implements RefasterRuleCollectionTestCase {}");

    verifyGeneratedFileContent(
        outputDirectory,
        "EmptyRefasterRuleCollectionTestInput",
        RefasterTestCases.create(
            URI.create("file:///EmptyRefasterRuleCollectionTestInput.java"),
            "EmptyRefasterRuleCollection",
            /* isInput= */ true,
            ImmutableList.of()));
  }

  @Test
  void singletonRefasterRuleCollectionTestOutput(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "SingletonRefasterRuleCollectionTestOutput.java",
        "import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;",
        "",
        "final class SingletonRefasterRuleCollectionTest implements RefasterRuleCollectionTestCase {",
        "  int testMyRule() {",
        "    return 42;",
        "  }",
        "}");

    verifyGeneratedFileContent(
        outputDirectory,
        "SingletonRefasterRuleCollectionTestOutput",
        RefasterTestCases.create(
            URI.create("file:///SingletonRefasterRuleCollectionTestOutput.java"),
            "SingletonRefasterRuleCollection",
            /* isInput= */ false,
            ImmutableList.of(
                RefasterTestCase.create(
                    "MyRule",
                    """
                    int testMyRule() {
                      return 42;
                    }"""))));
  }

  @Test
  void complexRefasterRuleCollectionTestOutput(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "pkg/ComplexRefasterRuleCollectionTestInput.java",
        "package pkg;",
        "",
        "import com.google.common.collect.ImmutableSet;",
        "import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;",
        "",
        "final class ComplexRefasterRuleCollectionTest implements RefasterRuleCollectionTestCase {",
        "  private static final String IGNORED_CONSTANT = \"constant\";",
        "",
        "  @Override",
        "  public ImmutableSet<Object> elidedTypesAndStaticImports() {",
        "    return ImmutableSet.of();",
        "  }",
        "",
        "  /** Javadoc. */",
        "  String testFirstRule() {",
        "    return \"Don't panic\";",
        "  }",
        "",
        "  // Comment.",
        "  String testSecondRule() {",
        "    return \"Carry a towel\";",
        "  }",
        "",
        "  void testEmptyRule() {}",
        "}");

    verifyGeneratedFileContent(
        outputDirectory,
        "ComplexRefasterRuleCollectionTestInput",
        RefasterTestCases.create(
            URI.create("file:///pkg/ComplexRefasterRuleCollectionTestInput.java"),
            "ComplexRefasterRuleCollection",
            /* isInput= */ true,
            ImmutableList.of(
                RefasterTestCase.create(
                    "FirstRule",
                    """
                    String testFirstRule() {
                      return "Don't panic";
                    }"""),
                RefasterTestCase.create(
                    "SecondRule",
                    """
                    String testSecondRule() {
                      return "Carry a towel";
                    }"""),
                RefasterTestCase.create("EmptyRule", "void testEmptyRule() {}"))));
  }

  private static void verifyGeneratedFileContent(
      Path outputDirectory, String testIdentifier, RefasterTestCases expected) {
    assertThat(
            outputDirectory.resolve(
                String.format("refaster-rule-collection-test-%s.json", testIdentifier)))
        .exists()
        .returns(expected, path -> Json.read(path, RefasterTestCases.class));
  }
}
