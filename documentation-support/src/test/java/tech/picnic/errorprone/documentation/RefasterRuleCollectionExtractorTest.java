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
        "}");

    verifyGeneratedFileContent(
        outputDirectory,
        "SimpleRules",
        new RefasterRuleCollection(
            URI.create("file:///SimpleRules.java"),
            "SimpleRules",
            "Rules for simplification.",
            ImmutableList.of(new Rule("MyRule", "Prefer simpler alternative.", SUGGESTION))));
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
        "}");

    verifyGeneratedFileContent(
        outputDirectory,
        "AnnotatedRules",
        new RefasterRuleCollection(
            URI.create("file:///AnnotatedRules.java"),
            "AnnotatedRules",
            "Collection description.",
            ImmutableList.of(
                new Rule("RuleWithAnnotations", "Rule description.", ERROR),
                new Rule("RuleWithJavadocOnly", "Collection description.", WARNING),
                new Rule("RuleWithoutAnnotationsOrJavadoc", "Collection description.", WARNING))));
  }

  private static void verifyGeneratedFileContent(
      Path outputDirectory, String testClass, RefasterRuleCollection expected) {
    assertThat(outputDirectory.resolve("refaster-rule-collection-%s.json".formatted(testClass)))
        .exists()
        .returns(expected, path -> Json.read(path, RefasterRuleCollection.class));
  }
}
