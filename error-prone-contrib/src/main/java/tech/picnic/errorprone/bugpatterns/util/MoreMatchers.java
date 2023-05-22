package tech.picnic.errorprone.bugpatterns.util;

import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.predicates.TypePredicate;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
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
   * Returns a {@link Matcher} that determines whether a given {@link AnnotationTree} has a
   * meta-annotation of the specified type.
   *
   * @param <T> The type of tree to match against.
   * @param annotationType The binary type name of the annotation (e.g.
   *     "org.jspecify.annotations.Nullable", or "some.package.OuterClassName$InnerClassName")
   * @return A {@link Matcher} that matches trees with the specified meta-annotation.
   */
  public static <T extends AnnotationTree> Matcher<T> hasMetaAnnotation(String annotationType) {
    TypePredicate typePredicate = hasAnnotation(annotationType);
    return (tree, state) -> {
      Symbol sym = ASTHelpers.getSymbol(tree);
      return sym != null && typePredicate.apply(sym.type, state);
    };
  }

  /**
   * Returns a {@link Matcher} that determines whether a given {@link ExpressionTree} has type
   * arguments.
   *
   * @param <T> The type of tree to match against.
   * @return A {@link Matcher} that matches trees with type arguments.
   */
  public static <T extends ExpressionTree> Matcher<T> hasTypeArguments() {
    return (tree, state) -> {
      switch (tree.getKind()) {
        case METHOD_INVOCATION:
          return !((MethodInvocationTree) tree).getTypeArguments().isEmpty();
        case NEW_CLASS:
          NewClassTree classTree = (NewClassTree) tree;
          if (!classTree.getTypeArguments().isEmpty()) {
            return true;
          }

          if (classTree.getIdentifier().getKind() != Tree.Kind.PARAMETERIZED_TYPE) {
            return false;
          }

          return !((ParameterizedTypeTree) classTree.getIdentifier()).getTypeArguments().isEmpty();
        default:
          return false;
      }
    };
  }

  // XXX: Consider moving to a `MoreTypePredicates` utility class.
  private static TypePredicate hasAnnotation(String annotationClassName) {
    return (type, state) -> ASTHelpers.hasAnnotation(type.tsym, annotationClassName, state);
  }
}
