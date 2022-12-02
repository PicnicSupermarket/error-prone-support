package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.FRAGILE_CODE;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} that flags calls to {@link String#toLowerCase()} and {@link
 * String#toUpperCase()}, as these methods implicitly rely on the environment's default locale.
 */
// XXX: Also flag `String::toLowerCase` and `String::toUpperCase` method references.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Specify a `Locale` when calling `String#to{Lower,Upper}Case`",
    link = BUG_PATTERNS_BASE_URL + "SpecifyLocale",
    linkType = CUSTOM,
    severity = WARNING,
    tags = FRAGILE_CODE)
public final class SpecifyLocale extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> DEFAULT_LOCALE_CASE_CONVERSION =
      instanceMethod()
          .onExactClass(String.class.getName())
          .namedAnyOf("toLowerCase", "toUpperCase")
          .withNoParameters();

  /** Instantiates a new {@link SpecifyLocale} instance. */
  public SpecifyLocale() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!DEFAULT_LOCALE_CASE_CONVERSION.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .addFix(suggestLocale(tree, "Locale.ROOT", state))
        .addFix(suggestLocale(tree, "Locale.getDefault()", state))
        .build();
  }

  private static Fix suggestLocale(MethodInvocationTree tree, String locale, VisitorState state) {
    return SuggestedFix.builder()
        .addImport("java.util.Locale")
        .replace(tree, SourceCode.treeToString(tree, state).replaceFirst("\\(", '(' + locale))
        .build();
  }
}
