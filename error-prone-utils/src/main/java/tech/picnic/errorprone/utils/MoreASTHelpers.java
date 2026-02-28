package tech.picnic.errorprone.utils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import java.util.Optional;

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

  /**
   * Tells whether the given tree is of type {@link String}.
   *
   * @param tree The tree of interest.
   * @param state The {@link VisitorState} describing the context in which the given tree was found.
   * @return Whether the specified tree has the same type as {@link
   *     com.sun.tools.javac.code.Symtab#stringType}.
   */
  public static boolean isStringTyped(Tree tree, VisitorState state) {
    return ASTHelpers.isSameType(ASTHelpers.getType(tree), state.getSymtab().stringType, state);
  }

  /**
   * Returns the lower bound of a type if it has one, or the type itself if not. Correctly handles
   * wildcards and capture variables.
   *
   * <p>This method mirrors {@link ASTHelpers#getUpperBound(Type, Types)} for lower bounds.
   *
   * @param type The type to get the lower bound of.
   * @param types The {@link Types} instance to use for type operations.
   * @return The lower bound of the type, or the type itself if it has no lower bound.
   * @see ASTHelpers#getUpperBound(Type, Types)
   */
  public static Type getLowerBound(Type type, Types types) {
    if (type.hasTag(TypeTag.WILDCARD)) {
      return types.wildLowerBound(type);
    }
    if (type.hasTag(TypeTag.TYPEVAR) && ((Type.TypeVar) type).isCaptured()) {
      return types.cvarLowerBound(type);
    }
    return type;
  }
}
