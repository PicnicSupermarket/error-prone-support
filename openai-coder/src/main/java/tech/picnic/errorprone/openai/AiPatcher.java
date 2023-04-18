package tech.picnic.errorprone.openai;

import static com.google.common.collect.ImmutableSetMultimap.toImmutableSetMultimap;
import static java.util.stream.Collectors.joining;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Streams;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.openai.IssueExtractor.Issue;

// XXX: Consider using https://picocli.info/quick-guide.html. Can also be used for an interactive
// CLI.
// XXX: Introduce README.
// XXX: Consider creating a binary executable using GraalVM.
// XXX: Add support for sending a suitable subset of the code to OpenAI, so as (a) to better deal
// with the token limit and (b) potentially reduce cost.
// XXX: Add support for the Sarif output format.
// XXX: Add support for an interactive mode. Support multiple rounds.
// XXX: Add support for filtering by message and file pattern.
// XXX: Add support for a "run until unchanged" mode.
public final class AiPatcher {
  private static final Pattern FILE_LOCATION_MARKER =
      Pattern.compile("^(.*?\\.java):\\[(\\d+)(?:,(\\d+))?\\] ");
  // XXX: Rename
  private static final String OPENAI_TOKEN_VARIABLE = "openapi_token";
  @Nullable private static final String OPENAI_TOKEN = System.getenv(OPENAI_TOKEN_VARIABLE);

  // Allow a custom source lookup directory to be specified.
  // Group by file.
  public static void main(String... args) {
    if (OPENAI_TOKEN == null) {
      System.err.printf(
          "OpenAI API token not found in environment variable '%s'.%n", OPENAI_TOKEN_VARIABLE);
      System.exit(1);
    }

    try {
      suggestFixes(
          getIssuesByFile(LogLineExtractor.mavenErrorAndWarningExtractor().extract(System.in)));
    } catch (IOException e) {
      // XXX: Fix
      throw new RuntimeException(e);
    }

    // Explicitly exit to prevent `mvn exec:java` from handing due to long-lived OkHTTP threads.
    System.exit(0);
  }

  private static void suggestFixes(ImmutableSetMultimap<Path, String> issuesByFile)
      throws IOException {
    try (OpenAi openAi = OpenAi.create(OPENAI_TOKEN)) {
      for (Map.Entry<Path, Set<String>> e : Multimaps.asMap(issuesByFile).entrySet()) {
        suggestFixes(e.getKey(), e.getValue(), openAi);
      }
    }
  }

  private static void suggestFixes(Path file, Set<String> issueDescriptions, OpenAi openAi)
      throws IOException {
    //  XXX: Cleanup

    String originalCode = Files.readString(file);

    if (file.toString().contains("RefasterRuleCollection")) {
      return;
    }

    String instruction =
        Streams.mapWithIndex(
                issueDescriptions.stream(),
                (description, index) -> String.format("%s. %s", index + 1, description))
            .collect(joining("\n", "Resolve the following issues:\n", "\n"));

    System.out.println("Instruction: " + instruction);

    if (true) {
      //      return;
    }

    // XXX: Handle case with too much input/output (tokens).
    // XXX: Handle error messages.

    String result = openAi.requestEdit(originalCode, instruction);

    // XXX: !!! Don't create diff in patch mode; just apply the patch.
    System.out.printf("Fix for %s:%n", Diffs.unifiedDiff(originalCode, result, file.toString()));
  }

  private static ImmutableSetMultimap<Path, String> getIssuesByFile(List<String> logMessages) {
    // XXX: Allow the path to be specified.
    IssueExtractor<Path> issueExtractor =
        new PathResolvingIssueExtractor(
            new PathFinder(FileSystems.getDefault(), Path.of("")),
            new AggregatingIssueExtractor<>(
                ImmutableSet.of(
                    new PlexusCompilerIssueExtractor(), new CheckstyleIssueExtractor())));

    return logMessages.stream()
        .flatMap(issueExtractor::extract)
        .collect(toImmutableSetMultimap(Issue::file, Issue::description));
  }
}
