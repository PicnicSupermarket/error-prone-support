package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.base.Splitter;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.ImportOrderer;
import com.google.googlejavaformat.java.JavaFormatterOptions.Style;
import com.google.googlejavaformat.java.RemoveUnusedImports;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.util.Position;
import java.util.List;
import java.util.Optional;

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
// XXX: Once we target JDK 17 (optionally?) suggest text block fixes.
// XXX: GJF guesses the line separator to be used by inspecting the source. When using text blocks
// this may cause the current unconditional use of `\n` not to be sufficient when building on
// Windows; TBD.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Test code should follow the Google Java style",
    link = BUG_PATTERNS_BASE_URL + "ErrorProneTestHelperSourceFormat",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = STYLE)
public final class ErrorProneTestHelperSourceFormat extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Formatter FORMATTER = new Formatter();
  private static final Matcher<ExpressionTree> INPUT_SOURCE_ACCEPTING_METHOD =
      anyOf(
          instanceMethod()
              .onDescendantOf("com.google.errorprone.CompilationTestHelper")
              .named("addSourceLines"),
          instanceMethod()
              .onDescendantOf("com.google.errorprone.BugCheckerRefactoringTestHelper")
              .named("addInputLines"));
  private static final Matcher<ExpressionTree> OUTPUT_SOURCE_ACCEPTING_METHOD =
      instanceMethod()
          .onDescendantOf("com.google.errorprone.BugCheckerRefactoringTestHelper.ExpectOutput")
          .named("addOutputLines");

  /** Instantiates a new {@link ErrorProneTestHelperSourceFormat} instance. */
  public ErrorProneTestHelperSourceFormat() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    boolean isOutputSource = OUTPUT_SOURCE_ACCEPTING_METHOD.matches(tree, state);
    if (!isOutputSource && !INPUT_SOURCE_ACCEPTING_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    List<? extends ExpressionTree> sourceLines =
        tree.getArguments().subList(1, tree.getArguments().size());
    if (sourceLines.isEmpty()) {
      return buildDescription(tree).setMessage("No source code provided").build();
    }

    int startPos = ASTHelpers.getStartPosition(sourceLines.get(0));
    int endPos = state.getEndPosition(sourceLines.get(sourceLines.size() - 1));

    /* Attempt to format the source code only if it fully consists of constant expressions. */
    return getConstantSourceCode(sourceLines)
        .map(source -> flagFormattingIssues(startPos, endPos, source, isOutputSource, state))
        .orElse(Description.NO_MATCH);
  }

  private Description flagFormattingIssues(
      int startPos, int endPos, String source, boolean retainUnusedImports, VisitorState state) {
    Tree methodInvocation = state.getPath().getLeaf();

    String formatted;
    try {
      formatted = formatSourceCode(source, retainUnusedImports).trim();
    } catch (
        @SuppressWarnings("java:S1166" /* Stack trace not relevant. */)
        FormatterException e) {
      return buildDescription(methodInvocation)
          .setMessage(String.format("Source code is malformed: %s", e.getMessage()))
          .build();
    }

    if (source.trim().equals(formatted)) {
      return Description.NO_MATCH;
    }

    if (startPos == Position.NOPOS || endPos == Position.NOPOS) {
      /*
       * We have insufficient source information to emit a fix, so we only flag the fact that the
       * code isn't properly formatted.
       */
      return describeMatch(methodInvocation);
    }

    /*
     * The code isn't properly formatted; replace all lines with the properly formatted
     * alternatives.
     */
    return describeMatch(
        methodInvocation,
        SuggestedFix.replace(
            startPos,
            endPos,
            Splitter.on('\n')
                .splitToStream(formatted)
                .map(state::getConstantExpression)
                .collect(joining(", "))));
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

  private static Optional<String> getConstantSourceCode(
      List<? extends ExpressionTree> sourceLines) {
    StringBuilder source = new StringBuilder();

    for (ExpressionTree sourceLine : sourceLines) {
      Object value = ASTHelpers.constValue(sourceLine);
      if (value == null) {
        return Optional.empty();
      }

      source.append(value).append('\n');
    }

    return Optional.of(source.toString());
  }
}
