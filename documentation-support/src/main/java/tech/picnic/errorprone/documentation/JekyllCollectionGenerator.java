package tech.picnic.errorprone.documentation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableListMultimap.flatteningToImmutableListMultimap;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableTable.toImmutableTable;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.google.auto.value.AutoValue;
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
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.documentation.BugPatternExtractor.BugPatternDocumentation;
import tech.picnic.errorprone.documentation.BugPatternTestExtractor.IdentificationTestEntry;
import tech.picnic.errorprone.documentation.BugPatternTestExtractor.ReplacementTestEntry;
import tech.picnic.errorprone.documentation.BugPatternTestExtractor.TestCase;
import tech.picnic.errorprone.documentation.BugPatternTestExtractor.TestCases;
import tech.picnic.errorprone.documentation.BugPatternTestExtractor.TestEntry;
import tech.picnic.errorprone.documentation.models.RefasterTemplateCollectionTestData;
import tech.picnic.errorprone.documentation.models.RefasterTemplateTestData;

// XXX: Rename this class. Then also update the reference in `website/.gitignore`.
public final class JekyllCollectionGenerator {
  // XXX: Find a bette name. Also, externalize this.
  private static final PathMatcher PATH_MATCHER =
      FileSystems.getDefault().getPathMatcher("glob:**/target/docs/*.json");

  // XXX: Review class setup.
  private JekyllCollectionGenerator() {}

  public static void main(String[] args) throws IOException {
    checkArgument(args.length == 1, "Precisely one project root path must be provided");
    Path projectRoot = Paths.get(args[0]).toAbsolutePath();

    generateIndex(projectRoot);
    PageGenerator.apply(projectRoot);
  }

  private static void generateIndex(Path projectRoot) throws IOException {
    try (BufferedWriter writer =
        Files.newBufferedWriter(projectRoot.resolve("website").resolve("index.md"), UTF_8)) {
      writer.write("---");
      writer.newLine();
      writer.write("layout: default");
      writer.newLine();
      writer.write("title: Home");
      writer.newLine();
      writer.write("nav_order: 1");
      writer.newLine();
      writer.write("---");
      writer.newLine();
      writer.write(
          Files.readString(projectRoot.resolve("README.md")).replace("=\"website/", "=\""));
    }
  }

  // XXX: Review this class should be split in two: one for bug patterns and one for Refaster rules.
  private static final class PageGenerator extends SimpleFileVisitor<Path> {
    private static final Splitter LINE_SPLITTER = Splitter.on(System.lineSeparator());
    private static final YAMLMapper YAML_MAPPER =
        YAMLMapper.builder()
            .visibility(PropertyAccessor.FIELD, Visibility.ANY)
            .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
            .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            .enable(YAMLGenerator.Feature.USE_PLATFORM_LINE_BREAKS)
            .build();

    // XXX: Rename the data types?
    private final List<BugPatternDocumentation> bugPatterns = new ArrayList<>();
    private final List<TestCases> bugPatternTests = new ArrayList<>();
    private final List<RefasterTemplateCollectionTestData> refasterTemplateCollectionTests =
        new ArrayList<>();

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

      // XXX: If we use a consistent ID separator, then this can become a switch statement. Now we
      // depend on evaluation order.
      // XXX: Alternatively, use polymorphism and let Jackson figure it out.
      String fileName = file.getFileName().toString();
      if (fileName.startsWith("bugpattern-test")) {
        bugPatternTests.add(Json.read(file, TestCases.class));
      } else if (fileName.startsWith("bugpattern")) {
        bugPatterns.add(Json.read(file, BugPatternDocumentation.class));
      } else if (fileName.startsWith("refaster-test")) {
        refasterTemplateCollectionTests.add(
            Json.read(file, RefasterTemplateCollectionTestData.class));
      } else {
        // XXX: Handle differently?
        throw new IllegalStateException("Unexpected file: " + fileName);
      }

      return FileVisitResult.CONTINUE;
    }

    private void writePages(Path projectRoot) throws IOException {
      Path website = projectRoot.resolve("website");
      writePages(
          website.resolve("_bugpatterns"),
          getJekyllBugPatternDescriptions(projectRoot),
          JekyllBugPatternDescription::name);
      writePages(
          website.resolve("_refasterrules"),
          getJekyllRefasterRuleCollectionDescription(),
          JekyllRefasterRuleCollectionDescription::name);
    }

    private static <T> void writePages(
        Path directory, ImmutableList<T> documents, Function<T, String> nameExtractor)
        throws IOException {
      for (T document : documents) {
        Files.createDirectories(directory);
        try (BufferedWriter writer =
            Files.newBufferedWriter(
                directory.resolve(nameExtractor.apply(document) + ".md"), UTF_8)) {
          YAML_MAPPER.writeValue(writer, document);
          writer.write("---");
          writer.newLine();
        }
      }
    }

    private ImmutableList<JekyllBugPatternDescription> getJekyllBugPatternDescriptions(
        Path projectRoot) {
      ImmutableListMultimap<String, TestEntry> bugPatternTestCases =
          bugPatternTests.stream()
              .flatMap(testCases -> testCases.testCases().stream())
              .collect(
                  flatteningToImmutableListMultimap(
                      TestCase::classUnderTest, t -> t.entries().stream()));

      return bugPatterns.stream()
          .map(
              b ->
                  new AutoValue_JekyllCollectionGenerator_JekyllBugPatternDescription(
                      b.name(),
                      b.name(),
                      b.summary(),
                      b.severityLevel(),
                      b.tags(),
                      // XXX: Derive `Path` from filesytem.
                      Path.of(b.source()).relativize(projectRoot).toString(),
                      bugPatternTestCases.get(b.fullyQualifiedName()).stream()
                          .filter(t -> t.type() == TestEntry.TestType.IDENTIFICATION)
                          .map(t -> ((IdentificationTestEntry) t).code())
                          .collect(toImmutableList()),
                      bugPatternTestCases.get(b.fullyQualifiedName()).stream()
                          .filter(t -> t.type() == TestEntry.TestType.REPLACEMENT)
                          .map(t -> generateDiff((ReplacementTestEntry) t))
                          .collect(toImmutableList())))
          .collect(toImmutableList());
    }

    private ImmutableList<JekyllRefasterRuleCollectionDescription>
        getJekyllRefasterRuleCollectionDescription() {
      ImmutableTable<String, Boolean, List<RefasterTemplateTestData>> refasterTests =
          refasterTemplateCollectionTests.stream()
              .collect(
                  toImmutableTable(
                      RefasterTemplateCollectionTestData::templateCollection,
                      RefasterTemplateCollectionTestData::isInput,
                      RefasterTemplateCollectionTestData::templateTests));

      return refasterTests.rowMap().entrySet().stream()
          .map(
              c ->
                  new AutoValue_JekyllCollectionGenerator_JekyllRefasterRuleCollectionDescription(
                      c.getKey(),
                      c.getKey(),
                      // XXX: Derive severity from input.
                      SUGGESTION,
                      // XXX: Derive tags from input (or drop this feature).
                      ImmutableList.of("Simplification"),
                      // XXX: Derive source location from input.
                      String.format(
                          "error-prone-contrib/src/main/java/tech/picnic/errorprone/refasterrules/%s.java",
                          c.getKey()),
                      getRules(c.getValue().get(true), c.getValue().get(false))))
          .collect(toImmutableList());
    }

    private static ImmutableList<JekyllRefasterRuleCollectionDescription.Rule> getRules(
        @Nullable List<RefasterTemplateTestData> inputTests,
        @Nullable List<RefasterTemplateTestData> outputTests) {
      ImmutableMap<String, String> inputs = indexRefasterTestData(inputTests);
      ImmutableMap<String, String> outputs = indexRefasterTestData(outputTests);

      return Sets.intersection(inputs.keySet(), outputs.keySet()).stream()
          .map(
              name ->
                  new AutoValue_JekyllCollectionGenerator_JekyllRefasterRuleCollectionDescription_Rule(
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

    private static ImmutableMap<String, String> indexRefasterTestData(
        @Nullable List<RefasterTemplateTestData> data) {
      return data == null
          ? ImmutableMap.of()
          : data.stream()
              .collect(
                  toImmutableMap(
                      RefasterTemplateTestData::templateName,
                      RefasterTemplateTestData::templateTestContent));
    }

    private static String generateDiff(ReplacementTestEntry testEntry) {
      return generateDiff(testEntry.input(), testEntry.output());
    }

    private static String generateDiff(String before, String after) {
      // XXX: Extract splitter.
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

  @AutoValue
  abstract static class JekyllBugPatternDescription {
    // XXX: Make this a derived property?
    abstract String title();

    abstract String name();

    abstract String summary();

    abstract SeverityLevel severity();

    abstract ImmutableList<String> tags();

    // XXX: The documentation could link to the original test code. Perhaps even with the correct
    // line numbers.
    abstract String source();

    // XXX: The `identification` and `replacement` fields have odd names.
    abstract ImmutableList<String> identification();

    abstract ImmutableList<String> replacement();
  }

  @AutoValue
  abstract static class JekyllRefasterRuleCollectionDescription {
    // XXX: Make this a derived property?
    abstract String title();

    abstract String name();

    abstract SeverityLevel severity();

    abstract ImmutableList<String> tags();

    // XXX: The documentation could link to the original test code. Perhaps even with the correct
    // line numbers. If we do this, we should do the same for individual rules.
    abstract String source();

    abstract ImmutableList<Rule> rules();

    @AutoValue
    abstract static class Rule {
      abstract String name();

      abstract SeverityLevel severity();

      abstract ImmutableList<String> tags();

      abstract String diff();
    }
  }
}
