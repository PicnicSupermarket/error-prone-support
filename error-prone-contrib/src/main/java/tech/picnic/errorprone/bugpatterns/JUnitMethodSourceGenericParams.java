package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.utils.MoreJUnitMatchers.HAS_METHOD_SOURCE;
import static tech.picnic.errorprone.utils.MoreJUnitMatchers.findMethodSourceAnnotation;
import static tech.picnic.errorprone.utils.MoreJUnitMatchers.getMethodSourceFactoryNames;
import static tech.picnic.errorprone.utils.SourceCode.treeToString;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.picnic.errorprone.utils.MoreASTHelpers;

/**
 * A {@link BugChecker} that flags JUnit {@link ParameterizedTest parameterized tests} with a {@link
 * MethodSource} annotation that provides generic parameters with incompatible types.
 *
 * <p>JUnit ensures type compatibility, and an error is thrown if different types are provided from
 * the method source. However, when generics are used, Java's type erasure means that the specific
 * generic type information is not available at runtime. Consequently, JUnit cannot compile-check or
 * enforce type correctness for these parameters, leading to potential runtime errors that are
 * difficult to anticipate.
 *
 * <p>This bug checker only flags {@link Arguments} provided via supported collection and stream
 * factory methods, and will not flag those calculated at runtime using a map-reduce pattern, such
 * as:
 *
 * <pre>{@code
 * Stream.of(1, 2).map(Arguments::argument);
 * }</pre>
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Generic parameter used in the provided `@MethodSource` is not applicable with accepted types",
    linkType = CUSTOM,
    link = BUG_PATTERNS_BASE_URL + "JUnitMethodSourceGenericParams",
    severity = ERROR,
    tags = LIKELY_ERROR)
public final class JUnitMethodSourceGenericParams extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String ARGUMENT_SET_FACTORY = "argumentSet";
  private static final Matcher<ExpressionTree> ARGUMENTS_FACTORY_METHODS =
      staticMethod()
          .onClass("org.junit.jupiter.params.provider.Arguments")
          .namedAnyOf("of", "arguments", ARGUMENT_SET_FACTORY);
  private static final Matcher<ExpressionTree> SUPPORTED_METHOD_SOURCE_PROVIDERS =
      staticMethod()
          .onDescendantOfAny(
              DoubleStream.class.getCanonicalName(),
              EnumSet.class.getCanonicalName(),
              ImmutableList.class.getCanonicalName(),
              ImmutableSet.class.getCanonicalName(),
              IntStream.class.getCanonicalName(),
              List.class.getCanonicalName(),
              LongStream.class.getCanonicalName(),
              Set.class.getCanonicalName(),
              Stream.class.getCanonicalName())
          .named("of");

  /** Instantiates a new {@link JUnitMethodSourceGenericParams} instance. */
  public JUnitMethodSourceGenericParams() {}

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (!HAS_METHOD_SOURCE.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    /* The method source annotation is always found because of the earlier matcher. */
    AnnotationTree methodSourceAnnotation = findMethodSourceAnnotation(tree, state).orElseThrow();
    ImmutableList<MethodTree> offendingMethodSourceProviders =
        findOffendingMethodSourceProviders(methodSourceAnnotation, tree, state);
    if (offendingMethodSourceProviders.isEmpty()) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .setMessage(
            "One or more generic parameters used in the following `@MethodSource` provider(s) don't match accepted types: %s"
                .formatted(
                    offendingMethodSourceProviders.stream()
                        .map(MethodTree::getName)
                        .collect(joining(", "))))
        .build();
  }

  private static ImmutableList<MethodTree> findOffendingMethodSourceProviders(
      AnnotationTree annotation, MethodTree testMethodTree, VisitorState state) {
    return getMethodSourceFactoryNames(annotation, testMethodTree).stream()
        .map(factoryName -> findMethodTreeFromEnclosingClass(factoryName, state))
        .flatMap(Optional::stream)
        .filter(method -> isOffendingMethodSourceProvider(method, testMethodTree, state))
        .collect(toImmutableList());
  }

  /**
   * Returns whether the given method source provider has any {@link Arguments} call whose generic
   * parameters are incompatible with the test method's parameters.
   */
  private static boolean isOffendingMethodSourceProvider(
      MethodTree methodSourceMethodTree, MethodTree testMethodTree, VisitorState state) {
    ImmutableList<Type> testMethodTypes = toTypes(testMethodTree.getParameters());
    return findPotentialOffendingProviders(methodSourceMethodTree, state).stream()
        .anyMatch(arguments -> !hasApplicableTypes(testMethodTypes, toTypes(arguments), state));
  }

  /**
   * Returns {@code true} if all parameterized method source provider argument types are compatible
   * with the accepted test method parameters.
   */
  private static boolean hasApplicableTypes(
      ImmutableList<Type> testMethodTypes,
      ImmutableList<Type> providerArgumentTypes,
      VisitorState state) {
    Types types = state.getTypes();
    return Streams.zip(
            testMethodTypes.stream(),
            providerArgumentTypes.stream(),
            (testType, providerType) ->
                allParameterizedArgumentsHaveApplicableTypes(testType, providerType, types))
        .allMatch(applicable -> applicable);
  }

  /**
   * Builds the list of argument lists by traversing the call hierarchy of the method source
   * providers.
   *
   * @implNote All traversed methods are assumed to be within the same class as the test method.
   */
  private static ImmutableList<ImmutableList<? extends ExpressionTree>>
      findPotentialOffendingProviders(Tree tree, VisitorState state) {
    return switch (tree) {
      case MethodTree methodTree ->
          collectReturnExpressions(methodTree).stream()
              .flatMap(
                  returnExpression ->
                      findPotentialOffendingProviders(returnExpression, state).stream())
              .collect(toImmutableList());
      case MethodInvocationTree methodInvocation -> {
        Optional<MethodTree> referencedMethod =
            findMethodTreeFromEnclosingClass(
                treeToString(methodInvocation.getMethodSelect(), state), state);
        if (referencedMethod.isPresent()) {
          yield findPotentialOffendingProviders(referencedMethod.orElseThrow(), state);
        }
        // XXX: The `referencedMethod.isPresent()` guard above is hard to mutation-test: Error
        // Prone's infrastructure catches the `NoSuchElementException` thrown by `orElseThrow()`
        // when the condition is mutated to always-true, so the test does not observe the failure.
        if (SUPPORTED_METHOD_SOURCE_PROVIDERS.matches(methodInvocation, state)) {
          yield collectPotentialOffendingProviders(methodInvocation.getArguments(), state);
        }
        // Otherwise, the method source provider is not supported. Ignore it as in JUnit this will
        // be checked compile-time from `MethodArgumentsProvider#isFactoryMethod`.
        yield ImmutableList.of();
      }
      case NewArrayTree newArrayTree -> {
        List<? extends ExpressionTree> arrayInitializers = newArrayTree.getInitializers();
        yield arrayInitializers == null
            ? ImmutableList.of()
            : collectPotentialOffendingProviders(arrayInitializers, state);
      }
      default -> ImmutableList.of();
    };
  }

  private static ImmutableList<ImmutableList<? extends ExpressionTree>>
      collectPotentialOffendingProviders(
          List<? extends ExpressionTree> expressions, VisitorState state) {
    return expressions.stream()
        .filter(e -> ARGUMENTS_FACTORY_METHODS.matches(e, state))
        .filter(MethodInvocationTree.class::isInstance)
        .map(MethodInvocationTree.class::cast)
        .map(invocation -> potentialOffendingProvider(invocation, state))
        .collect(toImmutableList());
  }

  private static ImmutableList<? extends ExpressionTree> potentialOffendingProvider(
      MethodInvocationTree argumentMethodInvocation, VisitorState state) {
    // Skip the first argument when the factory is `argumentSet(...)`, as it's the argument set
    // name rather than a test argument.
    int skip =
        treeToString(argumentMethodInvocation.getMethodSelect(), state)
                .contentEquals(ARGUMENT_SET_FACTORY)
            ? 1
            : 0;
    return argumentMethodInvocation.getArguments().stream().skip(skip).collect(toImmutableList());
  }

  /**
   * Determines whether all parameterized arguments have types that are compatible with the types of
   * the corresponding parameters in the test method.
   */
  private static boolean allParameterizedArgumentsHaveApplicableTypes(
      Type testMethodParameterType, Type methodSourceProviderType, Types types) {
    if (!testMethodParameterType.isParameterized() || !methodSourceProviderType.isParameterized()) {
      // Skip non-parameterized arguments, as any type incompatibility will be flagged by JUnit at
      // runtime.
      // XXX: This guard is technically redundant: `getTypeArguments()` returns an empty list for
      // non-parameterized types, and `Streams.zip` of empty lists yields an empty stream for which
      // `allMatch` trivially returns `true`. It is kept to make the intent explicit.
      return true;
    }

    List<Type> testMethodParameterTypeArguments = testMethodParameterType.getTypeArguments();
    List<Type> methodSourceProviderTypeArguments = methodSourceProviderType.getTypeArguments();

    if (testMethodParameterTypeArguments.size() > methodSourceProviderTypeArguments.size()) {
      // Skip the case if the method source provides fewer arguments, as these will be flagged by
      // JUnit at runtime.
      return true;
    }

    return Streams.zip(
            testMethodParameterTypeArguments.stream(),
            methodSourceProviderTypeArguments.stream(),
            (testArg, providerArg) -> isApplicableTypeArgument(testArg, providerArg, types))
        .allMatch(applicable -> applicable);
  }

  private static boolean isApplicableTypeArgument(
      Type testMethodArgumentTypeArgument,
      Type methodSourceProviderArgumentTypesArgument,
      Types types) {
    if (testMethodArgumentTypeArgument.isExtendsBound()) {
      Type upperBoundType = ASTHelpers.getUpperBound(testMethodArgumentTypeArgument, types);
      // We assume the user has ensured type compatibility for casting a subtype to the
      // super type. This ensures we can pass an empty `ImmutableCollection.of()` (effectively a
      // `ImmutableCollection<Object>`) to a `ImmutableCollection<Integer>` parameter.
      return isSuperTypeOrSubtype(methodSourceProviderArgumentTypesArgument, upperBoundType, types);
    }
    if (testMethodArgumentTypeArgument.isSuperBound()) {
      Type lowerBoundType = MoreASTHelpers.getLowerBound(testMethodArgumentTypeArgument, types);
      return types.isSuperType(methodSourceProviderArgumentTypesArgument, lowerBoundType);
    }
    return testMethodArgumentTypeArgument.isUnbound()
        || isSuperTypeOrSubtype(
            methodSourceProviderArgumentTypesArgument, testMethodArgumentTypeArgument, types);
  }

  private static Optional<MethodTree> findMethodTreeFromEnclosingClass(
      String factoryReference, VisitorState state) {
    ImmutableList.Builder<MethodTree> methods = ImmutableList.builder();
    new TreeScanner<@Nullable Void, @Nullable Void>() {
      @Override
      public @Nullable Void visitMethod(MethodTree node, @Nullable Void unused) {
        if (hasMethodName(factoryReference, node)) {
          methods.add(node);
        }
        return super.visitMethod(node, null);
      }
    }.scan(requireNonNull(state.findEnclosing(ClassTree.class), "No class enclosing method"), null);
    return methods.build().stream().findFirst();
  }

  private static ImmutableList<ExpressionTree> collectReturnExpressions(Tree methodTree) {
    ImmutableList.Builder<ExpressionTree> returnExpressions = ImmutableList.builder();
    new TreeScanner<@Nullable Void, @Nullable Void>() {
      @Override
      public @Nullable Void visitReturn(ReturnTree node, @Nullable Void unused) {
        returnExpressions.add(node.getExpression());
        return super.visitReturn(node, null);
      }
    }.scan(methodTree, null);

    return returnExpressions.build();
  }

  /**
   * Returns whether {@code method}'s name matches the trailing portion of {@code factoryReference}.
   *
   * <p>{@code factoryReference} can be a bare method name (e.g. {@code "foo"}) or a fully qualified
   * one (e.g. {@code "com.example.Bar#foo"}); the substring after the last {@code #} is taken as
   * the method name. {@link String#lastIndexOf(int)} returns {@code -1} when the character is
   * absent, making the call a no-op for bare names.
   */
  private static boolean hasMethodName(String factoryReference, MethodTree method) {
    return method
        .getName()
        .contentEquals(factoryReference.substring(factoryReference.lastIndexOf('#') + 1));
  }

  private static boolean isSuperTypeOrSubtype(Type baseType, Type type, Types types) {
    return types.isSuperType(baseType, type) || types.isSubtype(baseType, type);
  }

  private static ImmutableList<Type> toTypes(List<? extends Tree> parameters) {
    return parameters.stream().map(ASTHelpers::getType).collect(toImmutableList());
  }
}
