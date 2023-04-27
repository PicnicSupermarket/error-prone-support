package tech.picnic.errorprone.bugpatterns.util;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.isType;
import static java.util.Objects.requireNonNullElse;
import static tech.picnic.errorprone.bugpatterns.util.MoreMatchers.hasMetaAnnotation;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.matchers.AnnotationMatcherUtils;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import org.jspecify.annotations.Nullable;

/**
 * A collection of JUnit-specific helper methods and {@link Matcher}s.
 *
 * <p>These constants and methods are additions to the ones found in {@link
 * com.google.errorprone.matchers.JUnitMatchers}.
 */
public final class MoreJUnitMatchers {
  /** Matches JUnit Jupiter test methods. */
  public static final MultiMatcher<MethodTree, AnnotationTree> TEST_METHOD =
      annotations(
          AT_LEAST_ONE,
          anyOf(
              isType("org.junit.jupiter.api.Test"),
              hasMetaAnnotation("org.junit.jupiter.api.TestTemplate")));

  /** Matches JUnit Jupiter setup and teardown methods. */
  public static final MultiMatcher<MethodTree, AnnotationTree> SETUP_OR_TEARDOWN_METHOD =
      annotations(
          AT_LEAST_ONE,
          anyOf(
              isType("org.junit.jupiter.api.AfterAll"),
              isType("org.junit.jupiter.api.AfterEach"),
              isType("org.junit.jupiter.api.BeforeAll"),
              isType("org.junit.jupiter.api.BeforeEach")));

  /**
   * Matches methods that have a {@link org.junit.jupiter.params.provider.MethodSource} annotation.
   */
  public static final MultiMatcher<MethodTree, AnnotationTree> HAS_METHOD_SOURCE =
      annotations(AT_LEAST_ONE, isType("org.junit.jupiter.params.provider.MethodSource"));

  private MoreJUnitMatchers() {}

  /**
   * Returns the names of the JUnit value factory methods specified by the given {@link
   * org.junit.jupiter.params.provider.MethodSource} annotation.
   *
   * <p>This method differs from {@link #getMethodSourceFactoryDescriptors(AnnotationTree,
   * MethodTree)} in that it drops any parenthesized method parameter type enumerations. That is,
   * method descriptors such as {@code factoryMethod()} and {@code factoryMethod(java.lang.String)}
   * are both simplified to just {@code factoryMethod}. This also means that the returned method
   * names may not unambiguously reference a single value factory method; in such a case JUnit will
   * throw an error at runtime.
   *
   * @param methodSourceAnnotation The annotation from which to extract value factory method names.
   * @param method The method on which the annotation is located.
   * @return One or more value factory descriptors, in the order defined.
   * @see #getMethodSourceFactoryDescriptors(AnnotationTree, MethodTree)
   */
  // XXX: Drop this method in favour of `#getMethodSourceFactoryDescriptors`. That will require
  // callers to either explicitly drop information, or perform a more advanced analysis.
  public static ImmutableList<String> getMethodSourceFactoryNames(
      AnnotationTree methodSourceAnnotation, MethodTree method) {
    return getMethodSourceFactoryDescriptors(methodSourceAnnotation, method).stream()
        .map(
            descriptor -> {
              int index = descriptor.indexOf('(');
              return index < 0 ? descriptor : descriptor.substring(0, index);
            })
        .collect(toImmutableList());
  }

  /**
   * Returns the descriptors of the JUnit value factory methods specified by the given {@link
   * org.junit.jupiter.params.provider.MethodSource} annotation.
   *
   * @param methodSourceAnnotation The annotation from which to extract value factory method
   *     descriptors.
   * @param method The method on which the annotation is located.
   * @return One or more value factory descriptors, in the order defined.
   * @see #getMethodSourceFactoryNames(AnnotationTree, MethodTree)
   */
  // XXX: Rather than strings, have this method return instances of a value type capable of
  // resolving the value factory method pointed to.
  public static ImmutableList<String> getMethodSourceFactoryDescriptors(
      AnnotationTree methodSourceAnnotation, MethodTree method) {
    String methodName = method.getName().toString();
    ExpressionTree value = AnnotationMatcherUtils.getArgument(methodSourceAnnotation, "value");

    if (!(value instanceof NewArrayTree newArray)) {
      return ImmutableList.of(toMethodSourceFactoryDescriptor(value, methodName));
    }

    return newArray.getInitializers().stream()
        .map(name -> toMethodSourceFactoryDescriptor(name, methodName))
        .collect(toImmutableList());
  }

  private static String toMethodSourceFactoryDescriptor(
      @Nullable ExpressionTree tree, String annotatedMethodName) {
    return requireNonNullElse(
        Strings.emptyToNull(ASTHelpers.constValue(tree, String.class)), annotatedMethodName);
  }
}
