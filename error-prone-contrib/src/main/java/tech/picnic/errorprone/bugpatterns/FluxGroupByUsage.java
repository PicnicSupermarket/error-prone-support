package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.CONCURRENCY;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.symbolMatcher;
import static com.google.errorprone.predicates.TypePredicates.anyOf;
import static com.google.errorprone.predicates.TypePredicates.isExactType;
import static java.util.Objects.requireNonNull;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.utils.MoreTypePredicates.isSubTypeOf;
import static tech.picnic.errorprone.utils.MoreTypes.generic;
import static tech.picnic.errorprone.utils.MoreTypes.raw;
import static tech.picnic.errorprone.utils.MoreTypes.subOf;
import static tech.picnic.errorprone.utils.MoreTypes.type;
import static tech.picnic.errorprone.utils.MoreTypes.unbound;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ClassName;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MemberReferenceTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.predicates.TypePredicate;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberReferenceTree.ReferenceMode;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCMemberReference;
import java.util.List;
import java.util.function.Function;
import reactor.core.publisher.Flux;
import tech.picnic.errorprone.utils.MoreASTHelpers;

/**
 * A {@link BugChecker} that flags {@link Flux#groupBy} usages with a statically unbounded key
 * space.
 *
 * <p>The groups emitted by {@link Flux#groupBy} are live views of the source and must be drained
 * downstream. If the number of groups exceeds the downstream subscription concurrency, then
 * backpressure may prevent both the groups and their source from making progress, resulting in a
 * deadlock.
 *
 * <p>{@link Boolean} and enum keys are accepted, as their cardinality is statically bounded. For
 * any other key type, make sure that all groups are consumed concurrently, and suppress this check.
 */
// XXX: Even groups with a bounded key space may deadlock if they are consumed with insufficient
// concurrency, such as by `Flux#concatMap`. Consider flagging such cases too.
// XXX: `groupBy` operations with an explicit `prefetch` argument may be safe from deadlock, at the
// cost of large or unbounded memory usage. Consider introducing a flag to suppress flagging of such
// invocations.
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        """
        `Flux#groupBy` may deadlock if the number of groups exceeds the downstream subscription \
        concurrency; please use a statically bounded key type, or suppress this check if all \
        groups are consumed concurrently\
        """,
    link = BUG_PATTERNS_BASE_URL + "FluxGroupByUsage",
    linkType = CUSTOM,
    severity = ERROR,
    tags = {CONCURRENCY, LIKELY_ERROR})
public final class FluxGroupByUsage extends BugChecker
    implements MethodInvocationTreeMatcher, MemberReferenceTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final ClassName FLUX = ClassName.of("reactor.core.publisher.Flux");
  private static final String GROUP_BY = "groupBy";
  private static final String FUNCTION = Function.class.getCanonicalName();
  private static final TypePredicate IS_FLUX = isExactType(FLUX);
  private static final Matcher<ExpressionTree> FLUX_GROUP_BY =
      allOf(
          instanceMethod().onDescendantOf(FLUX).named(GROUP_BY),
          symbolMatcher((symbol, state) -> IS_FLUX.apply(symbol.owner.type, state)));
  private static final Supplier<Type> BOOLEAN = type(Boolean.class.getCanonicalName());
  private static final Supplier<Type> ENUM = raw(type(Enum.class.getCanonicalName()));
  private static final TypePredicate IS_BOUNDED_KEY_TYPE =
      anyOf(isSubTypeOf(BOOLEAN), isSubTypeOf(ENUM));
  private static final TypePredicate HAS_BOUNDED_KEY_SPACE =
      anyOf(
          isSubTypeOf(generic(type(FUNCTION), unbound(), subOf(BOOLEAN))),
          isSubTypeOf(generic(type(FUNCTION), unbound(), subOf(ENUM))));

  /** Instantiates a new {@link FluxGroupByUsage} instance. */
  public FluxGroupByUsage() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!FLUX_GROUP_BY.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    ExpressionTree keyMapper = tree.getArguments().getFirst();
    Type keyMapperType = requireNonNull(ASTHelpers.getType(keyMapper), "Key mapper lacks type");
    return HAS_BOUNDED_KEY_SPACE.apply(keyMapperType, state) || isBoundedKeyMapper(keyMapper, state)
        ? Description.NO_MATCH
        : describeMatch(tree);
  }

  @Override
  public Description matchMemberReference(MemberReferenceTree tree, VisitorState state) {
    if (!FLUX_GROUP_BY.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    /*
     * The key mapper type is derived from the method reference's functional descriptor. For unbound
     * method references such as `Flux::groupBy` the descriptor's first parameter is the receiver,
     * so the key mapper's index is determined relative to the end of the parameter list.
     */
    List<Type> parameterTypes =
        state
            .getTypes()
            .findDescriptorType(
                requireNonNull(ASTHelpers.getType(tree), "Method reference lacks type"))
            .getParameterTypes();
    Type keyMapperType =
        parameterTypes.get(
            parameterTypes.size() - ASTHelpers.getSymbol(tree).getParameters().size());
    return HAS_BOUNDED_KEY_SPACE.apply(keyMapperType, state)
        ? Description.NO_MATCH
        : describeMatch(tree);
  }

  /**
   * Tells whether the given key mapper is a lambda expression or method reference that only
   * produces keys of a statically bounded key type.
   *
   * <p>Such a key mapper's own type may be less specific, as the key type of the {@code groupBy}
   * invocation may be inferred to be wider.
   */
  private static boolean isBoundedKeyMapper(ExpressionTree keyMapper, VisitorState state) {
    return switch (ASTHelpers.stripParentheses(keyMapper)) {
      case LambdaExpressionTree lambda ->
          MoreASTHelpers.findResultExpressions(lambda).stream()
              .flatMap(result -> MoreASTHelpers.findBranchExpressions(result).stream())
              .allMatch(key ->
                  isBoundedKeyType(
                      requireNonNull(ASTHelpers.getType(key), "Key lacks type"), state));
      case JCMemberReference memberReference ->
          isBoundedKeyType(
              memberReference.getMode() == ReferenceMode.NEW
                  ? ASTHelpers.getSymbol(memberReference).owner.type
                  : memberReference.referentType.getReturnType(),
              state);
      default -> false;
    };
  }

  private static boolean isBoundedKeyType(Type type, VisitorState state) {
    return IS_BOUNDED_KEY_TYPE.apply(state.getTypes().boxedTypeOrType(type), state);
  }
}
