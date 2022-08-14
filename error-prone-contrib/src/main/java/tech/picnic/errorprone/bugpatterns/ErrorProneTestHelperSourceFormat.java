package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.sun.tools.javac.util.Position.NOPOS;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.SourceVersion;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.ImportOrderer;
import com.google.googlejavaformat.java.JavaFormatterOptions.Style;
import com.google.googlejavaformat.java.RemoveUnusedImports;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.MultiTaskListener;
import com.sun.tools.javac.util.Context;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} that flags improperly formatted Error Prone test code.
 *
 * <p>All test code should be formatted in accordance with Google Java Format's {@link Formatter}
 * output, and imports should be ordered according to the {@link Style#GOOGLE Google} style.
 *
 * <p>This checker inspects inline code passed to {@code
 * com.google.errorprone.CompilationTestHelper} and {@code
 * com.google.errorprone.BugCheckerRefactoringTestHelper}. It requires that this code is properly
 * formatted and that its imports are organized. Only code that represents the expected output of a
 * refactoring operation is allowed to have unused imports, as most {@link BugChecker}s do not (and
 * are not able to) remove imports that become obsolete as a result of applying their suggested
 * fix(es).
 */
// XXX: The check does not flag well-formatted text blocks with insufficient indentation. Cover this
// using an generic check or wait for Google Java Format support (see
// https://github.com/google/google-java-format/issues/883#issuecomment-1404336418).
// XXX: The check does not flag well-formatted text blocks with excess indentation.
// XXX: ^ Validate this claim.
// XXX: GJF guesses the line separator to be used by inspecting the source. When using text blocks
// this may cause the current unconditional use of `\n` not to be sufficient when building on
// Windows; TBD.
// XXX: Forward compatibility: ignore "malformed" code in tests that, based on an
// `@DisabledForJreRange` or `@EnableForJreRange` annotation, target a Java runtime greater than the
// current runtime.
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Test code should follow the Google Java style (and when targeting JDK 15+ be "
            + "specified using a single text block)",
    link = BUG_PATTERNS_BASE_URL + "ErrorProneTestHelperSourceFormat",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = STYLE)
// XXX: Drop suppression if/when the `avoidTextBlocks` field is dropped.
@SuppressWarnings("java:S2160" /* Super class equality definition suffices. */)
public final class ErrorProneTestHelperSourceFormat extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String FLAG_AVOID_TEXT_BLOCKS =
      "ErrorProneTestHelperSourceFormat:AvoidTextBlocks";
  private static final Formatter FORMATTER = new Formatter();
  private static final Matcher<ExpressionTree> INPUT_SOURCE_ACCEPTING_METHOD =
      anyOf(
          instanceMethod()
              .onDescendantOf("com.google.errorprone.CompilationTestHelper")
              .named("addSourceLines"),
          instanceMethod()
              .onDescendantOf("com.google.errorprone.BugCheckerRefactoringTestHelper")
              .named("addInputLines"),
          // XXX: Add tests for `Compilation.compileWithDocumentationGenerator`. Until done, make
          // sure to update this matcher if that method's class or name is changed/moved.
          staticMethod()
              .onClass("tech.picnic.errorprone.documentation.Compilation")
              .named("compileWithDocumentationGenerator"));
  private static final Matcher<ExpressionTree> OUTPUT_SOURCE_ACCEPTING_METHOD =
      instanceMethod()
          .onDescendantOf("com.google.errorprone.BugCheckerRefactoringTestHelper.ExpectOutput")
          .named("addOutputLines");
  private static final Supplier<Boolean> IS_JABEL_ENABLED =
      VisitorState.memoize(ErrorProneTestHelperSourceFormat::isJabelEnabled);
  // XXX: Proper name for this?
  // XXX: Something about tabs.
  private static final String TEXT_BLOCK_MARKER = "\"\"\"";
  private static final String TEXT_BLOCK_LINE_SEPARATOR = "\n";
  private static final String DEFAULT_TEXT_BLOCK_INDENTATION = " ".repeat(12);
  private static final String METHOD_SELECT_ARGUMENT_RELATIVE_INDENTATION = " ".repeat(8);

  private final boolean avoidTextBlocks;

  /** Instantiates a default {@link ErrorProneTestHelperSourceFormat} instance. */
  public ErrorProneTestHelperSourceFormat() {
    this(ErrorProneFlags.empty());
  }

  /**
   * Instantiates a customized {@link ErrorProneTestHelperSourceFormat}.
   *
   * @param flags Any provided command line flags.
   */
  @Inject
  ErrorProneTestHelperSourceFormat(ErrorProneFlags flags) {
    avoidTextBlocks = flags.getBoolean(FLAG_AVOID_TEXT_BLOCKS).orElse(Boolean.FALSE);
  }

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    boolean isOutputSource = OUTPUT_SOURCE_ACCEPTING_METHOD.matches(tree, state);
    if (!isOutputSource && !INPUT_SOURCE_ACCEPTING_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    List<? extends ExpressionTree> sourceLines =
        tree.getArguments()
            .subList(ASTHelpers.getSymbol(tree).params().size() - 1, tree.getArguments().size());
    if (sourceLines.isEmpty()) {
      return buildDescription(tree).setMessage("No source code provided").build();
    }

    /* Attempt to format the source code only if it fully consists of constant expressions. */
    return getConstantSourceCode(sourceLines)
        .map(source -> flagFormattingIssues(sourceLines, source, isOutputSource, state))
        .orElse(Description.NO_MATCH);
  }

  private Description flagFormattingIssues(
      List<? extends ExpressionTree> sourceLines,
      String source,
      boolean retainUnusedImports,
      VisitorState state) {
    MethodInvocationTree methodInvocation = (MethodInvocationTree) state.getPath().getLeaf();

    String formatted;
    try {
      String gjfResult = formatSourceCode(source, retainUnusedImports);
      formatted = canUseTextBlocks(sourceLines, state) ? gjfResult : gjfResult.stripTrailing();
    } catch (
        @SuppressWarnings("java:S1166" /* Stack trace not relevant. */)
        FormatterException e) {
      return buildDescription(methodInvocation)
          .setMessage(String.format("Source code is malformed: %s", e.getMessage()))
          .build();
    }

    boolean isFormatted = source.equals(formatted);
    boolean hasStringLiteralMismatch = shouldUpdateStringLiteralFormat(sourceLines, state);

    if (isFormatted && !hasStringLiteralMismatch) {
      return Description.NO_MATCH;
    }

    int startPos = ASTHelpers.getStartPosition(sourceLines.get(0));
    int endPos = state.getEndPosition(sourceLines.get(sourceLines.size() - 1));
    boolean hasNewlineMismatch =
        !isFormatted && source.stripTrailing().equals(formatted.stripTrailing());

    /*
     * The source code is not properly formatted and/or not specified using a single text block.
     * Report the more salient of the violations, and suggest a fix if sufficient source information
     * is available.
     */
    boolean isTextBlockUsageIssue = isFormatted || (hasNewlineMismatch && hasStringLiteralMismatch);
    boolean canSuggestFix = startPos != NOPOS && endPos != NOPOS;
    return buildDescription(methodInvocation)
        .setMessage(
            isTextBlockUsageIssue
                ? String.format(
                    "Test code should %sbe specified using a single text block",
                    avoidTextBlocks ? "not " : "")
                : String.format(
                    "Test code should follow the Google Java style%s",
                    hasNewlineMismatch ? " (pay attention to trailing newlines)" : ""))
        .addFix(
            canSuggestFix
                ? SuggestedFix.replace(
                    startPos,
                    endPos,
                    canUseTextBlocks(sourceLines, state)
                        ? toTextBlockExpression(methodInvocation, formatted, state)
                        : toLineEnumeration(formatted, state))
                : SuggestedFix.emptyFix())
        .build();
  }

  private boolean shouldUpdateStringLiteralFormat(
      List<? extends ExpressionTree> sourceLines, VisitorState state) {
    return canUseTextBlocks(sourceLines, state)
        ? (sourceLines.size() > 1 || !SourceCode.isTextBlock(sourceLines.get(0), state))
        : sourceLines.stream().anyMatch(tree -> SourceCode.isTextBlock(tree, state));
  }

  private boolean canUseTextBlocks(List<? extends ExpressionTree> sourceLines, VisitorState state) {
    return !avoidTextBlocks
        && (SourceVersion.supportsTextBlocks(state.context)
            || IS_JABEL_ENABLED.get(state)
            || sourceLines.stream().anyMatch(line -> SourceCode.isTextBlock(line, state)));
  }

  private static String toTextBlockExpression(
      MethodInvocationTree tree, String source, VisitorState state) {
    String indentation = suggestTextBlockIndentation(tree, state);

    // XXX: Verify trailing """ on new line.
    return TEXT_BLOCK_MARKER
        + System.lineSeparator()
        + indentation
        + source
            .replace(TEXT_BLOCK_LINE_SEPARATOR, System.lineSeparator() + indentation)
            .replace("\\", "\\\\")
            .replace(TEXT_BLOCK_MARKER, "\"\"\\\"")
        + TEXT_BLOCK_MARKER;
  }

  private static String toLineEnumeration(String source, VisitorState state) {
    return Splitter.on(TEXT_BLOCK_LINE_SEPARATOR)
        .splitToStream(source)
        .map(state::getConstantExpression)
        .collect(joining(", "));
  }

  // XXX: This makes certain assumptions; document these.
  private static String suggestTextBlockIndentation(
      MethodInvocationTree target, VisitorState state) {
    CharSequence sourceCode = state.getSourceCode();
    if (sourceCode == null) {
      return DEFAULT_TEXT_BLOCK_INDENTATION;
    }

    String source = sourceCode.toString();
    return getIndentation(target.getArguments().get(1), source)
        .or(() -> getIndentation(target.getArguments().get(0), source))
        .or(
            () ->
                getIndentation(target.getMethodSelect(), source)
                    .map(METHOD_SELECT_ARGUMENT_RELATIVE_INDENTATION::concat))
        .orElse(DEFAULT_TEXT_BLOCK_INDENTATION);
  }

  private static Optional<String> getIndentation(Tree tree, String source) {
    int startPos = ASTHelpers.getStartPosition(tree);
    if (startPos == NOPOS) {
      return Optional.empty();
    }

    int finalNewLine = source.lastIndexOf(System.lineSeparator(), startPos);
    if (finalNewLine < 0) {
      return Optional.empty();
    }

    return Optional.of(source.substring(finalNewLine + 1, startPos))
        .filter(CharMatcher.whitespace()::matchesAllOf);
  }

  private static String formatSourceCode(String source, boolean retainUnusedImports)
      throws FormatterException {
    String withReorderedImports = ImportOrderer.reorderImports(source, Style.GOOGLE);
    String withOptionallyRemovedImports =
        retainUnusedImports
            ? withReorderedImports
            : RemoveUnusedImports.removeUnusedImports(withReorderedImports);
    return FORMATTER.formatSource(withOptionallyRemovedImports);
  }

  // XXX: This logic is duplicated in `BugPatternTestExtractor`. Can we do better?
  private static Optional<String> getConstantSourceCode(
      List<? extends ExpressionTree> sourceLines) {
    StringBuilder source = new StringBuilder();

    for (ExpressionTree sourceLine : sourceLines) {
      if (source.length() > 0) {
        source.append(TEXT_BLOCK_LINE_SEPARATOR);
      }

      String value = ASTHelpers.constValue(sourceLine, String.class);
      if (value == null) {
        return Optional.empty();
      }

      source.append(value);
    }

    return Optional.of(source.toString());
  }

  /**
   * Tells whether Jabel appears to be enabled, indicating that text blocks are supported, even if
   * may <em>appear</em> that they {@link SourceVersion#supportsTextBlocks(Context) aren't}.
   *
   * @see <a href="https://github.com/bsideup/jabel">Jabel</a>
   */
  private static boolean isJabelEnabled(VisitorState state) {
    return MultiTaskListener.instance(state.context).getTaskListeners().stream()
        .anyMatch(listener -> listener.toString().contains("com.github.bsideup.jabel"));
  }
}
