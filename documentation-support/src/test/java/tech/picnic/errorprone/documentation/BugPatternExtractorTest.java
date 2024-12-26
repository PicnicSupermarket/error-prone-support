package tech.picnic.errorprone.documentation;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import java.net.URI;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tech.picnic.errorprone.documentation.BugPatternExtractor.BugPatternDocumentation;

final class BugPatternExtractorTest {
  @Test
  void noBugPattern(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "TestCheckerWithoutAnnotation.java",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "public final class TestCheckerWithoutAnnotation extends BugChecker {}");

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }

  @Test
  void minimalBugPattern(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "MinimalBugChecker.java",
        "package pkg;",
        "",
        "import com.google.errorprone.BugPattern;",
        "import com.google.errorprone.BugPattern.SeverityLevel;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "@BugPattern(summary = \"MinimalBugChecker summary\", severity = SeverityLevel.ERROR)",
        "public final class MinimalBugChecker extends BugChecker {}");

    verifyGeneratedFileContent(
        outputDirectory,
        "MinimalBugChecker",
        BugPatternDocumentation.create(
            URI.create("file:///MinimalBugChecker.java"),
            "pkg.MinimalBugChecker",
            "MinimalBugChecker",
            ImmutableList.of(),
            "",
            ImmutableList.of(),
            "MinimalBugChecker summary",
            "",
            ERROR,
            /* canDisable= */ true,
            ImmutableList.of(SuppressWarnings.class.getCanonicalName())));
  }

  @Test
  void completeBugPattern(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "CompleteBugChecker.java",
        "package pkg;",
        "",
        "import com.google.errorprone.BugPattern;",
        "import com.google.errorprone.BugPattern.SeverityLevel;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "import org.junit.jupiter.api.Test;",
        "",
        "@BugPattern(",
        "    name = \"OtherName\",",
        "    summary = \"CompleteBugChecker summary\",",
        "    linkType = BugPattern.LinkType.CUSTOM,",
        "    link = \"https://error-prone.picnic.tech\",",
        "    explanation = \"Example explanation\",",
        "    severity = SeverityLevel.SUGGESTION,",
        "    altNames = \"Check\",",
        "    tags = BugPattern.StandardTags.SIMPLIFICATION,",
        "    disableable = false,",
        "    suppressionAnnotations = {BugPattern.class, Test.class})",
        "public final class CompleteBugChecker extends BugChecker {}");

    verifyGeneratedFileContent(
        outputDirectory,
        "CompleteBugChecker",
        BugPatternDocumentation.create(
            URI.create("file:///CompleteBugChecker.java"),
            "pkg.CompleteBugChecker",
            "OtherName",
            ImmutableList.of("Check"),
            "https://error-prone.picnic.tech",
            ImmutableList.of("Simplification"),
            "CompleteBugChecker summary",
            "Example explanation",
            SUGGESTION,
            /* canDisable= */ false,
            ImmutableList.of(BugPattern.class.getCanonicalName(), "org.junit.jupiter.api.Test")));
  }

  @Test
  void undocumentedSuppressionBugPattern(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "UndocumentedSuppressionBugPattern.java",
        "package pkg;",
        "",
        "import com.google.errorprone.BugPattern;",
        "import com.google.errorprone.BugPattern.SeverityLevel;",
        "import com.google.errorprone.bugpatterns.BugChecker;",
        "",
        "@BugPattern(",
        "    summary = \"UndocumentedSuppressionBugPattern summary\",",
        "    severity = SeverityLevel.WARNING,",
        "    documentSuppression = false)",
        "public final class UndocumentedSuppressionBugPattern extends BugChecker {}");

    verifyGeneratedFileContent(
        outputDirectory,
        "UndocumentedSuppressionBugPattern",
        BugPatternDocumentation.create(
            URI.create("file:///UndocumentedSuppressionBugPattern.java"),
            "pkg.UndocumentedSuppressionBugPattern",
            "UndocumentedSuppressionBugPattern",
            ImmutableList.of(),
            "",
            ImmutableList.of(),
            "UndocumentedSuppressionBugPattern summary",
            "",
            WARNING,
            /* canDisable= */ true,
            ImmutableList.of()));
  }

  private static void verifyGeneratedFileContent(
      Path outputDirectory, String testClass, BugPatternDocumentation expected) {
    assertThat(outputDirectory.resolve(String.format("bugpattern-%s.json", testClass)))
        .exists()
        .returns(expected, path -> Json.read(path, BugPatternDocumentation.class));
  }
}
