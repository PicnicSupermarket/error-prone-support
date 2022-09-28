package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.refaster.Refaster;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} that flags unnecessary {@link Refaster#anyOf(Object[])} usages.
 *
 * <p>Note that this logic can't be implemented as a Refaster template, as the {@link Refaster}
 * class is treated specially.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "`Refaster#anyOf` should be passed at least two parameters",
    link = BUG_PATTERNS_BASE_URL + "RefasterAnyOfUsage",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class RefasterAnyOfUsage extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> REFASTER_ANY_OF =
      staticMethod().onClass(Refaster.class.getName()).named("anyOf");

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (REFASTER_ANY_OF.matches(tree, state)) {
      switch (tree.getArguments().size()) {
        case 0:
          // We can't safely fix this case; dropping the expression may produce non-compilable code.
          return describeMatch(tree);
        case 1:
          return describeMatch(
              tree,
              SuggestedFix.replace(
                  tree, SourceCode.treeToString(tree.getArguments().get(0), state)));
        default:
          /* Handled below. */
      }
    }

    return Description.NO_MATCH;
  }
}
