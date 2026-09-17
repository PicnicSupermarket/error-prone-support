package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.CONCURRENCY;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MemberReferenceTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MethodInvocationTree;
import java.util.function.Function;
import reactor.core.publisher.Flux;

/**
 * A {@link BugChecker} that flags usages of {@link Flux#groupBy(Function)}.
 *
 * <p>The groups emitted by {@link Flux#groupBy(Function)} are live views of the source and must be
 * drained downstream. If the number of groups exceeds the downstream subscription concurrency,
 * backpressure can prevent both the groups and their source from making progress. This can result
 * in a deadlock.
 *
 * <p>As safe cardinality and consumption cannot generally be established statically, this check
 * flags all usages. Suppress it only when the number of groups is provably small and the downstream
 * consumption strategy is known to be safe.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "`Flux#groupBy` can deadlock; avoid it unless group cardinality is bounded and downstream "
            + "consumption is provably safe",
    link = BUG_PATTERNS_BASE_URL + "FluxGroupByUsage",
    linkType = CUSTOM,
    severity = ERROR,
    tags = {CONCURRENCY, LIKELY_ERROR})
public final class FluxGroupByUsage extends BugChecker
    implements MethodInvocationTreeMatcher, MemberReferenceTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> FLUX_GROUP_BY =
      instanceMethod().onExactClass("reactor.core.publisher.Flux").named("groupBy");

  /** Instantiates a new {@link FluxGroupByUsage} instance. */
  public FluxGroupByUsage() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    return FLUX_GROUP_BY.matches(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
  }

  @Override
  public Description matchMemberReference(MemberReferenceTree tree, VisitorState state) {
    return FLUX_GROUP_BY.matches(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
  }
}
