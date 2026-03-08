package tech.picnic.errorprone.documentation;

import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
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
  void simpleRefasterRuleCollection(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "SimpleRules.java",
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
        "}",
        "",
        "@interface BeforeTemplate {}",
        "",
        "@interface AfterTemplate {}");

    verifyGeneratedFileContent(
        outputDirectory,
        "SimpleRules",
        new RefasterRuleCollection(
            URI.create("file:///SimpleRules.java"),
            "SimpleRules",
            "Rules for simplification.",
            "https://error-prone.picnic.tech/refasterrules/SimpleRules#",
            ImmutableList.of(
                new Rule(
                    "MyRule",
                    "Prefer simpler alternative.",
                    "https://error-prone.picnic.tech/refasterrules/SimpleRules#MyRule",
                    SUGGESTION))));
  }

  @Test
  void multipleRulesWithCustomUrl(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "pkg/MultiRules.java",
        "package pkg;",
        "",
        "import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;",
        "",
        "@OnlineDocumentation(\"https://example.com/${topLevelClassName}#${nestedClassName}\")",
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
        "    @AfterTemplate",
        "    int after() {",
        "      return 42;",
        "    }",
        "  }",
        "",
        "  static final class NotARule {",
        "    void helper() {}",
        "  }",
        "}",
        "",
        "@interface BeforeTemplate {}",
        "",
        "@interface AfterTemplate {}");

    verifyGeneratedFileContent(
        outputDirectory,
        "MultiRules",
        new RefasterRuleCollection(
            URI.create("file:///pkg/MultiRules.java"),
            "MultiRules",
            "",
            "https://example.com/MultiRules#",
            ImmutableList.of(
                new Rule("RuleA", "", "https://example.com/MultiRules#RuleA", SUGGESTION),
                new Rule("RuleB", "", "https://example.com/MultiRules#RuleB", SUGGESTION))));
  }

  private static void verifyGeneratedFileContent(
      Path outputDirectory, String testClass, RefasterRuleCollection expected) {
    assertThat(outputDirectory.resolve("refaster-rule-collection-%s.json".formatted(testClass)))
        .exists()
        .returns(expected, path -> Json.read(path, RefasterRuleCollection.class));
  }
}
