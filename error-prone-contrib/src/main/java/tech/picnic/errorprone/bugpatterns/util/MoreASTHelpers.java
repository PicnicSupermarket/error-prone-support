package tech.picnic.errorprone.bugpatterns.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.util.Optional;

/**
 * A collection of helper methods for working with the AST.
 *
 * <p>These methods are additions to the ones found in {@link
 * com.google.errorprone.util.ASTHelpers}.
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
   * Returns the {@link MethodTree} from which the control flow would exit if there would be a
   * {@code return} statement at the given {@link VisitorState}'s current {@link
   * VisitorState#getPath() path}.
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
