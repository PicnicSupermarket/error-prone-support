package tech.picnic.errorprone.documentation;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class BugPatternExtractorTest {
  @Test
  void noBugPattern(@TempDir Path outputDirectory) {
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
  void minimalBugPattern(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "MinimalBugChecker.java",
        """
        package pkg;

        import com.google.errorprone.BugPattern;
        import com.google.errorprone.BugPattern.SeverityLevel;
        import com.google.errorprone.bugpatterns.BugChecker;

        @BugPattern(summary = "MinimalBugChecker summary", severity = SeverityLevel.ERROR)
        public final class MinimalBugChecker extends BugChecker {}
        """);

    verifyGeneratedFileContent(
        outputDirectory,
        "MinimalBugChecker",
        """
        {
          "fullyQualifiedName": "pkg.MinimalBugChecker",
          "name": "MinimalBugChecker",
          "altNames": [],
          "link": "",
          "tags": [],
          "summary": "MinimalBugChecker summary",
          "explanation": "",
          "severityLevel": "ERROR",
          "canDisable": true,
          "suppressionAnnotations": [
            "java.lang.SuppressWarnings"
          ]
        }
        """);
  }

  @Test
  void completeBugPattern(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "CompleteBugChecker.java",
        """
        package pkg;

        import com.google.errorprone.BugPattern;
        import com.google.errorprone.BugPattern.SeverityLevel;
        import com.google.errorprone.bugpatterns.BugChecker;
        import org.junit.jupiter.api.Test;

        @BugPattern(
            name = "OtherName",
            summary = "CompleteBugChecker summary",
            linkType = BugPattern.LinkType.CUSTOM,
            link = "https://error-prone.picnic.tech",
            explanation = "Example explanation",
            severity = SeverityLevel.SUGGESTION,
            altNames = "Check",
            tags = BugPattern.StandardTags.SIMPLIFICATION,
            disableable = false,
            suppressionAnnotations = {BugPattern.class, Test.class})
        public final class CompleteBugChecker extends BugChecker {}
        """);

    verifyGeneratedFileContent(
        outputDirectory,
        "CompleteBugChecker",
        """
        {
          "fullyQualifiedName": "pkg.CompleteBugChecker",
          "name": "OtherName",
          "altNames": [
            "Check"
          ],
          "link": "https://error-prone.picnic.tech",
          "tags": [
            "Simplification"
          ],
          "summary": "CompleteBugChecker summary",
          "explanation": "Example explanation",
          "severityLevel": "SUGGESTION",
          "canDisable": false,
          "suppressionAnnotations": [
            "com.google.errorprone.BugPattern",
            "org.junit.jupiter.api.Test"
          ]
        }
        """);
  }

  @Test
  void undocumentedSuppressionBugPattern(@TempDir Path outputDirectory) {
    Compilation.compileWithDocumentationGenerator(
        outputDirectory,
        "UndocumentedSuppressionBugPattern.java",
        """
        package pkg;

        import com.google.errorprone.BugPattern;
        import com.google.errorprone.BugPattern.SeverityLevel;
        import com.google.errorprone.bugpatterns.BugChecker;

        @BugPattern(
            summary = "UndocumentedSuppressionBugPattern summary",
            severity = SeverityLevel.WARNING,
            documentSuppression = false)
        public final class UndocumentedSuppressionBugPattern extends BugChecker {}
        """);

    verifyGeneratedFileContent(
        outputDirectory,
        "UndocumentedSuppressionBugPattern",
        """
        {
          "fullyQualifiedName": "pkg.UndocumentedSuppressionBugPattern",
          "name": "UndocumentedSuppressionBugPattern",
          "altNames": [],
          "link": "",
          "tags": [],
          "summary": "UndocumentedSuppressionBugPattern summary",
          "explanation": "",
          "severityLevel": "WARNING",
          "canDisable": true,
          "suppressionAnnotations": []
        }
        """);
  }

  private static void verifyGeneratedFileContent(
      Path outputDirectory, String testClass, String expectedContent) {
    String resourceName = String.format("bugpattern-%s.json", testClass);
    assertThat(outputDirectory.resolve(resourceName))
        .content(UTF_8)
        .isEqualToIgnoringWhitespace(expectedContent);
  }
}
