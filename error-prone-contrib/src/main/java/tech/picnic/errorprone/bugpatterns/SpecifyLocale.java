package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
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
 * which do not specify a Locale.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Specify `Locale.ROOT` or `Locale.getDefault()` when calling `String#to{Lower,Upper}Case` without a specific Locale",
    link = BUG_PATTERNS_BASE_URL + "SpecifyLocale",
    linkType = CUSTOM,
    severity = WARNING,
    tags = LIKELY_ERROR)
public final class SpecifyLocale extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> STRING_TO_UPPER_OR_LOWER_CASE =
      instanceMethod()
          .onExactClass(String.class.getName())
          .namedAnyOf("toLowerCase", "toUpperCase");

  /** Instantiates a new {@link SpecifyLocale} instance. */
  public SpecifyLocale() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (STRING_TO_UPPER_OR_LOWER_CASE.matches(tree, state) && tree.getArguments().isEmpty()) {
      return buildDescription(tree)
          .addFix(buildFix("Locale.ROOT", tree, state))
          .addFix(buildFix("Locale.getDefault()", tree, state))
          .build();
    }
    return Description.NO_MATCH;
  }

  private static Fix buildFix(
      String localeToSpecify, MethodInvocationTree tree, VisitorState state) {
    return SuggestedFix.builder()
        .replace(
            tree, SourceCode.treeToString(tree, state).replace("()", "(" + localeToSpecify + ")"))
        .addImport("java.util.Locale")
        .build();
  }
}
