package tech.picnic.errorprone.openai;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

// XXX: Consider using https://picocli.info/quick-guide.html. Can also be used for an interactive
// CLI.
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

  private static void suggestFixes(ImmutableMap<Path, String> issuesByFile) throws IOException {
    try (OpenAi openAi = OpenAi.create(OPENAI_TOKEN)) {
      for (Map.Entry<Path, String> e : issuesByFile.entrySet()) {
        suggestFixes(e.getKey(), e.getValue(), openAi);
      }
    }
  }

  private static void suggestFixes(Path file, String issueDescriptions, OpenAi openAi)
      throws IOException {
    //  XXX: Cleanup

    String originalCode = Files.readString(file);

    if (file.toString().contains("RefasterRuleCollection")) {
      return;
    }

    System.out.println("Instruction: " + issueDescriptions);

    if (true) {
      //      return;
    }

    // XXX: Handle case with too much input/output (tokens).
    // XXX: Handle error messages.

    String result =
        openAi.requestEdit(
            originalCode, "Resolve the following Java compilation errors:\n\n" + issueDescriptions);

    // XXX: !!! Don't create diff in patch mode; just apply the patch.
    System.out.printf("Fix for %s:%n", Diffs.unifiedDiff(originalCode, result, file.toString()));
  }

  private static ImmutableMap<Path, String> getIssuesByFile(List<String> logMessages) {
    Map<Path, String> messages = new HashMap<>();

    for (String message : logMessages) {
      extractPathAndMessage(message, (path, m) -> messages.merge(path, m, String::concat));
    }

    return ImmutableMap.copyOf(messages);
  }

  // XXX: Clean this up.
  private static void extractPathAndMessage(String logLine, BiConsumer<Path, String> sink) {
    Optional.of(FILE_LOCATION_MARKER.matcher(logLine))
        .filter(Matcher::find)
        .ifPresent(
            m ->
                new PathFinder(FileSystems.getDefault(), Path.of(""))
                    .findPath(m.group(1))
                    .ifPresent(
                        path ->
                            sink.accept(
                                path,
                                m.group(3) == null
                                    ? String.format(
                                        "- Line %s: %s", m.group(2), logLine.substring(m.end()))
                                    : String.format(
                                        "- Line %s, column %s: %s",
                                        m.group(2), m.group(3), logLine.substring(m.end())))));
  }
}
