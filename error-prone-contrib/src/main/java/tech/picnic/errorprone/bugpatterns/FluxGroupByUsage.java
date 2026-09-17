package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.CONCURRENCY;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
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
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Flux;

/**
 * A {@link BugChecker} that flags usages of {@link Flux#groupBy(Function)} without a statically
 * bounded key space.
 *
 * <p>The groups emitted by {@link Flux#groupBy(Function)} are live views of the source and must be
 * drained downstream. If the number of groups exceeds the downstream subscription concurrency,
 * backpressure can prevent both the groups and their source from making progress. This can result
 * in a deadlock.
 *
 * <p>This check permits Boolean and concrete enum keys, whose cardinality is statically bounded.
 * Suppress it for another key type only when the number of groups is provably small and the
 * downstream consumption strategy is known to be safe.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Use a Boolean or concrete enum key, or suppress this check for another provably bounded "
            + "`Flux#groupBy` key space",
    link = BUG_PATTERNS_BASE_URL + "FluxGroupByUsage",
    linkType = CUSTOM,
    severity = ERROR,
    tags = {CONCURRENCY, LIKELY_ERROR})
public final class FluxGroupByUsage extends BugChecker
    implements MethodInvocationTreeMatcher, MemberReferenceTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Supplier<Type> BOOLEAN = Suppliers.typeFromClass(Boolean.class);
  private static final Supplier<Type> FUNCTION = Suppliers.typeFromClass(Function.class);
  private static final Matcher<ExpressionTree> FLUX_GROUP_BY =
      instanceMethod().onExactClass("reactor.core.publisher.Flux").named("groupBy");

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
    return describeIfUnbounded(tree, getFunctionReturnType(keyMapperType, state), state);
  }

  @Override
  public Description matchMemberReference(MemberReferenceTree tree, VisitorState state) {
    if (!FLUX_GROUP_BY.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Type keyMapperType = ((JCMemberReference) tree).referentType.getParameterTypes().getFirst();
    return describeIfUnbounded(tree, getFunctionReturnType(keyMapperType, state), state);
  }

  private Description describeIfUnbounded(
      ExpressionTree tree, @Nullable Type keyType, VisitorState state) {
    return keyType != null
            && (ASTHelpers.isSameType(keyType, BOOLEAN.get(state), state)
                || keyType.asElement().getKind() == ENUM)
        ? Description.NO_MATCH
        : describeMatch(tree);
  }

  private static @Nullable Type getFunctionReturnType(Type type, VisitorState state) {
    Type functionType = state.getTypes().asSuper(type, FUNCTION.get(state).tsym);
    return functionType == null
        ? null
        : state.getTypes().findDescriptorType(functionType).getReturnType();
  }
}
