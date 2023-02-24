package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.ALL;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.argument;
import static com.google.errorprone.matchers.Matchers.argumentCount;
import static com.google.errorprone.matchers.Matchers.classLiteral;
import static com.google.errorprone.matchers.Matchers.hasArguments;
import static com.google.errorprone.matchers.Matchers.isPrimitiveOrBoxedPrimitiveType;
import static com.google.errorprone.matchers.Matchers.isSameType;
import static com.google.errorprone.matchers.Matchers.methodHasParameters;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.matchers.Matchers.toType;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.bugpatterns.util.MoreJUnitMatchers.HAS_METHOD_SOURCE;
import static tech.picnic.errorprone.bugpatterns.util.MoreJUnitMatchers.getMethodSourceFactoryNames;

import com.google.auto.service.AutoService;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} that flags JUnit tests with a {@link
 * org.junit.jupiter.params.provider.MethodSource} annotation that can be replaced with an
 * equivalent {@link org.junit.jupiter.params.provider.ValueSource} annotation.
 */
// XXX: Where applicable, also flag `@MethodSource` annotations that reference multiple value
// factory methods (or that repeat the same value factory method multiple times).
// XXX: Support inlining of overloaded value factory methods .
// XXX: Support inlining of value factory methods referenced by multiple `@MethodSource`
// annotations.
// XXX: Support value factory return expressions of the form `Stream.of(a, b,
// c).map(Arguments::argument)`.
// XXX: Support simplification of test methods that accept additional injected parameters such as
// `TestInfo`; such parameters should be ignored for the purpose of this check.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Prefer `@ValueSource` over a `@MethodSource` where possible and reasonable",
    linkType = CUSTOM,
    link = BUG_PATTERNS_BASE_URL + "JUnitValueSource",
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class JUnitValueSource extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> SUPPORTED_VALUE_FACTORY_VALUES =
      anyOf(
          isArrayArgumentValueCandidate(),
          toType(
              MethodInvocationTree.class,
              allOf(
                  staticMethod()
                      .onClass("org.junit.jupiter.params.provider.Arguments")
                      .namedAnyOf("arguments", "of"),
                  argumentCount(1),
                  argument(0, isArrayArgumentValueCandidate()))));
  private static final Matcher<ExpressionTree> ARRAY_OF_SUPPORTED_SINGLE_VALUE_ARGUMENTS =
      isSingleDimensionArrayCreationWithAllElementsMatching(SUPPORTED_VALUE_FACTORY_VALUES);
  private static final Matcher<ExpressionTree> ENUMERATION_OF_SUPPORTED_SINGLE_VALUE_ARGUMENTS =
      toType(
          MethodInvocationTree.class,
          allOf(
              staticMethod()
                  .onClassAny(
                      Stream.class.getName(),
                      IntStream.class.getName(),
                      LongStream.class.getName(),
                      DoubleStream.class.getName(),
                      List.class.getName(),
                      Set.class.getName(),
                      "com.google.common.collect.ImmutableList",
                      "com.google.common.collect.ImmutableSet")
                  .named("of"),
              hasArguments(AT_LEAST_ONE, (tree, state) -> true),
              hasArguments(ALL, SUPPORTED_VALUE_FACTORY_VALUES)));
  private static final Matcher<MethodTree> IS_UNARY_METHOD_WITH_SUPPORTED_PARAMETER =
      methodHasParameters(
          anyOf(
              isPrimitiveOrBoxedPrimitiveType(),
              isSameType(String.class),
              isSameType(state -> state.getSymtab().classType)));

  /** Instantiates a new {@link JUnitValueSource} instance. */
  public JUnitValueSource() {}

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (!IS_UNARY_METHOD_WITH_SUPPORTED_PARAMETER.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Type parameterType = ASTHelpers.getType(Iterables.getOnlyElement(tree.getParameters()));

    return findMethodSourceAnnotation(tree, state)
        .flatMap(
            methodSourceAnnotation ->
                getSoleLocalFactoryName(methodSourceAnnotation, tree)
                    .filter(factory -> !hasSiblingReferencingValueFactory(tree, factory, state))
                    .flatMap(factory -> findSiblingWithName(tree, factory, state))
                    .flatMap(
                        factoryMethod ->
                            tryConstructValueSourceFix(
                                parameterType, methodSourceAnnotation, factoryMethod, state))
                    .map(fix -> describeMatch(methodSourceAnnotation, fix)))
        .orElse(Description.NO_MATCH);
  }

  /**
   * Returns the name of the value factory method pointed to by the given {@code @MethodSource}
   * annotation, if it (a) is the only one and (b) is a method in the same class as the annotated
   * method.
   */
  private static Optional<String> getSoleLocalFactoryName(
      AnnotationTree methodSourceAnnotation, MethodTree method) {
    return getElementIfSingleton(getMethodSourceFactoryNames(methodSourceAnnotation, method))
        .filter(name -> name.indexOf('#') < 0);
  }

  /**
   * Tells whether the given method has a sibling method in the same class that depends on the
   * specified value factory method.
   */
  private static boolean hasSiblingReferencingValueFactory(
      MethodTree tree, String valueFactory, VisitorState state) {
    return findMatchingSibling(tree, m -> hasValueFactory(m, valueFactory, state), state)
        .isPresent();
  }

  private static Optional<MethodTree> findSiblingWithName(
      MethodTree tree, String methodName, VisitorState state) {
    return findMatchingSibling(tree, m -> m.getName().contentEquals(methodName), state);
  }

  private static Optional<MethodTree> findMatchingSibling(
      MethodTree tree, Predicate<? super MethodTree> predicate, VisitorState state) {
    return state.findEnclosing(ClassTree.class).getMembers().stream()
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast)
        .filter(not(tree::equals))
        .filter(predicate)
        .findFirst();
  }

  private static boolean hasValueFactory(
      MethodTree tree, String valueFactoryMethodName, VisitorState state) {
    return findMethodSourceAnnotation(tree, state).stream()
        .anyMatch(
            annotation ->
                getMethodSourceFactoryNames(annotation, tree).contains(valueFactoryMethodName));
  }

  private static Optional<AnnotationTree> findMethodSourceAnnotation(
      MethodTree tree, VisitorState state) {
    return HAS_METHOD_SOURCE.multiMatchResult(tree, state).matchingNodes().stream().findFirst();
  }

  private static Optional<SuggestedFix> tryConstructValueSourceFix(
      Type parameterType,
      AnnotationTree methodSourceAnnotation,
      MethodTree valueFactoryMethod,
      VisitorState state) {
    return getSingleReturnExpression(valueFactoryMethod)
        .flatMap(expression -> tryExtractValueSourceAttributeValue(expression, state))
        .map(
            valueSourceAttributeValue ->
                SuggestedFix.builder()
                    .addImport("org.junit.jupiter.params.provider.ValueSource")
                    .replace(
                        methodSourceAnnotation,
                        String.format(
                            "@ValueSource(%s = %s)",
                            toValueSourceAttributeName(parameterType), valueSourceAttributeValue))
                    .delete(valueFactoryMethod)
                    .build());
  }

  // XXX: This pattern also occurs a few times inside Error Prone; contribute upstream.
  private static Optional<ExpressionTree> getSingleReturnExpression(MethodTree methodTree) {
    List<ExpressionTree> returnExpressions = new ArrayList<>();
    new TreeScanner<@Nullable Void, @Nullable Void>() {
      @Override
      public @Nullable Void visitClass(ClassTree node, @Nullable Void unused) {
        /* Ignore `return` statements inside anonymous/local classes. */
        return null;
      }

      @Override
      public @Nullable Void visitReturn(ReturnTree node, @Nullable Void unused) {
        returnExpressions.add(node.getExpression());
        return super.visitReturn(node, unused);
      }

      @Override
      public @Nullable Void visitLambdaExpression(
          LambdaExpressionTree node, @Nullable Void unused) {
        /* Ignore `return` statements inside lambda expressions. */
        return null;
      }
    }.scan(methodTree, null);

    return getElementIfSingleton(returnExpressions);
  }

  private static Optional<String> tryExtractValueSourceAttributeValue(
      ExpressionTree tree, VisitorState state) {
    List<? extends ExpressionTree> arguments;
    if (ENUMERATION_OF_SUPPORTED_SINGLE_VALUE_ARGUMENTS.matches(tree, state)) {
      arguments = ((MethodInvocationTree) tree).getArguments();
    } else if (ARRAY_OF_SUPPORTED_SINGLE_VALUE_ARGUMENTS.matches(tree, state)) {
      arguments = ((NewArrayTree) tree).getInitializers();
    } else {
      return Optional.empty();
    }

    /*
     * Join the values into a comma-separated string, unwrapping `Arguments` factory method
     * invocations if applicable.
     */
    return Optional.of(
            arguments.stream()
                .map(
                    arg ->
                        arg instanceof MethodInvocationTree
                            ? Iterables.getOnlyElement(((MethodInvocationTree) arg).getArguments())
                            : arg)
                .map(argument -> SourceCode.treeToString(argument, state))
                .collect(joining(", ")))
        .map(value -> arguments.size() > 1 ? String.format("{%s}", value) : value);
  }

  private static String toValueSourceAttributeName(Type type) {
    String typeString = type.tsym.name.toString();

    switch (typeString) {
      case "Class":
        return "classes";
      case "Character":
        return "chars";
      case "Integer":
        return "ints";
      default:
        return typeString.toLowerCase(Locale.ROOT) + 's';
    }
  }

  private static <T> Optional<T> getElementIfSingleton(Collection<T> collection) {
    return Optional.of(collection)
        .filter(elements -> elements.size() == 1)
        .map(Iterables::getOnlyElement);
  }

  private static Matcher<ExpressionTree> isSingleDimensionArrayCreationWithAllElementsMatching(
      Matcher<? super ExpressionTree> elementMatcher) {
    return (tree, state) -> {
      if (!(tree instanceof NewArrayTree)) {
        return false;
      }

      NewArrayTree newArray = (NewArrayTree) tree;
      return newArray.getDimensions().isEmpty()
          && !newArray.getInitializers().isEmpty()
          && newArray.getInitializers().stream()
              .allMatch(element -> elementMatcher.matches(element, state));
    };
  }

  private static Matcher<ExpressionTree> isArrayArgumentValueCandidate() {
    return anyOf(
        classLiteral((tree, state) -> true), (tree, state) -> ASTHelpers.constValue(tree) != null);
  }
}
