package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.sun.source.tree.Tree.Kind.INSTANCE_OF;
import static com.sun.source.tree.Tree.Kind.LAMBDA_EXPRESSION;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.LambdaExpressionTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LambdaExpressionTree;

/** A {@link BugChecker} that aligns usages of T.class::isInstance. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Use Class::isInstance where possible",
    link = BUG_PATTERNS_BASE_URL + "IsInstanceUsage",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = STYLE)
public final class IsInstanceUsage extends BugChecker implements LambdaExpressionTreeMatcher {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link IsInstanceUsage} instance. */
  public IsInstanceUsage() {}

  @Override
  public Description matchLambdaExpression(LambdaExpressionTree tree, VisitorState state) {
    if (LAMBDA_EXPRESSION == tree.getKind() && INSTANCE_OF == tree.getBody().getKind()) {
      return constructDescription(
          tree, constructFix(tree, ((InstanceOfTree) tree.getBody()).getType()));
    }
    return Description.NO_MATCH;
  }

  private Description constructDescription(
      LambdaExpressionTree tree, SuggestedFix.Builder fixBuilder) {
    return describeMatch(tree, fixBuilder.build());
  }

  private static SuggestedFix.Builder constructFix(LambdaExpressionTree tree, Object target) {
    return SuggestedFix.builder().replace(tree, target + ".class::isInstance");
  }
}
