package tech.picnic.errorprone.utils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * A collection of helper methods for working with the AST.
 *
 * <p>These methods are additions to the ones found in {@link ASTHelpers}.
 */
public final class MoreASTHelpers {
  private MoreASTHelpers() {}

  /**
   * Finds methods with the specified name in given the {@link VisitorState}'s current enclosing
   * class.
   *
   * @param methodName The method name to search for.
   * @param state The {@link VisitorState} from which to derive the enclosing class of interest.
   * @return The {@link MethodTree}s of the methods with the given name in the enclosing class.
   */
  public static ImmutableList<MethodTree> findMethods(CharSequence methodName, VisitorState state) {
    ClassTree clazz = state.findEnclosing(ClassTree.class);
    checkArgument(clazz != null, "Visited node is not enclosed by a class");
    return clazz.getMembers().stream()
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast)
        .filter(method -> method.getName().contentEquals(methodName))
        .collect(toImmutableList());
  }

  /**
   * Determines whether there are any methods with the specified name in given the {@link
   * VisitorState}'s current enclosing class.
   *
   * @param methodName The method name to search for.
   * @param state The {@link VisitorState} from which to derive the enclosing class of interest.
   * @return Whether there are any methods with the given name in the enclosing class.
   */
  public static boolean methodExistsInEnclosingClass(CharSequence methodName, VisitorState state) {
    return !findMethods(methodName, state).isEmpty();
  }

  /**
   * Returns the {@link MethodTree} from which control flow would exit if there would be a {@code
   * return} statement at the given {@link VisitorState}'s current {@link VisitorState#getPath()
   * path}.
   *
   * @param state The {@link VisitorState} from which to derive the AST location of interest.
   * @return A {@link MethodTree}, unless the {@link VisitorState}'s path does not point to an AST
   *     node located inside a method, or if the (hypothetical) {@code return} statement would exit
   *     a lambda expression instead.
   */
  public static Optional<MethodTree> findMethodExitedOnReturn(VisitorState state) {
    return Optional.ofNullable(state.findEnclosing(LambdaExpressionTree.class, MethodTree.class))
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast);
  }

  /**
   * Returns the result expressions of the given lambda expression.
   *
   * <p>If the lambda expression has an expression body, then this is the body itself. Otherwise
   * these are the expressions of the {@code return} statements contained in the block body,
   * excluding those that belong to nested classes and lambda expressions.
   *
   * @param tree The lambda expression of interest.
   * @return The lambda expression's result expressions, in source order and with any enclosing
   *     parentheses removed.
   */
  public static ImmutableList<ExpressionTree> findResultExpressions(LambdaExpressionTree tree) {
    return findBodyResultExpressions(tree.getBody());
  }

  /**
   * Returns the result expressions of the given method.
   *
   * <p>These are the expressions of the {@code return} statements contained in the method body,
   * excluding those that belong to nested classes and lambda expressions.
   *
   * @param tree The method of interest.
   * @return The method's result expressions, in source order and with any enclosing parentheses
   *     removed; empty if the method has no body.
   */
  public static ImmutableList<ExpressionTree> findResultExpressions(MethodTree tree) {
    return findBodyResultExpressions(tree.getBody());
  }

  // XXX: This pattern also occurs a few times inside Error Prone; contribute upstream.
  private static ImmutableList<ExpressionTree> findBodyResultExpressions(@Nullable Tree body) {
    if (body instanceof ExpressionTree expression) {
      return ImmutableList.of(ASTHelpers.stripParentheses(expression));
    }

    ImmutableList.Builder<ExpressionTree> results = ImmutableList.builder();
    new TreeScanner<@Nullable Void, @Nullable Void>() {
      @Override
      public @Nullable Void visitClass(ClassTree node, @Nullable Void unused) {
        return null;
      }

      @Override
      public @Nullable Void visitLambdaExpression(
          LambdaExpressionTree node, @Nullable Void unused) {
        return null;
      }

      @Override
      public @Nullable Void visitReturn(ReturnTree node, @Nullable Void unused) {
        ExpressionTree expression = node.getExpression();
        if (expression != null) {
          results.add(ASTHelpers.stripParentheses(expression));
        }
        return null;
      }
    }.scan(body, null);
    return results.build();
  }

  /**
   * Returns the branch expressions of the given expression.
   *
   * <p>For a conditional expression, these are the branch expressions of its second and third
   * operands; for any other expression, this is the expression itself.
   *
   * @param tree The expression of interest.
   * @return The expression's branch expressions, in source order and with any enclosing parentheses
   *     removed.
   */
  // XXX: Also support switch expressions.
  public static ImmutableList<ExpressionTree> findBranchExpressions(ExpressionTree tree) {
    ExpressionTree expression = ASTHelpers.stripParentheses(tree);
    return expression instanceof ConditionalExpressionTree conditional
        ? Stream.of(conditional.getTrueExpression(), conditional.getFalseExpression())
            .flatMap(operand -> findBranchExpressions(operand).stream())
            .collect(toImmutableList())
        : ImmutableList.of(expression);
  }

  /**
   * Tells whether the given trees are of the same type, after type erasure.
   *
   * @param treeA The first tree of interest.
   * @param treeB The second tree of interest.
   * @param state The {@link VisitorState} describing the context in which the given trees were
   *     found.
   * @return Whether the specified trees have the same erased types.
   */
  public static boolean areSameType(Tree treeA, Tree treeB, VisitorState state) {
    return ASTHelpers.isSameType(ASTHelpers.getType(treeA), ASTHelpers.getType(treeB), state);
  }
}
