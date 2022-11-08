package tech.picnic.errorprone.bugpatterns.util;

import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.predicates.TypePredicate;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;

/**
 * A collection of methods to enhance the use of {@link Matcher}s.
 *
 * <p>These methods are additions to the ones from {@link Matchers}.
 */
public final class MoreMatchers {
  private MoreMatchers() {}

  /**
   * Determines whether an expression has a meta annotation of the given class name. This includes
   * annotations inherited from superclasses due to {@link java.lang.annotation.Inherited}.
   *
   * @param <T> The type of the expression tree.
   * @param annotationClass The binary class name of the annotation (e.g. "
   *     org.jspecify.nullness.Nullable", or "some.package.OuterClassName$InnerClassName")
   * @return A {@link Matcher} that matches expressions with the specified meta annotation.
   */
  public static <T extends Tree> Matcher<T> hasMetaAnnotation(String annotationClass) {
    TypePredicate typePredicate = hasAnnotation(annotationClass);
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
