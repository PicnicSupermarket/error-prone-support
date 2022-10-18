package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyMethod;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.not;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SimpleTreeVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} that flags string concatenations that produce a format string; in such cases
 * the string concatenation should instead be deferred to the invoked method.
 *
 * @implNote This checker is based on the implementation of {@link
 *     com.google.errorprone.bugpatterns.flogger.FloggerStringConcatenation}.
 */
// XXX: Support arbitrary `@FormatMethod`-annotated methods.
// XXX: For (explicit or delegated) invocations of `java.util.Formatter` _strictly speaking_ we
// should introduce special handling of `Formattable` arguments, as this check would replace a
// `Formattable#toString` invocation with a `Formattable#formatTo` invocation. But likely that
// should be considered a bug fix, too.
// XXX: Introduce a separate check that adds/removes the `Locale` parameter to `String.format`
// invocations, as necessary. See also a comment in the `StringJoin` check.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Defer string concatenation to the invoked method",
    link = BUG_PATTERNS_BASE_URL + "FormatStringConcatenation",
    linkType = CUSTOM,
    severity = WARNING,
    tags = SIMPLIFICATION)
public final class FormatStringConcatenation extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  /**
   * AssertJ exposes varargs {@code fail} methods with a {@link Throwable}-accepting overload, the
   * latter of which should not be flagged.
   */
  private static final Matcher<ExpressionTree> ASSERTJ_FAIL_WITH_THROWABLE_METHOD =
      anyMethod()
          .anyClass()
          .withAnyName()
          .withParameters(String.class.getName(), Throwable.class.getName());
  // XXX: Drop some of these methods if we use Refaster to replace some with others.
  private static final Matcher<ExpressionTree> ASSERTJ_FORMAT_METHOD =
      anyOf(
          instanceMethod()
              .onDescendantOf("org.assertj.core.api.AbstractAssert")
              .namedAnyOf("overridingErrorMessage", "withFailMessage"),
          allOf(
              instanceMethod()
                  .onDescendantOf("org.assertj.core.api.AbstractSoftAssertions")
                  .named("fail"),
              not(ASSERTJ_FAIL_WITH_THROWABLE_METHOD)),
          instanceMethod()
              .onDescendantOf("org.assertj.core.api.AbstractStringAssert")
              .named("isEqualTo"),
          instanceMethod()
              .onDescendantOf("org.assertj.core.api.AbstractThrowableAssert")
              .namedAnyOf(
                  "hasMessage",
                  "hasMessageContaining",
                  "hasMessageEndingWith",
                  "hasMessageStartingWith",
                  "hasRootCauseMessage",
                  "hasStackTraceContaining"),
          instanceMethod()
              .onDescendantOf("org.assertj.core.api.Descriptable")
              .namedAnyOf("as", "describedAs"),
          instanceMethod()
              .onDescendantOf("org.assertj.core.api.ThrowableAssertAlternative")
              .namedAnyOf(
                  "withMessage",
                  "withMessageContaining",
                  "withMessageEndingWith",
                  "withMessageStartingWith",
                  "withStackTraceContaining"),
          allOf(
              instanceMethod().onDescendantOf("org.assertj.core.api.WithAssertions").named("fail"),
              not(ASSERTJ_FAIL_WITH_THROWABLE_METHOD)),
          allOf(
              staticMethod()
                  .onClassAny(
                      "org.assertj.core.api.Assertions",
                      "org.assertj.core.api.BDDAssertions",
                      "org.assertj.core.api.Fail")
                  .named("fail"),
              not(ASSERTJ_FAIL_WITH_THROWABLE_METHOD)));
  private static final Matcher<ExpressionTree> GUAVA_FORMAT_METHOD =
      anyOf(
          staticMethod()
              .onClass("com.google.common.base.Preconditions")
              .namedAnyOf("checkArgument", "checkNotNull", "checkState"),
          staticMethod().onClass("com.google.common.base.Verify").named("verify"));
  // XXX: Add `PrintWriter`, maybe others.
  private static final Matcher<ExpressionTree> JDK_FORMAT_METHOD =
      anyOf(
          staticMethod().onClass("java.lang.String").named("format"),
          instanceMethod().onExactClass("java.util.Formatter").named("format"));
  private static final Matcher<ExpressionTree> SLF4J_FORMAT_METHOD =
      instanceMethod()
          .onDescendantOf("org.slf4j.Logger")
          .namedAnyOf("debug", "error", "info", "trace", "warn");

  /** Instantiates the default {@link FormatStringConcatenation}. */
  public FormatStringConcatenation() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (hasNonConstantStringConcatenationArgument(tree, 0, state)) {
      return flagViolation(tree, ASSERTJ_FORMAT_METHOD, 0, "%s", state)
          .or(() -> flagViolation(tree, JDK_FORMAT_METHOD, 0, "%s", state))
          .or(() -> flagViolation(tree, SLF4J_FORMAT_METHOD, 0, "{}", state))
          .orElse(Description.NO_MATCH);
    }

    if (hasNonConstantStringConcatenationArgument(tree, 1, state)) {
      return flagViolation(tree, GUAVA_FORMAT_METHOD, 1, "%s", state)
          .or(() -> flagViolation(tree, JDK_FORMAT_METHOD, 1, "%s", state))
          .or(() -> flagViolation(tree, SLF4J_FORMAT_METHOD, 1, "{}", state))
          .orElse(Description.NO_MATCH);
    }

    return Description.NO_MATCH;
  }

  /**
   * Flags the given method invocation if it matches a targeted method and passes a non-compile time
   * constant string concatenation as a format string.
   */
  private Optional<Description> flagViolation(
      MethodInvocationTree tree,
      Matcher<ExpressionTree> matcher,
      int formatStringParam,
      String formatSpecifier,
      VisitorState state) {
    if (!matcher.matches(tree, state)) {
      /* The invoked method is not targeted by this check. */
      return Optional.empty();
    }

    List<? extends ExpressionTree> arguments = tree.getArguments();

    if (arguments.size() > formatStringParam + 1) {
      /*
       * This method invocation uses explicit string concatenation but _also_ already relies on
       * format specifiers: flag but don't suggest a fix.
       */
      return Optional.of(describeMatch(tree));
    }

    ExpressionTree formatStringArg = arguments.get(formatStringParam);
    ReplacementArgumentsConstructor replacementConstructor =
        new ReplacementArgumentsConstructor(formatSpecifier);
    formatStringArg.accept(replacementConstructor, state);

    return Optional.of(
        describeMatch(
            tree,
            SuggestedFix.replace(
                formatStringArg, replacementConstructor.getReplacementArguments(state))));
  }

  private static boolean hasNonConstantStringConcatenationArgument(
      MethodInvocationTree tree, int argPosition, VisitorState state) {
    List<? extends ExpressionTree> arguments = tree.getArguments();
    if (arguments.size() <= argPosition) {
      /* This method doesn't accept enough parameters. */
      return false;
    }

    ExpressionTree argument = ASTHelpers.stripParentheses(arguments.get(argPosition));
    return argument instanceof BinaryTree
        && isStringTyped(argument, state)
        && ASTHelpers.constValue(argument, String.class) == null;
  }

  private static boolean isStringTyped(ExpressionTree tree, VisitorState state) {
    return ASTHelpers.isSameType(ASTHelpers.getType(tree), state.getSymtab().stringType, state);
  }

  private static class ReplacementArgumentsConstructor
      extends SimpleTreeVisitor<Void, VisitorState> {
    private final StringBuilder formatString = new StringBuilder();
    private final List<Tree> formatArguments = new ArrayList<>();
    private final String formatSpecifier;

    ReplacementArgumentsConstructor(String formatSpecifier) {
      this.formatSpecifier = formatSpecifier;
    }

    @Nullable
    @Override
    public Void visitBinary(BinaryTree tree, VisitorState state) {
      if (tree.getKind() == Kind.PLUS && isStringTyped(tree, state)) {
        tree.getLeftOperand().accept(this, state);
        tree.getRightOperand().accept(this, state);
      } else {
        appendExpression(tree);
      }

      return null;
    }

    @Nullable
    @Override
    public Void visitParenthesized(ParenthesizedTree tree, VisitorState state) {
      return tree.getExpression().accept(this, state);
    }

    @Nullable
    @Override
    protected Void defaultAction(Tree tree, VisitorState state) {
      appendExpression(tree);
      return null;
    }

    private void appendExpression(Tree tree) {
      if (tree instanceof LiteralTree) {
        formatString.append(((LiteralTree) tree).getValue());
      } else {
        formatString.append(formatSpecifier);
        formatArguments.add(tree);
      }
    }

    private String getReplacementArguments(VisitorState state) {
      return state.getConstantExpression(formatString.toString())
          + ", "
          + formatArguments.stream()
              .map(tree -> SourceCode.treeToString(tree, state))
              .collect(joining(", "));
    }
  }
}
