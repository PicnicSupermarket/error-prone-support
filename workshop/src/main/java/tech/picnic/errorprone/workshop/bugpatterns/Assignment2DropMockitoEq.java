package tech.picnic.errorprone.workshop.bugpatterns;

import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.staticMethod;

import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import java.util.List;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags method invocations for which all arguments are wrapped using
 * {@link org.mockito.Mockito#eq}; this is redundant.
 */
@BugPattern(
    summary = "Don't unnecessarily use Mockito's `eq(...)`",
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class Assignment2DropMockitoEq extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> MOCKITO_EQ_METHOD =
      staticMethod().onClass("org.mockito.ArgumentMatchers").named("eq");

  /** Instantiates a new {@link Assignment2DropMockitoEq} instance. */
  public Assignment2DropMockitoEq() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    // XXX: Make sure to return `Description.NO_MATCH` if the `tree` doesn't have arguments, or if
    // the `isEqInvocation` method below returns `false` for at least one of the arguments.
    List<? extends ExpressionTree> arguments = tree.getArguments();
    if (arguments.isEmpty() || !arguments.stream().allMatch(arg -> isEqInvocation(arg, state))) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder suggestedFix = SuggestedFix.builder();
    for (ExpressionTree arg : arguments) {
      suggestedFix.replace(
          arg,
          SourceCode.treeToString(
              Iterables.getOnlyElement(((MethodInvocationTree) arg).getArguments()), state));
    }

    return describeMatch(tree, suggestedFix.build());
  }

  private static boolean isEqInvocation(ExpressionTree tree, VisitorState state) {
    return tree instanceof MethodInvocationTree && MOCKITO_EQ_METHOD.matches(tree, state);
  }
}
