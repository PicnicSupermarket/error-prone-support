package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.sun.source.tree.Tree.Kind.IDENTIFIER;
import static com.sun.source.tree.Tree.Kind.INSTANCE_OF;
import static com.sun.source.tree.Tree.Kind.LAMBDA_EXPRESSION;
import static com.sun.source.tree.Tree.Kind.MEMBER_SELECT;
import static com.sun.source.tree.Tree.Kind.METHOD_INVOCATION;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.LambdaExpressionTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;

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
    if (ImmutableSet.of(LAMBDA_EXPRESSION, METHOD_INVOCATION).contains(tree.getKind())) {
      if (INSTANCE_OF == tree.getBody().getKind()) {
        return constructDescription(
            tree, state, constructFix(tree, ((InstanceOfTree) tree.getBody()).getType()));
      } else if (METHOD_INVOCATION == tree.getBody().getKind()) {
        return treatMethodInvocation(tree, (MethodInvocationTree) tree.getBody(), state);
      }
    }
    return Description.NO_MATCH;
  }

  private Description treatMethodInvocation(
      LambdaExpressionTree tree, MethodInvocationTree methodInvocationTree, VisitorState state) {
    if (MEMBER_SELECT != methodInvocationTree.getMethodSelect().getKind()
        || !((MemberSelectTree) methodInvocationTree.getMethodSelect())
            .getIdentifier()
            .contentEquals("isInstance")) {
      return Description.NO_MATCH;
    }
    MemberSelectTree methodSelect = (MemberSelectTree) methodInvocationTree.getMethodSelect();
    if (MEMBER_SELECT != methodSelect.getExpression().getKind()
        || IDENTIFIER
            != ((MemberSelectTree) methodSelect.getExpression()).getExpression().getKind()) {
      return Description.NO_MATCH;
    }
    IdentifierTree identifierTree =
        (IdentifierTree) ((MemberSelectTree) methodSelect.getExpression()).getExpression();
    return constructDescription(tree, state, constructFix(tree, identifierTree.getName()));
  }

  private Description constructDescription(
      LambdaExpressionTree tree, VisitorState state, SuggestedFix.Builder fixBuilder) {
    SuggestedFix fix = fixBuilder.build();
    return SuggestedFixes.compilesWithFix(
            fix, state, ImmutableList.of(), /* onlyInSameCompilationUnit= */ true)
        ? describeMatch(tree, fix)
        : Description.NO_MATCH;
  }

  private static SuggestedFix.Builder constructFix(LambdaExpressionTree tree, Object target) {
    return SuggestedFix.builder().replace(tree, target + ".class::isInstance");
  }
}
