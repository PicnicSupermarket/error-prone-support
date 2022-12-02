package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
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
 * A {@link BugChecker} that flags {@link String#toLowerCase()} or {@link String#toUpperCase()}
 * which do not specify a {@code Locale}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Specify a `Locale` when calling `String#to{Lower,Upper}Case`",
    link = BUG_PATTERNS_BASE_URL + "SpecifyLocale",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = LIKELY_ERROR)
public final class SpecifyLocale extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> STRING_TO_LOWER_OR_UPPER_CASE =
      instanceMethod()
          .onExactClass(String.class.getName())
          .namedAnyOf("toLowerCase", "toUpperCase")
          .withNoParameters();

  /** Instantiates a new {@link SpecifyLocale} instance. */
  public SpecifyLocale() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!STRING_TO_LOWER_OR_UPPER_CASE.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .addFix(suggestLocale("Locale.ROOT", tree, state))
        .addFix(suggestLocale("Locale.getDefault()", tree, state))
        .build();
  }

  private static Fix suggestLocale(String locale, MethodInvocationTree tree, VisitorState state) {
    return SuggestedFix.builder()
        .addImport("java.util.Locale")
        .replace(tree, SourceCode.treeToString(tree, state).replace("()", "(" + locale + ")"))
        .build();
  }
}
