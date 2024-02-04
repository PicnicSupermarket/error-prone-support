package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.LambdaExpressionTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags lambda expressions that can be replaced with a method reference
 * of the form {@code T.class::isInstance}.
 */
// XXX: Consider folding this logic into the `MethodReferenceUsage` check of the
// `error-prone-experimental` module.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Prefer `Class::isInstance` method reference over equivalent lambda expression",
    link = BUG_PATTERNS_BASE_URL + "IsInstanceLambdaUsage",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class IsInstanceLambdaUsage extends BugChecker implements LambdaExpressionTreeMatcher {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link IsInstanceLambdaUsage} instance. */
  public IsInstanceLambdaUsage() {}

  @Override
  public Description matchLambdaExpression(LambdaExpressionTree tree, VisitorState state) {
    if (tree.getParameters().size() != 1 || tree.getBody().getKind() != Kind.INSTANCE_OF) {
      return Description.NO_MATCH;
    }

    VariableTree param = Iterables.getOnlyElement(tree.getParameters());
    InstanceOfTree instanceOf = (InstanceOfTree) tree.getBody();
    if (!ASTHelpers.getSymbol(param).equals(ASTHelpers.getSymbol(instanceOf.getExpression()))) {
      return Description.NO_MATCH;
    }

    return describeMatch(
        tree,
        SuggestedFix.replace(
            tree, SourceCode.treeToString(instanceOf.getType(), state) + ".class::isInstance"));
  }
}
