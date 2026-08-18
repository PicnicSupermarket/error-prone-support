package tech.picnic.errorprone.documentation;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tech.picnic.errorprone.documentation.ProjectInfo.RefasterRuleCollection;
import tech.picnic.errorprone.documentation.ProjectInfo.RefasterRuleCollection.Rule;

final class RefasterRuleCollectionExtractorTest {
  @Test
  void noOnlineDocumentation(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory, "NoAnnotation.java", "final class NoAnnotation {}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void customOnlineDocumentation(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "CustomUrlRules.java",
        "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
        "import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;",
        "",
        "@OnlineDocumentation(\"https://example.com\")",
        "final class CustomUrlRules {",
        "  static final class MyRule {",
        "    @BeforeTemplate",
        "    int before() {",
        "      return 0;",
        "    }",
        "  }",
        "",
        "  /** Inline.<p>Tight.<pre>{@code tight();}</pre> */",
        "  static final class TightRule {",
        "    @BeforeTemplate",
        "    int before() {",
        "      return 1;",
        "    }",
        "  }",
        "}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void simpleRefasterRuleCollection(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "SimpleRules.java",
        "import com.google.errorprone.refaster.annotation.AfterTemplate;",
        "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
        "import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;",
        "",
        "/** Rules for simplification. */",
        "@OnlineDocumentation",
        "final class SimpleRules {",
        "  private SimpleRules() {}",
        "",
        "  /** Prefer simpler alternative. */",
        "  static final class MyRule {",
        "    @BeforeTemplate",
        "    int before() {",
        "      return 0;",
        "    }",
        "",
        "    @AfterTemplate",
        "    int after() {",
        "      return 1;",
        "    }",
        "  }",
        "",
        "  static final class UndocumentedRule {",
        "    @BeforeTemplate",
        "    int before() {",
        "      return 2;",
        "    }",
        "  }",
        "}");

    verifyGeneratedFileContent(
        outputDirectory,
        "SimpleRules",
        new RefasterRuleCollection(
            URI.create("file:///SimpleRules.java"),
            "SimpleRules",
            "Rules for simplification.",
            ImmutableList.of(
                new Rule("MyRule", "Prefer simpler alternative.", SUGGESTION),
                /* A rule collection's Javadoc is not inherited by the rules it contains. */
                new Rule("UndocumentedRule", "", SUGGESTION))));
  }

  @Test
  void multipleRules(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "pkg/MultiRules.java",
        "package pkg;",
        "",
        "import com.google.errorprone.refaster.annotation.AfterTemplate;",
        "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
        "import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;",
        "",
        "@OnlineDocumentation",
        "final class MultiRules {",
        "  private MultiRules() {}",
        "",
        "  static final class RuleA {",
        "    @BeforeTemplate",
        "    String before() {",
        "      return \"old\";",
        "    }",
        "",
        "    @AfterTemplate",
        "    String after() {",
        "      return \"new\";",
        "    }",
        "  }",
        "",
        "  static final class RuleB {",
        "    @BeforeTemplate",
        "    int before() {",
        "      return 42;",
        "    }",
        "  }",
        "",
        "  static final class NotARule {",
        "    static final int CONSTANT = 1;",
        "",
        "    void helper() {}",
        "  }",
        "}");

    verifyGeneratedFileContent(
        outputDirectory,
        "MultiRules",
        new RefasterRuleCollection(
            URI.create("file:///pkg/MultiRules.java"),
            "MultiRules",
            "",
            ImmutableList.of(
                new Rule("RuleA", "", SUGGESTION), new Rule("RuleB", "", SUGGESTION))));
  }

  @Test
  void nestedOnlineDocumentation(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "Outer.java",
        "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
        "import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;",
        "",
        "final class Outer {",
        "  @OnlineDocumentation",
        "  static final class NestedRules {",
        "    static final class MyRule {",
        "      @BeforeTemplate",
        "      int before() {",
        "        return 0;",
        "      }",
        "    }",
        "  }",
        "}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void annotatedRules(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "AnnotatedRules.java",
        "import com.google.errorprone.BugPattern.SeverityLevel;",
        "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
        "import tech.picnic.errorprone.refaster.annotation.Description;",
        "import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;",
        "import tech.picnic.errorprone.refaster.annotation.Severity;",
        "",
        "/** Collection Javadoc. */",
        "@OnlineDocumentation",
        "@Severity(SeverityLevel.WARNING)",
        "@Description(\"Collection description.\")",
        "final class AnnotatedRules {",
        "  /** Javadoc on rule. */",
        "  @Severity(SeverityLevel.ERROR)",
        "  @Description(\"Rule description.\")",
        "  static final class RuleWithAnnotations {",
        "    @BeforeTemplate",
        "    int before() {",
        "      return 0;",
        "    }",
        "  }",
        "",
        "  /** Only Javadoc. */",
        "  static final class RuleWithJavadocOnly {",
        "    @BeforeTemplate",
        "    int before() {",
        "      return 1;",
        "    }",
        "  }",
        "",
        "  static final class RuleWithoutAnnotationsOrJavadoc {",
        "    @BeforeTemplate",
        "    int before() {",
        "      return 2;",
        "    }",
        "  }",
        "",
        "  /** */",
        "  static final class RuleWithEmptyJavadoc {",
        "    @BeforeTemplate",
        "    int before() {",
        "      return 3;",
        "    }",
        "  }",
        "}");

    verifyGeneratedFileContent(
        outputDirectory,
        "AnnotatedRules",
        new RefasterRuleCollection(
            URI.create("file:///AnnotatedRules.java"),
            "AnnotatedRules",
            /* The `@Description` annotation takes precedence over the Javadoc. */
            "Collection description.",
            ImmutableList.of(
                new Rule("RuleWithAnnotations", "Rule description.", ERROR),
                /* A rule's own Javadoc takes precedence over the inherited `@Description`. */
                new Rule("RuleWithJavadocOnly", "Only Javadoc.", WARNING),
                new Rule("RuleWithoutAnnotationsOrJavadoc", "Collection description.", WARNING),
                /* An empty Javadoc comment is disregarded. */
                new Rule("RuleWithEmptyJavadoc", "Collection description.", WARNING))));
  }

  @Test
  void javadocMarkup(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "MarkupRules.java",
        "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
        "import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;",
        "",
        "/**",
        " * Rules for {@link String}s.",
        " *",
        " * <p><strong>Warning:</strong> these rules are {@code contrived}.",
        " *",
        " * <pre>{@code",
        " * List<String> strings = new ArrayList<>();",
        " * }</pre>",
        " */",
        "@OnlineDocumentation",
        "final class MarkupRules {",
        "  /**",
        "   * Prefer {@link String#isEmpty()} over {@link String#length() the alternative}, as",
        "   * discussed <a href=\"https://example.com\">here</a>.",
        "   *",
        "   * <P>See {@linkplain String#chars()}, which is <EM>very</EM> <code>useful</code>,",
        "   * <BR>and costs {@literal <1ms}.",
        "   */",
        "  static final class MyRule {",
        "    @BeforeTemplate",
        "    int before() {",
        "      return 0;",
        "    }",
        "  }",
        "",
        "  /** Inline.<p>Tight.<pre>{@code tight();}</pre> */",
        "  static final class TightRule {",
        "    @BeforeTemplate",
        "    int before() {",
        "      return 1;",
        "    }",
        "  }",
        "}");

    verifyGeneratedFileContent(
        outputDirectory,
        "MarkupRules",
        new RefasterRuleCollection(
            URI.create("file:///MarkupRules.java"),
            "MarkupRules",
            """
            Rules for `String`s.

            **Warning:** these rules are `contrived`.

            ```java
            List<String> strings = new ArrayList<>();
            ```""",
            ImmutableList.of(
                new Rule(
                    "MyRule",
                    /* Line breaks within a paragraph are retained; Markdown treats them as such. */
                    """
                    Prefer `String#isEmpty()` over the alternative, as
                    discussed <a href="https://example.com">here</a>.

                    See String#chars(), which is *very* `useful`,\s\s
                    and costs <1ms.""",
                    SUGGESTION),
                new Rule(
                    "TightRule",
                    /* Paragraphs and code blocks are separated even without surrounding blanks. */
                    """
                    Inline.

                    Tight.

                    ```java
                    tight();
                    ```""",
                    SUGGESTION))));
  }

  private static void verifyGeneratedFileContent(
      Path outputDirectory, String testClass, RefasterRuleCollection expected) {
    assertThat(outputDirectory.resolve("refaster-rule-collection-%s.json".formatted(testClass)))
        .exists()
        .returns(expected, path -> Json.read(path, RefasterRuleCollection.class));
  }
}
