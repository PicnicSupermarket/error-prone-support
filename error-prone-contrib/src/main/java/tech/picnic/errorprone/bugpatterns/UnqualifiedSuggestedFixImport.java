package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.FRAGILE_CODE;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;

/**
 * A {@link BugChecker} that flags suggested fixes that involve unconditional imports.
 *
 * <p>Such unconditional imports may clash with other imports of the same identifier.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Avoid direct invocation of `SuggestedFix#add{,Static}Import`",
    link = BUG_PATTERNS_BASE_URL + "UnqualifiedSuggestedFixImport",
    linkType = CUSTOM,
    severity = WARNING,
    tags = FRAGILE_CODE)
public final class UnqualifiedSuggestedFixImport extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> FIX_BUILDER_METHOD =
      instanceMethod().onDescendantOf(SuggestedFix.Builder.class.getCanonicalName());

  /** Instantiates a new {@link UnqualifiedSuggestedFixImport} instance. */
  public UnqualifiedSuggestedFixImport() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!FIX_BUILDER_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    switch (ASTHelpers.getSymbol(tree).getSimpleName().toString()) {
      case "addImport":
        return createDescription(
            tree, "SuggestedFix.Builder#addImport", "SuggestedFixes#qualifyType");
      case "addStaticImport":
        return createDescription(
            tree, "SuggestedFix.Builder#addStaticImport", "SuggestedFixes#qualifyStaticImport");
      default:
        return Description.NO_MATCH;
    }
  }

  private Description createDescription(
      MethodInvocationTree tree, String method, String alternative) {
    return buildDescription(tree)
        .setMessage(
            String.format("Prefer `%s` over direct invocation of `%s`", alternative, method))
        .build();
  }
}
