package tech.picnic.errorprone.bugpatterns.util;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;

/**
 * A set of helper methods for working with the AST.
 *
 * <p>These helper methods are additions to the ones from {@link
 * com.google.errorprone.util.ASTHelpers}.
 */
public final class MoreASTHelpers {
  private MoreASTHelpers() {}

  /**
   * Finds methods with the given name in the enclosing class.
   *
   * @param methodName The method name to search for.
   * @param state A {@link VisitorState} describing the context in which the given {@link Tree} is
   *     to be found.
   * @return The {@link MethodTree}s of the methods with the given name in the enclosing class.
   */
  public static ImmutableList<MethodTree> findMethods(String methodName, VisitorState state) {
    return state.findEnclosing(ClassTree.class).getMembers().stream()
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast)
        .filter(method -> method.getName().contentEquals(methodName))
        .collect(toImmutableList());
  }

  /**
   * Determines if there are any methods with the given name in the enclosing class.
   *
   * @param methodName The method name to search for.
   * @param state A {@link VisitorState} describing the context in which the given {@link Tree} is
   *     to be found.
   * @return Whether there are any methods with the given name in the enclosing class.
   */
  public static boolean isMethodInEnclosingClass(String methodName, VisitorState state) {
    return !findMethods(methodName, state).isEmpty();
  }
}
