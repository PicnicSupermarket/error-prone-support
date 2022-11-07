package tech.picnic.errorprone.bugpatterns.util;

import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.isType;
import static tech.picnic.errorprone.bugpatterns.util.MoreMatchers.hasMetaAnnotation;

import com.google.common.collect.Iterables;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Type;
import java.util.Optional;
import javax.lang.model.type.TypeKind;

/**
 * A set of JUnit Jupiter-specific helper methods and {@link Matcher Matchers}.
 *
 * <p>These are additions to the ones from {@link com.google.errorprone.matchers.JUnitMatchers}.
 */
public final class MoreJUnitJupiterMatchers {
  /** Matches JUnit Jupiter test methods. */
  public static final MultiMatcher<MethodTree, AnnotationTree> TEST_METHOD =
      annotations(
          AT_LEAST_ONE,
          anyOf(
              isType("org.junit.jupiter.api.Test"),
              hasMetaAnnotation("org.junit.jupiter.api.TestTemplate")));

  /** Matches JUnit setup and teardown methods. */
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
  public static final Matcher<MethodTree> HAS_METHOD_SOURCE =
      allOf(annotations(AT_LEAST_ONE, isType("org.junit.jupiter.params.provider.MethodSource")));

  private MoreJUnitJupiterMatchers() {}

  /**
   * Extracts the name of the JUnit factory method from a {@link
   * org.junit.jupiter.params.provider.MethodSource} annotation.
   *
   * @param methodSourceAnnotation The {@link org.junit.jupiter.params.provider.MethodSource}
   *     annotation to extract a method name from.
   * @return The name of the factory method referred to in the annotation. Alternatively, {@link
   *     Optional#empty()} if there is more than one.
   */
  public static Optional<String> extractSingleFactoryMethodName(
      AnnotationTree methodSourceAnnotation) {
    ExpressionTree attributeExpression =
        ((AssignmentTree) Iterables.getOnlyElement(methodSourceAnnotation.getArguments()))
            .getExpression();
    Type attributeType = ASTHelpers.getType(attributeExpression);
    return attributeType.getKind() == TypeKind.ARRAY
        ? Optional.empty()
        : Optional.of(attributeType.stringValue());
  }
}
