package tech.picnic.errorprone.documentation;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static java.util.stream.Collectors.joining;

import com.google.auto.service.AutoService;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.net.URI;
import java.util.Optional;
import java.util.regex.Pattern;
import tech.picnic.errorprone.documentation.RefasterRuleCollectionTestExtractor.RefasterTestCases;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * An {@link Extractor} that describes how to extract data from Refaster rule input and output test
 * classes.
 */
// XXX: Drop this extractor if/when the Refaster test framework is reimplemented such that tests can
// be located alongside rules, rather than in two additional resource files as currently required by
// `RefasterRuleCollection`.
@AutoService(Extractor.class)
@Immutable
@SuppressWarnings("rawtypes" /* See https://github.com/google/auto/issues/870. */)
public final class RefasterRuleCollectionTestExtractor implements Extractor<RefasterTestCases> {
  private static final Matcher<ClassTree> IS_REFASTER_RULE_COLLECTION_TEST_CASE =
      isSubtypeOf("tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase");
  private static final Pattern TEST_CLASS_NAME_PATTERN = Pattern.compile("(.*)Test");
  private static final Pattern TEST_CLASS_FILE_NAME_PATTERN =
      Pattern.compile(".*(Input|Output)\\.java");
  private static final Pattern TEST_METHOD_NAME_PATTERN = Pattern.compile("test(.*)");
  private static final String LINE_SEPARATOR = "\n";
  private static final Splitter LINE_SPLITTER = Splitter.on(LINE_SEPARATOR);

  /** Instantiates a new {@link RefasterRuleCollectionTestExtractor} instance. */
  public RefasterRuleCollectionTestExtractor() {}

  @Override
  public String identifier() {
    return "refaster-rule-collection-test";
  }

  @Override
  public Optional<RefasterTestCases> tryExtract(ClassTree tree, VisitorState state) {
    if (!IS_REFASTER_RULE_COLLECTION_TEST_CASE.matches(tree, state)) {
      return Optional.empty();
    }

    URI sourceFile = state.getPath().getCompilationUnit().getSourceFile().toUri();
    return Optional.of(
        new RefasterTestCases(
            sourceFile,
            getRuleCollectionName(tree),
            isInputFile(sourceFile),
            getRefasterTestCases(tree, state)));
  }

  private static String getRuleCollectionName(ClassTree tree) {
    String className = tree.getSimpleName().toString();

    // XXX: Instead of throwing an error here, it'd be nicer to have a bug checker validate key
    // aspects of `RefasterRuleCollectionTestCase` subtypes.
    return tryExtractPatternGroup(className, TEST_CLASS_NAME_PATTERN)
        .orElseThrow(
            violation(
                "Refaster rule collection test class name '%s' does not match '%s'",
                className, TEST_CLASS_NAME_PATTERN));
  }

  private static boolean isInputFile(URI sourceFile) {
    String path = sourceFile.getPath();

    // XXX: Instead of throwing an error here, it'd be nicer to have a bug checker validate key
    // aspects of `RefasterRuleCollectionTestCase` subtypes.
    return "Input"
        .equals(
            tryExtractPatternGroup(path, TEST_CLASS_FILE_NAME_PATTERN)
                .orElseThrow(
                    violation(
                        "Refaster rule collection test file name '%s' does not match '%s'",
                        path, TEST_CLASS_FILE_NAME_PATTERN)));
  }

  private static ImmutableList<RefasterTestCase> getRefasterTestCases(
      ClassTree tree, VisitorState state) {
    return tree.getMembers().stream()
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast)
        .flatMap(m -> tryExtractRefasterTestCase(m, state).stream())
        .collect(toImmutableList());
  }

  private static Optional<RefasterTestCase> tryExtractRefasterTestCase(
      MethodTree method, VisitorState state) {
    return tryExtractPatternGroup(method.getName().toString(), TEST_METHOD_NAME_PATTERN)
        .map(name -> new RefasterTestCase(name, getFormattedSource(method, state)));
  }

  /**
   * Returns the source code for the specified method.
   *
   * @implNote This operation attempts to trim leading whitespace, such that the start and end of
   *     the method declaration are aligned. The implemented heuristic assumes that the code is
   *     formatted using Google Java Format.
   */
  // XXX: Leading Javadoc and other comments are currently not extracted. Consider fixing this.
  private static String getFormattedSource(MethodTree method, VisitorState state) {
    String source = SourceCode.treeToString(method, state);
    int finalNewline = source.lastIndexOf(LINE_SEPARATOR);
    if (finalNewline < 0) {
      return source;
    }

    int indentation = Math.max(0, source.lastIndexOf(' ') - finalNewline);
    String prefixToStrip = " ".repeat(indentation);

    return LINE_SPLITTER
        .splitToStream(source)
        .map(line -> line.startsWith(prefixToStrip) ? line.substring(indentation) : line)
        .collect(joining(LINE_SEPARATOR));
  }

  private static Optional<String> tryExtractPatternGroup(String input, Pattern pattern) {
    java.util.regex.Matcher matcher = pattern.matcher(input);
    return matcher.matches() ? Optional.of(matcher.group(1)) : Optional.empty();
  }

  @FormatMethod
  private static Supplier<VerifyException> violation(String format, Object... args) {
    return () -> new VerifyException(format.formatted(args));
  }

  record RefasterTestCases(
      URI source,
      String ruleCollection,
      boolean isInput,
      ImmutableList<RefasterTestCase> testCases) {}

  record RefasterTestCase(String name, String content) {}
}
