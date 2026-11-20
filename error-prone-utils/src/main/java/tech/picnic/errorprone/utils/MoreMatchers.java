package tech.picnic.errorprone.utils;

import static com.google.errorprone.matchers.Matchers.typePredicateMatcher;

import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.suppliers.Supplier;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Type;

/**
 * A collection of general-purpose {@link Matcher}s.
 *
 * <p>These methods are additions to the ones found in {@link Matchers}.
 */
public final class MoreMatchers {
  private MoreMatchers() {}

  /**
   * Returns a {@link Matcher} that determines whether the type of a given {@link Tree} is a subtype
   * of the type returned by the specified {@link Supplier}.
   *
   * <p>This method differs from {@link Matchers#isSubtypeOf(Supplier)} in that it does not perform
   * type erasure.
   *
   * @param <T> The type of tree to match against.
   * @param type The {@link Supplier} that returns the type to match against.
   * @return A {@link Matcher} that matches trees with the specified type.
   */
  public static <T extends Tree> Matcher<T> isSubTypeOf(Supplier<Type> type) {
    return typePredicateMatcher(MoreTypePredicates.isSubTypeOf(type));
  }
}
