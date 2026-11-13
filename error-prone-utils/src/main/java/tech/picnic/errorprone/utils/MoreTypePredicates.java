package tech.picnic.errorprone.utils;

import com.google.errorprone.VisitorState;
import com.google.errorprone.predicates.TypePredicate;
import com.google.errorprone.predicates.TypePredicates;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.util.ASTHelpers;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;

/**
 * A collection of general-purpose {@link TypePredicate}s.
 *
 * <p>These methods are additions to the ones found in {@link TypePredicates}.
 */
public final class MoreTypePredicates {
  private MoreTypePredicates() {}

  /**
   * Returns a {@link TypePredicate} that matches subtypes of the type returned by the specified
   * {@link Supplier}.
   *
   * @implNote This method does not use {@link ASTHelpers#isSubtype(Type, Type, VisitorState)}, as
   *     that method performs type erasure.
   * @param bound The {@link Supplier} that returns the type to match against.
   * @return A {@link TypePredicate} that matches appropriate subtypes.
   */
  public static TypePredicate isSubTypeOf(Supplier<Type> bound) {
    Supplier<Type> memoizedType = VisitorState.memoize(bound);
    return (type, state) -> {
      Type boundType = memoizedType.get(state);
      return boundType != null && state.getTypes().isSubtype(type, boundType);
    };
  }

  /**
   * Returns a {@link TypePredicate} that matches generic types with a type parameter that matches
   * the specified {@link TypePredicate} at the indicated index.
   *
   * @param index The index of the type parameter to match against.
   * @param predicate The {@link TypePredicate} to match against the type parameter.
   * @return A {@link TypePredicate} that matches appropriate generic types.
   */
  public static TypePredicate hasTypeParameter(int index, TypePredicate predicate) {
    return (type, state) -> {
      List<Type> typeArguments = type.getTypeArguments();
      return typeArguments.size() > index && predicate.apply(typeArguments.get(index), state);
    };
  }
}
