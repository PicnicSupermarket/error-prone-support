package tech.picnic.errorprone.bugpatterns.util;

import com.google.common.collect.Iterables;
import com.google.errorprone.VisitorState;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;

/** Utility class that can be used to identify nesting of the same type. */
public final class NestedTypesUtils {
  private NestedTypesUtils() {}

  /**
   * Checks nesting of the same type.
   *
   * @param type Type we expect to be nested.
   * @param tree The AST node of interest.
   * @param state A {@link VisitorState} describing the context in which the given {@link Tree} is
   *     found.
   * @return {@code true} if the given node contains nested node of the same type.
   */
  public static boolean isSameTypeNested(Supplier<Type> type, Tree tree, VisitorState state) {
    Type expectedType = type.get(state);
    Type actualType = ASTHelpers.getType(tree);
    if (!ASTHelpers.isSubtype(actualType, expectedType, state)) {
      return false;
    }

    List<Type> typeArguments = actualType.getTypeArguments();
    return !typeArguments.isEmpty()
        && ASTHelpers.isSubtype(Iterables.getOnlyElement(typeArguments), expectedType, state);
  }
}
