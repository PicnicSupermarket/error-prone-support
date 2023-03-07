package tech.picnic.errorprone.bugpatterns.util;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;

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
    requireNonNull(clazz, "Visited node is not enclosed by a class");
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
}
