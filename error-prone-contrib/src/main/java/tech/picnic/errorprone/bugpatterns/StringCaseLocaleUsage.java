package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.FRAGILE_CODE;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static com.sun.tools.javac.parser.Tokens.TokenKind.RPAREN;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.ErrorProneTokens;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.util.Position;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} that flags calls to {@link String#toLowerCase()} and {@link
 * String#toUpperCase()}, as these methods implicitly rely on the environment's default locale.
 */
// XXX: Also flag `String::toLowerCase` and `String::toUpperCase` method references. For these cases
// the suggested fix should introduce a lambda expression with a parameter of which the name does
// not coincide with the name of an existing variable name. Such functionality should likely be
// introduced in a utility class.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Specify a `Locale` when calling `String#to{Lower,Upper}Case`",
    link = BUG_PATTERNS_BASE_URL + "StringCaseLocaleUsage",
    linkType = CUSTOM,
    severity = WARNING,
    tags = FRAGILE_CODE)
public final class StringCaseLocaleUsage extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> DEFAULT_LOCALE_CASE_CONVERSION =
      instanceMethod()
          .onExactClass(String.class.getName())
          .namedAnyOf("toLowerCase", "toUpperCase")
          .withNoParameters();

  /** Instantiates a new {@link StringCaseLocaleUsage} instance. */
  public StringCaseLocaleUsage() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!DEFAULT_LOCALE_CASE_CONVERSION.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    int closingParenPosition = getClosingParenPosition(tree, state);
    if (closingParenPosition == Position.NOPOS) {
      return describeMatch(tree);
    }

    return buildDescription(tree)
        .addFix(suggestLocale(closingParenPosition, "Locale.ROOT"))
        .addFix(suggestLocale(closingParenPosition, "Locale.getDefault()"))
        .build();
  }

  private static Fix suggestLocale(int insertPosition, String locale) {
    return SuggestedFix.builder()
        .addImport("java.util.Locale")
        .replace(insertPosition, insertPosition, locale)
        .build();
  }

  private static int getClosingParenPosition(MethodInvocationTree tree, VisitorState state) {
    int startPosition = ASTHelpers.getStartPosition(tree);
    if (startPosition == Position.NOPOS) {
      return Position.NOPOS;
    }

    return Streams.findLast(
            ErrorProneTokens.getTokens(SourceCode.treeToString(tree, state), state.context).stream()
                .filter(t -> t.kind() == RPAREN))
        .map(token -> startPosition + token.pos())
        .orElse(Position.NOPOS);
  }
}
