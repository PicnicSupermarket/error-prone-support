package tech.picnic.errorprone.bugpatterns.util;

import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.predicates.TypePredicate;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;

/**
 * A collection of general-purpose {@link Matcher}s.
 *
 * <p>These methods are additions to the ones found in {@link Matchers}.
 */
public final class MoreMatchers {
  private MoreMatchers() {}

  /**
   * Returns a {@link Matcher} that determines whether a given tree has a meta annotation of the
   * specified type.
   *
   * <p>This includes annotations inherited from superclasses due to {@link
   * java.lang.annotation.Inherited}.
   *
   * @param <T> The type of tree to match against.
   * @param annotationType The binary type name of the annotation (e.g.
   *     "org.jspecify.annotations.Nullable", or "some.package.OuterClassName$InnerClassName")
   * @return A {@link Matcher} that matches trees with the specified meta annotation.
   */
  public static <T extends Tree> Matcher<T> hasMetaAnnotation(String annotationType) {
    TypePredicate typePredicate = hasAnnotation(annotationType);
    return (tree, state) -> {
      Symbol sym = ASTHelpers.getSymbol(tree);
      return sym != null && typePredicate.apply(sym.type, state);
    };
  }

  // XXX: Consider moving to a `MoreTypePredicates` utility class.
  private static TypePredicate hasAnnotation(String annotationClassName) {
    return (type, state) -> ASTHelpers.hasAnnotation(type.tsym, annotationClassName, state);
  }
}
