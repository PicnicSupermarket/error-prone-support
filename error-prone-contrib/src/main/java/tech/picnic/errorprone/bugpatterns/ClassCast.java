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
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.VariableTree;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags lambda expressions that can be replaced with a method reference
 * of the form {@code T.class::cast}, if applicable.
 */
// XXX: Consider folding this logic into the `MethodReferenceUsage` check of the
// `error-prone-experimental` module.
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Prefer `Class::cast` method reference over equivalent lambda expression, if applicable",
    link = BUG_PATTERNS_BASE_URL + "ClassCast",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class ClassCast extends BugChecker implements LambdaExpressionTreeMatcher {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link ClassCast} instance. */
  public ClassCast() {}

  @Override
  public Description matchLambdaExpression(LambdaExpressionTree tree, VisitorState state) {
    if (tree.getParameters().size() != 1 || !(tree.getBody() instanceof TypeCastTree typeCast)) {
      return Description.NO_MATCH;
    }

    String classCast = SourceCode.treeToString(typeCast.getType(), state);
    if (isGenericCastExpression(classCast)) {
      return Description.NO_MATCH;
    }

    VariableTree param = Iterables.getOnlyElement(tree.getParameters());
    if (!ASTHelpers.getSymbol(param).equals(ASTHelpers.getSymbol(typeCast.getExpression()))) {
      return Description.NO_MATCH;
    }

    return describeMatch(tree, SuggestedFix.replace(tree, classCast + ".class::cast"));
  }

  private static boolean isGenericCastExpression(String expression) {
    return expression.contains("<") && expression.contains(">");
  }
}
