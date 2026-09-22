package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.CONCURRENCY;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static com.google.errorprone.suppliers.Suppliers.JAVA_LANG_BOOLEAN_TYPE;
import static java.util.Objects.requireNonNull;
import static javax.lang.model.element.ElementKind.ENUM;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MemberReferenceTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCMemberReference;
import java.util.function.Function;
import reactor.core.publisher.Flux;

/**
 * A {@link BugChecker} that flags {@link Flux#groupBy(Function)} usages with a statically unbounded
 * key space.
 *
 * <p>The groups emitted by {@link Flux#groupBy(Function)} are live views of the source and must be
 * drained downstream. If the number of groups exceeds the downstream subscription concurrency, then
 * backpressure prevents both the groups and their source from making progress, resulting in a
 * deadlock.
 *
 * <p>Boolean and enum keys are accepted, as their cardinality is statically bounded. For any other
 * key type, make sure that all groups are consumed concurrently, and suppress this check.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        """
        `Flux#groupBy` may deadlock if the number of groups exceeds the downstream subscription \
        concurrency; please use a statically bounded key type, or make sure that all groups are \
        consumed concurrently""",
    link = BUG_PATTERNS_BASE_URL + "FluxGroupByUsage",
    linkType = CUSTOM,
    severity = ERROR,
    tags = {CONCURRENCY, LIKELY_ERROR})
public final class FluxGroupByUsage extends BugChecker
    implements MethodInvocationTreeMatcher, MemberReferenceTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String FLUX = "reactor.core.publisher.Flux";
  private static final String FUNCTION = Function.class.getCanonicalName();
  private static final Supplier<Type> FUNCTION_TYPE = Suppliers.typeFromString(FUNCTION);
  /*
   * Reactor's `Flux#groupBy` overloads. These are enumerated explicitly so that identically named
   * methods declared by `Flux` subtypes are not matched, and so that the presence of a key mapper
   * argument is guaranteed.
   */
  private static final Matcher<ExpressionTree> FLUX_GROUP_BY =
      anyOf(
          instanceMethod().onDescendantOf(FLUX).named("groupBy").withParameters(FUNCTION),
          instanceMethod().onDescendantOf(FLUX).named("groupBy").withParameters(FUNCTION, "int"),
          instanceMethod().onDescendantOf(FLUX).named("groupBy").withParameters(FUNCTION, FUNCTION),
          instanceMethod()
              .onDescendantOf(FLUX)
              .named("groupBy")
              .withParameters(FUNCTION, FUNCTION, "int"));

  /** Instantiates a new {@link FluxGroupByUsage} instance. */
  public FluxGroupByUsage() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!FLUX_GROUP_BY.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Type keyMapperType =
        requireNonNull(
            ASTHelpers.getType(tree.getArguments().getFirst()), "Key mapper lacks a type");
    return hasBoundedKeySpace(keyMapperType, state) ? Description.NO_MATCH : describeMatch(tree);
  }

  @Override
  public Description matchMemberReference(MemberReferenceTree tree, VisitorState state) {
    if (!FLUX_GROUP_BY.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    /*
     * The `MemberReferenceTree` API does not expose the referenced method's instantiated type, so
     * this information is obtained from the associated javac AST node. Note that its parameter list
     * omits the receiver, also for unbound references such as `Flux::groupBy`.
     */
    Type keyMapperType = ((JCMemberReference) tree).referentType.getParameterTypes().getFirst();
    return hasBoundedKeySpace(keyMapperType, state) ? Description.NO_MATCH : describeMatch(tree);
  }

  private static boolean hasBoundedKeySpace(Type keyMapperType, VisitorState state) {
    Type functionType = state.getTypes().asSuper(keyMapperType, FUNCTION_TYPE.get(state).tsym);
    if (functionType == null) {
      return false;
    }

    /* Unwrap wildcards such as the `? extends K` in `Function<? super T, ? extends K>`. */
    Type keyType =
        ASTHelpers.getUpperBound(
            state.getTypes().findDescriptorType(functionType).getReturnType(), state.getTypes());
    return ASTHelpers.isSameType(keyType, JAVA_LANG_BOOLEAN_TYPE.get(state), state)
        || keyType.asElement().getKind() == ENUM;
  }
}
