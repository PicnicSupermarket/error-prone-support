package tech.picnic.errorprone.bugpatterns.util;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import javax.lang.model.element.Name;

/** A set of helper methods for working with the AST. */
public final class MoreASTHelpers {
  private MoreASTHelpers() {}

  /**
   * Finds methods with the given name in the given class.
   *
   * @param enclosingClass The class to search in.
   * @param methodName The method name to search for.
   * @return The method trees of the methods with the given name in the class.
   */
  public static ImmutableList<MethodTree> findMethods(ClassTree enclosingClass, String methodName) {
    return enclosingClass.getMembers().stream()
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast)
        .filter(method -> method.getName().contentEquals(methodName))
        .collect(toImmutableList());
  }

  static boolean isMethodInEnclosingClass(String methodName, VisitorState state) {
    return state.findEnclosing(ClassTree.class).getMembers().stream()
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast)
        .map(MethodTree::getName)
        .map(Name::toString)
        .anyMatch(methodName::equals);
  }
}
