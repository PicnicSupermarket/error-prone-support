package tech.picnic.errorprone.bugpatterns.util;

import com.google.common.collect.Iterables;
import com.google.errorprone.VisitorState;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;

public final class NestedTypesUtils {

  private NestedTypesUtils() {}

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
