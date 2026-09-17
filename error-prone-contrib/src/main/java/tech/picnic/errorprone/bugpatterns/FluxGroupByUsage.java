package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.CONCURRENCY;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
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
import com.google.errorprone.util.TargetType;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
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
        "`Flux#groupBy` can deadlock; use a Boolean or enum key, or prove bounded cardinality and "
            + "safe consumption",
    link = BUG_PATTERNS_BASE_URL + "FluxGroupByUsage",
    linkType = CUSTOM,
    severity = ERROR,
    tags = {CONCURRENCY, LIKELY_ERROR})
public final class FluxGroupByUsage extends BugChecker
    implements MethodInvocationTreeMatcher, MemberReferenceTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> FLUX_GROUP_BY =
      instanceMethod().onExactClass("reactor.core.publisher.Flux").named("groupBy");
  private static final Supplier<Type> BOOLEAN = Suppliers.typeFromClass(Boolean.class);

  /** Instantiates a new {@link FluxGroupByUsage} instance. */
  public FluxGroupByUsage() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    return shouldFlag(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
  }

  @Override
  public Description matchMemberReference(MemberReferenceTree tree, VisitorState state) {
    return shouldFlag(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
  }

  private static boolean shouldFlag(ExpressionTree tree, VisitorState state) {
    if (!FLUX_GROUP_BY.matches(tree, state)) {
      return false;
    }

    Type returnType = getReturnType(tree, state);
    if (returnType == null || returnType.getTypeArguments().isEmpty()) {
      return true;
    }

    Type groupedFluxType = returnType.getTypeArguments().getFirst();
    if (groupedFluxType.getTypeArguments().isEmpty()) {
      return true;
    }

    Type keyType = groupedFluxType.getTypeArguments().getFirst();
    return !ASTHelpers.isSameType(keyType, BOOLEAN.get(state), state)
        && keyType.asElement().getKind() != ENUM;
  }

  private static @Nullable Type getReturnType(ExpressionTree tree, VisitorState state) {
    if (tree instanceof MemberReferenceTree) {
      TargetType targetType = TargetType.targetType(state);
      if (targetType != null) {
        return state.getTypes().findDescriptorType(targetType.type()).getReturnType();
      }
    }

    return ASTHelpers.getResultType(tree);
  }
}
