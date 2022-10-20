package tech.picnic.errorprone.bugpatterns.util;

import static java.util.stream.Collectors.toCollection;

import com.google.errorprone.VisitorState;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * A set of helper methods which together define a DSL for defining {@link Type types}.
 *
 * <p>These methods are meant to be statically imported. Example usage:
 *
 * <pre>{@code
 * Supplier<Type> type =
 *     VisitorState.memoize(
 *         generic(
 *             type("reactor.core.publisher.Flux"),
 *             subOf(generic(type("org.reactivestreams.Publisher"), unbound()))));
 * }</pre>
 *
 * This statement produces a memoized supplier of the type {@code Flux<? extends Publisher<?>>}.
 */
public final class MoreTypes {
  private MoreTypes() {}

  /**
   * Creates a supplier of the type with the given fully qualified class name.
   *
   * <p>This method should only be used when building more complex types in combination with other
   * {@link MoreTypes} methods. In other cases prefer directly calling {@link
   * Suppliers#typeFromString(String)}.
   *
   * @param typeName The type of interest.
   * @return A supplier which returns the described type if available in the given state, and {@code
   *     null} otherwise.
   */
  public static Supplier<Type> type(String typeName) {
    return Suppliers.typeFromString(typeName);
  }

  /**
   * Creates a supplier of the described generic type.
   *
   * @param type The base type of interest.
   * @param typeArgs The desired type arguments.
   * @return A supplier which returns the described type if available in the given state, and {@code
   *     null} otherwise.
   */
  // XXX: The given `type` should be a generic type, so perhaps `withParams` would be a better
  // method name. But the DSL wouldn't look as nice that way.
  @SafeVarargs
  @SuppressWarnings("varargs")
  public static Supplier<Type> generic(Supplier<Type> type, Supplier<Type>... typeArgs) {
    return propagateNull(
        type,
        (state, baseType) -> {
          List<Type> params =
              Arrays.stream(typeArgs).map(s -> s.get(state)).collect(toCollection(ArrayList::new));
          if (params.stream().anyMatch(Objects::isNull)) {
            return null;
          }

          return state.getType(baseType, /* isArray= */ false, params);
        });
  }

  /**
   * Creates a raw (erased, non-generic) variant of the given type.
   *
   * @param type The base type of interest.
   * @return A supplier which returns the described type if available in the given state, and {@code
   *     null} otherwise.
   */
  public static Supplier<Type> raw(Supplier<Type> type) {
    return propagateNull(type, (state, baseType) -> baseType.tsym.erasure(state.getTypes()));
  }

  /**
   * Creates a {@code ? super T} wildcard type, with {@code T} bound to the given type.
   *
   * @param type The base type of interest.
   * @return A supplier which returns the described type if available in the given state, and {@code
   *     null} otherwise.
   */
  public static Supplier<Type> supOf(Supplier<Type> type) {
    return propagateNull(
        type,
        (state, baseType) ->
            new Type.WildcardType(baseType, BoundKind.SUPER, state.getSymtab().boundClass));
  }

  /**
   * Creates a {@code ? extends T} wildcard type, with {@code T} bound to the given type.
   *
   * @param type The base type of interest.
   * @return A supplier which returns the described type if available in the given state, and {@code
   *     null} otherwise.
   */
  public static Supplier<Type> subOf(Supplier<Type> type) {
    return propagateNull(
        type,
        (state, baseType) ->
            new Type.WildcardType(
                type.get(state), BoundKind.EXTENDS, state.getSymtab().boundClass));
  }

  /**
   * Creates an unbound wildcard type ({@code ?}).
   *
   * @return A supplier which returns the described type.
   */
  public static Supplier<Type> unbound() {
    return state ->
        new Type.WildcardType(
            state.getSymtab().objectType, BoundKind.UNBOUND, state.getSymtab().boundClass);
  }

  private static Supplier<Type> propagateNull(
      Supplier<Type> type, BiFunction<VisitorState, Type, Type> transformer) {
    return state ->
        Optional.ofNullable(type.get(state)).map(t -> transformer.apply(state, t)).orElse(null);
  }
}
