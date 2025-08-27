package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static java.util.Objects.requireNonNull;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.utils.MoreJUnitMatchers.HAS_METHOD_SOURCE;
import static tech.picnic.errorprone.utils.MoreJUnitMatchers.findMethodSourceAnnotation;
import static tech.picnic.errorprone.utils.MoreJUnitMatchers.getMethodSourceFactoryNames;
import static tech.picnic.errorprone.utils.SourceCode.treeToString;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.lang.model.element.Name;
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
 * <p>This bug checker only flags the {@link Arguments} provided in the {@code
 * JUnitMethodSourceGenericParams#SUPPORTED_METHOD_SOURCE_PROVIDERS supported method source
 * providers}, and will not flag those calculated at runtime using a map-reduce pattern, such as:
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
  private static final String ARGUMENT_SET = "argumentSet";
  private static final Matcher<ExpressionTree> ARGUMENTS_FACTORY_METHODS =
      staticMethod()
          .onClass("org.junit.jupiter.params.provider.Arguments")
          .namedAnyOf("of", "arguments", ARGUMENT_SET);
  private static final Matcher<ExpressionTree> SUPPORTED_METHOD_SOURCE_PROVIDERS =
      staticMethod()
          .onDescendantOfAny(
              List.class.getCanonicalName(),
              ImmutableList.class.getCanonicalName(),
              ImmutableSet.class.getCanonicalName(),
              Set.class.getCanonicalName(),
              EnumSet.class.getCanonicalName(),
              IntStream.class.getCanonicalName(),
              LongStream.class.getCanonicalName(),
              DoubleStream.class.getCanonicalName(),
              Stream.class.getCanonicalName())
          .named("of");

  /** Instantiates a new {@link JUnitMethodSourceGenericParams} instance. */
  public JUnitMethodSourceGenericParams() {}

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (!HAS_METHOD_SOURCE.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    // In practice, the method source annotation is always found because of the earlier matcher.
    AnnotationTree methodSourceAnnotation = findMethodSourceAnnotation(tree, state).orElseThrow();
    ImmutableList<Optional<MethodTree>> offendingMethodSourceProviders =
        getOffendingMethodSourceProviders(methodSourceAnnotation, tree, state);
    if (offendingMethodSourceProviders.stream().noneMatch(Optional::isPresent)) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .setMessage(
            String.format(
                "One or more generic parameters used in the following @MethodSource provider(s) don't match accepted types: %s",
                getMethodNames(offendingMethodSourceProviders)))
        .build();
  }

  private static ImmutableList<Optional<MethodTree>> getOffendingMethodSourceProviders(
      AnnotationTree annotation, MethodTree testMethodTree, VisitorState state) {
    return getMethodSourceFactoryNames(annotation, testMethodTree).stream()
        .map(methodFactoryName -> findMethodTreeFromEnclosingClass(methodFactoryName, state))
        .flatMap(Optional::stream)
        .map(
            methodSourceMethodTree ->
                findOffendingMethodSourceProvider(methodSourceMethodTree, testMethodTree, state))
        .collect(toImmutableList());
  }

  /**
   * Returns an optional of the methodSourceMethodTree if it has incompatible generic parameters, if
   * any.
   */
  private static Optional<MethodTree> findOffendingMethodSourceProvider(
      MethodTree methodSourceMethodTree, MethodTree testMethodTree, VisitorState state) {
    List<PotentialOffendingArgumentsProvider> potentialOffendingProviders = new ArrayList<>();

    buildPotentialOffendingProviders(methodSourceMethodTree, potentialOffendingProviders, state);

    ImmutableList<Type> testMethodTypes = getTypes(testMethodTree.getParameters());
    for (PotentialOffendingArgumentsProvider potentialOffendingProvider :
        potentialOffendingProviders) {
      ImmutableList<Type> providerArgumentTypes = getTypes(potentialOffendingProvider.arguments());
      if (!hasApplicableTypes(testMethodTypes, providerArgumentTypes, state)) {
        return Optional.of(methodSourceMethodTree);
      }
    }

    return Optional.empty();
  }

  /**
   * Returns true iff all parameterised method source provider argument types are compatible with
   * the accepted test method parameters.
   */
  private static boolean hasApplicableTypes(
      ImmutableList<Type> testMethodTypes,
      ImmutableList<Type> providerArgumentTypes,
      VisitorState state) {
    for (int i = 0; i < testMethodTypes.size(); i++) {
      Type testMethodParameterType = testMethodTypes.get(i);
      Type methodSourceProviderType = providerArgumentTypes.get(i);

      if (!allParameterisedArgumentsHaveApplicableTypes(
          testMethodParameterType, methodSourceProviderType, state.getTypes())) {
        return false;
      }
    }
    return true;
  }

  /**
   * Builds the list of {@link PotentialOffendingArgumentsProvider} by traversing the call hierarchy
   * of the method source providers.
   *
   * @implNote All traversed methods are assumed to be within the same class as the test method.
   */
  private static void buildPotentialOffendingProviders(
      Tree tree,
      List<PotentialOffendingArgumentsProvider> potentialOffendingProviders,
      VisitorState state) {
    if (tree instanceof MethodTree methodTree) {
      for (ExpressionTree returnExpression : getReturnExpressions(methodTree)) {
        buildPotentialOffendingProviders(returnExpression, potentialOffendingProviders, state);
      }
    } else if (tree instanceof MethodInvocationTree methodInvocationTree) {
      Optional<MethodTree> methodTree =
          findMethodTreeFromEnclosingClass(
              treeToString(methodInvocationTree.getMethodSelect(), state), state);

      if (methodTree.isPresent()) {
        // Method is from the call hierarchy, within the same class.
        buildPotentialOffendingProviders(
            methodTree.orElseThrow(), potentialOffendingProviders, state);
      } else {
        if (SUPPORTED_METHOD_SOURCE_PROVIDERS.matches(methodInvocationTree, state)) {
          getPotentialOffendingProvider(
              potentialOffendingProviders, methodInvocationTree.getArguments(), state);
        }
        // Else, the method source provider is not supported. Ignore it as JUnit this will be
        // compile-time checked from MethodArgumentsProvider#isFactoryMethod.
      }
    } else if (tree instanceof NewArrayTree newArrayTree) {
      List<? extends ExpressionTree> arrayInitializers = newArrayTree.getInitializers();
      if (arrayInitializers != null) {
        getPotentialOffendingProvider(potentialOffendingProviders, arrayInitializers, state);
      }
    }
  }

  private static void getPotentialOffendingProvider(
      List<PotentialOffendingArgumentsProvider> potentialOffendingProviders,
      List<? extends ExpressionTree> expressions,
      VisitorState state) {
    for (ExpressionTree potentialArgumentExpression : expressions) {
      if (ARGUMENTS_FACTORY_METHODS.matches(potentialArgumentExpression, state)
          && potentialArgumentExpression
              instanceof MethodInvocationTree argumentMethodInvocationTree) {
        addPotentialOffendingProvider(
            potentialOffendingProviders, state, argumentMethodInvocationTree);
      }
    }
  }

  private static void addPotentialOffendingProvider(
      List<PotentialOffendingArgumentsProvider> potentialOffendingProviders,
      VisitorState state,
      MethodInvocationTree argumentMethodInvocationTree) {
    List<? extends ExpressionTree> arguments = argumentMethodInvocationTree.getArguments();
    if (treeToString(argumentMethodInvocationTree.getMethodSelect(), state)
        .contentEquals(ARGUMENT_SET)) {
      // We skip the first argument, because it's the argument set name.
      potentialOffendingProviders.add(
          new PotentialOffendingArgumentsProvider(
              arguments.stream().skip(1).collect(toImmutableList())));
    } else {
      potentialOffendingProviders.add(
          new PotentialOffendingArgumentsProvider(ImmutableList.copyOf(arguments)));
    }
  }

  /**
   * Determines whether all parameterised arguments have types that are compatible with the types of
   * the corresponding parameters in the test method.
   */
  private static boolean allParameterisedArgumentsHaveApplicableTypes(
      Type testMethodParameterType, Type methodSourceProviderType, Types types) {
    if (!testMethodParameterType.isParameterized() || !methodSourceProviderType.isParameterized()) {
      // Skip non-parameterised arguments, as any type incompatibility will be flagged by JUnit at
      // runtime.
      return true;
    }

    List<Type> testMethodParameterTypeArguments = testMethodParameterType.getTypeArguments();
    List<Type> methodSourceProviderTypeArguments = methodSourceProviderType.getTypeArguments();

    if (testMethodParameterTypeArguments.size() > methodSourceProviderTypeArguments.size()) {
      // Skip the case if the method source provides fewer arguments, as these will be flagged by
      // JUnit at runtime.
      return true;
    }

    for (int i = 0; i < testMethodParameterTypeArguments.size(); i++) {
      Type methodSourceProviderArgumentTypesArgument = methodSourceProviderTypeArguments.get(i);
      Type testMethodArgumentTypeArgument = testMethodParameterTypeArguments.get(i);

      if (testMethodArgumentTypeArgument.isExtendsBound()) {
        Type upperBoundType = ASTHelpers.getUpperBound(testMethodArgumentTypeArgument, types);

        //  Here, we assume the user has ensured type compatibility for casting a subtype to the
        // super type. This ensures we can pass an empty ImmutableCollection.of() (effectively a
        // ImmutableCollection<Object>) to a ImmutableCollection<Integer> parameter.
        if (isSuperTypeOrSubtype(
            methodSourceProviderArgumentTypesArgument, upperBoundType, types)) {
          return true;
        }
      } else if (testMethodArgumentTypeArgument.isSuperBound()) {
        Type lowerBoundType = MoreASTHelpers.getLowerBound(testMethodArgumentTypeArgument, types);
        if (types.isSuperType(methodSourceProviderArgumentTypesArgument, lowerBoundType)) {
          return true;
        }
      } else if (testMethodArgumentTypeArgument.isUnbound()
          || isSuperTypeOrSubtype(
              methodSourceProviderArgumentTypesArgument, testMethodArgumentTypeArgument, types)) {
        return true;
      }
    }

    return false;
  }

  private static Optional<MethodTree> findMethodTreeFromEnclosingClass(
      String methodName, VisitorState state) {
    return requireNonNull(state.findEnclosing(ClassTree.class), "No class enclosing method")
        .getMembers()
        .stream()
        .flatMap(JUnitMethodSourceGenericParams::findMembers)
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast)
        .filter(method -> findMethodByName(methodName, method))
        .findFirst();
  }

  private static ImmutableList<String> getMethodNames(
      ImmutableList<Optional<MethodTree>> offendingMethodSourceProviders) {
    return offendingMethodSourceProviders.stream()
        .flatMap(Optional::stream)
        .map(MethodTree::getName)
        .map(Name::toString)
        .collect(toImmutableList());
  }

  private static List<ExpressionTree> getReturnExpressions(Tree methodTree) {
    List<ExpressionTree> returnExpressions = new ArrayList<>();
    new TreeScanner<@Nullable Void, @Nullable Void>() {
      @Override
      public @Nullable Void visitReturn(ReturnTree node, @Nullable Void unused) {
        returnExpressions.add(node.getExpression());
        return super.visitReturn(node, null);
      }
    }.scan(methodTree, null);

    return returnExpressions;
  }

  private static Stream<? extends Tree> findMembers(Tree member) {
    if (member instanceof ClassTree classTree) {
      return classTree.getMembers().stream().flatMap(JUnitMethodSourceGenericParams::findMembers);
    } else {
      return Stream.of(member);
    }
  }

  private static boolean findMethodByName(String methodName, MethodTree method) {
    if (methodName.contains("#")) {
      return method.getName().contentEquals(methodName.substring(methodName.lastIndexOf("#") + 1));
    } else {
      return method.getName().toString().contentEquals(methodName);
    }
  }

  private static boolean isSuperTypeOrSubtype(Type baseType, Type type, Types types) {
    return types.isSuperType(baseType, type) || types.isSubtype(baseType, type);
  }

  private static ImmutableList<Type> getTypes(List<? extends Tree> parameters) {
    return parameters.stream().map(ASTHelpers::getType).collect(toImmutableList());
  }

  /** A presentation of a potential offending {@link Arguments} object. */
  private record PotentialOffendingArgumentsProvider(
      ImmutableList<? extends ExpressionTree> arguments) {}
}
