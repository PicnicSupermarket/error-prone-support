package tech.picnic.errorprone.documentation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableListMultimap.flatteningToImmutableListMultimap;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableTable.toImmutableTable;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.stream.Collectors.joining;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Sets;
import com.google.errorprone.BugPattern.SeverityLevel;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import tech.picnic.errorprone.documentation.ProjectInfo.BugPatternInfo;
import tech.picnic.errorprone.documentation.ProjectInfo.BugPatternTestCases;
import tech.picnic.errorprone.documentation.ProjectInfo.BugPatternTestCases.BugPatternTestCase;
import tech.picnic.errorprone.documentation.ProjectInfo.BugPatternTestCases.TestEntry;
import tech.picnic.errorprone.documentation.ProjectInfo.BugPatternTestCases.TestEntry.Identification;
import tech.picnic.errorprone.documentation.ProjectInfo.BugPatternTestCases.TestEntry.Replacement;
import tech.picnic.errorprone.documentation.ProjectInfo.RefasterTestCases;
import tech.picnic.errorprone.documentation.ProjectInfo.RefasterTestCases.RefasterTestCase;

/**
 * A command line utility that produces configuration files for the Jekyll-based Error Prone Support
 * website.
 */
// XXX: Expand the documentation.
// XXX: Rename this type. Then also update the reference in `website/.gitignore`.
// XXX: Now that we have bug checkers in multiple Maven modules, we should
// likely document the source of each check on the website, perhaps even
// grouping them by module.
public record JekyllCollectionGenerator() {
  // XXX: Find a bette name. Also, externalize this.
  private static final PathMatcher PATH_MATCHER =
      FileSystems.getDefault().getPathMatcher("glob:**/target/docs/*.json");
  private static final YAMLMapper YAML_MAPPER =
      YAMLMapper.builder()
          .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
          .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
          .enable(YAMLGenerator.Feature.USE_PLATFORM_LINE_BREAKS)
          .propertyNamingStrategy(new SnakeCaseStrategy())
          .visibility(PropertyAccessor.FIELD, Visibility.ANY)
          .build();
  @VisibleForTesting static final Path WEBSITE_ROOT = Path.of("website");
  @VisibleForTesting static final Path BUGPATTERNS_ROOT = WEBSITE_ROOT.resolve("_bugpatterns");
  @VisibleForTesting static final Path REFASTER_RULES_ROOT = WEBSITE_ROOT.resolve("_refasterrules");

  /**
   * Runs the application.
   *
   * @param args Arguments to the application; must specify the path to the Error Prone Support
   *     project root, and nothing else.
   * @throws IOException If any file could not be read or written.
   */
  public static void main(String[] args) throws IOException {
    checkArgument(args.length == 1, "Precisely one project root path must be provided");
    Path projectRoot = Path.of(args[0]);

    generateIndex(projectRoot);
    PageGenerator.apply(projectRoot);
  }

  private static void generateIndex(Path projectRoot) throws IOException {
    Path index = projectRoot.resolve(WEBSITE_ROOT).resolve("index.md");
    try (BufferedWriter writer = Files.newBufferedWriter(index, UTF_8)) {
      record IndexFrontMatter(String layout, String title, int navOrder) {}
      writeFrontMatter(writer, new IndexFrontMatter("default", "Home", 1));
      writer.write(
          Files.readString(projectRoot.resolve("README.md")).replace("=\"website/", "=\""));
    }
  }

  private static <T> void writeFrontMatter(BufferedWriter writer, T frontMatter)
      throws IOException {
    /* Write the data as a YAML document, including a document start marker (`---`). */
    YAML_MAPPER.writeValue(writer, frontMatter);
    /* Close the front matter by emitting another three dashes. */
    writer.write("---");
    writer.newLine();
  }

  // XXX: Review whether this class should be split in two: one for bug patterns and one for
  // Refaster rules.
  // XXX: Reorder methods.
  private static final class PageGenerator extends SimpleFileVisitor<Path> {
    private static final Splitter LINE_SPLITTER = Splitter.on(System.lineSeparator());

    private final List<BugPatternInfo> bugPatterns = new ArrayList<>();
    private final List<BugPatternTestCases> bugPatternTests = new ArrayList<>();
    private final List<RefasterTestCases> refasterRuleCollectionTests = new ArrayList<>();

    static void apply(Path projectRoot) throws IOException {
      PageGenerator pageGenerator = new PageGenerator();
      Files.walkFileTree(projectRoot, pageGenerator);
      pageGenerator.writePages(projectRoot);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
      if (!PATH_MATCHER.matches(file)) {
        return FileVisitResult.CONTINUE;
      }

      // XXX: Replace with type switch once we target JDK 21+.
      ProjectInfo entry = Json.read(file, ProjectInfo.class);
      if (entry instanceof BugPatternInfo data) {
        bugPatterns.add(data);
      } else if (entry instanceof BugPatternTestCases data) {
        bugPatternTests.add(data);
      } else if (entry instanceof RefasterTestCases data) {
        refasterRuleCollectionTests.add(data);
      }

      return FileVisitResult.CONTINUE;
    }

    private void writePages(Path projectRoot) throws IOException {
      writePages(
          projectRoot.resolve(BUGPATTERNS_ROOT),
          createBugPatternDescriptions(projectRoot),
          BugPatternDescription::name);
      writePages(
          projectRoot.resolve(REFASTER_RULES_ROOT),
          createRefasterRuleCollection(),
          RefasterRuleCollectionDescription::name);
    }

    private static <T> void writePages(
        Path directory, ImmutableList<T> documents, Function<T, String> nameExtractor)
        throws IOException {
      Files.createDirectories(directory);
      for (T document : documents) {
        try (BufferedWriter writer =
            Files.newBufferedWriter(
                directory.resolve(nameExtractor.apply(document) + ".md"), UTF_8)) {
          writeFrontMatter(writer, document);
        }
      }
    }

    private ImmutableList<BugPatternDescription> createBugPatternDescriptions(Path projectRoot) {
      ImmutableListMultimap<String, TestEntry> bugPatternTestCases =
          bugPatternTests.stream()
              .flatMap(testCases -> testCases.testCases().stream())
              .collect(
                  flatteningToImmutableListMultimap(
                      BugPatternTestCase::classUnderTest, t -> t.entries().stream()));

      return bugPatterns.stream()
          .map(
              bugPattern ->
                  getBugPatternDescription(
                      projectRoot,
                      bugPattern,
                      bugPatternTestCases.get(bugPattern.fullyQualifiedName())))
          .collect(toImmutableList());
    }

    private static BugPatternDescription getBugPatternDescription(
        Path projectRoot, BugPatternInfo bugPattern, ImmutableList<TestEntry> testEntries) {
      ImmutableList.Builder<String> identification = ImmutableList.builder();
      ImmutableList.Builder<String> replacement = ImmutableList.builder();

      // XXX: Replace with type switch once we target JDK 21+.
      for (TestEntry testEntry : testEntries) {
        if (testEntry instanceof Identification entry) {
          identification.add(entry.code());
        } else if (testEntry instanceof Replacement entry) {
          replacement.add(generateDiff(entry));
        }
      }

      return new BugPatternDescription(
          bugPattern.name(),
          bugPattern.name(),
          bugPattern.summary(),
          bugPattern.severityLevel(),
          bugPattern.tags(),
          // XXX: Or use (absolute) string paths and derive `Path` from
          // `projectRoot.getFileSystem()`.
          projectRoot.relativize(Path.of(bugPattern.source())).toString(),
          identification.build(),
          replacement.build());
    }

    private ImmutableList<RefasterRuleCollectionDescription> createRefasterRuleCollection() {
      ImmutableTable<String, Boolean, ImmutableList<RefasterTestCase>> refasterTests =
          refasterRuleCollectionTests.stream()
              .collect(
                  toImmutableTable(
                      RefasterTestCases::ruleCollection,
                      RefasterTestCases::isInput,
                      RefasterTestCases::testCases));

      return refasterTests.rowMap().entrySet().stream()
          .map(
              e ->
                  createRefasterRuleCollection(
                      e.getKey(),
                      requireNonNullElseGet(e.getValue().get(true), ImmutableList::of),
                      requireNonNullElseGet(e.getValue().get(false), ImmutableList::of)))
          .collect(toImmutableList());
    }

    private static RefasterRuleCollectionDescription createRefasterRuleCollection(
        String name,
        ImmutableList<RefasterTestCase> inputTests,
        ImmutableList<RefasterTestCase> outputTests) {
      return new RefasterRuleCollectionDescription(
          name,
          name,
          // XXX: Derive severity from input.
          SUGGESTION,
          // XXX: Derive tags from input (or drop this feature).
          ImmutableList.of("Simplification"),
          // XXX: Derive source location from input.
          String.format(
              "error-prone-contrib/src/main/java/tech/picnic/errorprone/refasterrules/%s.java",
              name),
          createRefasterRule(inputTests, outputTests));
    }

    private static ImmutableList<RefasterRuleCollectionDescription.Rule> createRefasterRule(
        ImmutableList<RefasterTestCase> inputTests, ImmutableList<RefasterTestCase> outputTests) {
      ImmutableMap<String, String> inputs = indexRefasterTestCases(inputTests);
      ImmutableMap<String, String> outputs = indexRefasterTestCases(outputTests);

      // XXX: Consider simply requiring that input and output test cases have the same names.
      return Sets.intersection(inputs.keySet(), outputs.keySet()).stream()
          .map(
              name ->
                  new RefasterRuleCollectionDescription.Rule(
                      name,
                      // XXX: Derive severity from input.
                      SUGGESTION,
                      // XXX: Derive tags from input (or drop this feature).
                      ImmutableList.of("Simplification"),
                      generateDiff(
                          requireNonNull(inputs.get(name), "Input"),
                          requireNonNull(outputs.get(name), "Output"))))
          .collect(toImmutableList());
    }

    private static ImmutableMap<String, String> indexRefasterTestCases(
        ImmutableList<RefasterTestCase> testCases) {
      return testCases.stream()
          .collect(toImmutableMap(RefasterTestCase::name, RefasterTestCase::content));
    }

    private static String generateDiff(Replacement testEntry) {
      return generateDiff(testEntry.input(), testEntry.output());
    }

    private static String generateDiff(String before, String after) {
      List<String> originalLines = LINE_SPLITTER.splitToList(before);
      List<String> replacementLines = LINE_SPLITTER.splitToList(after);

      Patch<String> diff = DiffUtils.diff(originalLines, replacementLines);

      return UnifiedDiffUtils.generateUnifiedDiff(
              "", "", originalLines, diff, Integer.MAX_VALUE / 2)
          .stream()
          .skip(3)
          .collect(joining(System.lineSeparator()));
    }
  }

  private record BugPatternDescription(
      String title,
      String name,
      String summary,
      SeverityLevel severity,
      ImmutableList<String> tags,
      // XXX: The documentation could link to the original test code. Perhaps even with the correct
      // line numbers.
      String source,
      ImmutableList<String> identification,
      ImmutableList<String> replacement) {}

  private record RefasterRuleCollectionDescription(
      String title,
      String name,
      SeverityLevel severity,
      ImmutableList<String> tags,
      // XXX: The documentation could link to the original test code. Perhaps even with the correct
      // line numbers. If we do this, we should do the same for individual rules.
      String source,
      ImmutableList<Rule> rules) {
    private record Rule(
        String name, SeverityLevel severity, ImmutableList<String> tags, String diff) {}
  }
}
