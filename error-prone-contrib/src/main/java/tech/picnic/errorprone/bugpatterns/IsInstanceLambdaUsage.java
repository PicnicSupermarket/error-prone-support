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
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Type;

/**
 * A {@link BugChecker} that flags lambda expressions that can be replaced with a method reference
 * of the form {@code T.class::isInstance}.
 */
// XXX: Consider folding this logic into the `MethodReferenceUsage` check of the
// `error-prone-experimental` module.
// XXX: This check and its tests are structurally nearly identical to `ClassCastLambdaUsage`. Unless
// folded into `MethodReferenceUsage`, consider merging the two.
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
    if (tree.getParameters().size() != 1
        || !(tree.getBody() instanceof InstanceOfTree instanceOf)) {
      return Description.NO_MATCH;
    }

    Type type = ASTHelpers.getType(instanceOf.getType());
    if (type == null) {
      /*
       * A record pattern such as `o instanceof R(var i)` does more than test the type of `o`: it also
       * matches the record's components against nested patterns. A class literal can't express that.
       * Such a pattern has no type operand, and `InstanceOfTree#getType` returns `null` for it.
       */
      return Description.NO_MATCH;
    }

    VariableTree param = Iterables.getOnlyElement(tree.getParameters());
    if (!ASTHelpers.getSymbol(param).equals(ASTHelpers.getSymbol(instanceOf.getExpression()))) {
      return Description.NO_MATCH;
    }

    /*
     * Class literals cannot express parameterized types, type variables or arrays thereof. Because
     * the `instanceof` operator only performs a runtime check against the erasure of its type
     * operand, the erased type can be used instead.
     */
    SuggestedFix.Builder fix = SuggestedFix.builder();
    String typeName = SuggestedFixes.qualifyType(state, fix, state.getTypes().erasure(type));
    return describeMatch(tree, fix.replace(tree, typeName + ".class::isInstance").build());
  }
}
