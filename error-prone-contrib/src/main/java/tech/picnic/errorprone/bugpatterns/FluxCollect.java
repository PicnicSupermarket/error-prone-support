package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.sun.source.tree.Tree.Kind.MEMBER_SELECT;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} which flags usages of Flux {@code collect(...)} (and similar) followed by
 * empty source checks.
 *
 * <p>Flux collect methods like {@link Flux#collect(java.util.stream.Collector)} (and similar)
 * always emit a value even on an empty source (in which case an empty collections is returned).
 * Following such operations with methods like {@link Mono#single()} or {@link
 * Mono#switchIfEmpty(Mono)} is unnecessary.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "The collect methods of `Flux` always emit a value. Don't unnecessary check otherwise.",
    linkType = NONE,
    severity = WARNING,
    tags = SIMPLIFICATION)
public final class FluxCollect extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> FLUX_EMPTY_CHECK =
      instanceMethod()
          .onDescendantOf("reactor.core.publisher.Mono")
          .namedAnyOf("single", "defaultIfEmpty", "switchIfEmpty");
  private static final Matcher<ExpressionTree> FLUX_COLLECT =
      instanceMethod()
          .onDescendantOf("reactor.core.publisher.Flux")
          .namedAnyOf(
              "collect", "collectList", "collectSortedList", "collectMap", "collectMultimap");

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!FLUX_EMPTY_CHECK.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    if (tree.getMethodSelect().getKind() != MEMBER_SELECT) {
      return Description.NO_MATCH;
    }

    ExpressionTree selectExpression = ((MemberSelectTree) tree.getMethodSelect()).getExpression();
    if (!FLUX_COLLECT.matches(selectExpression, state)) {
      return Description.NO_MATCH;
    }

    return describeMatch(
        tree, SuggestedFix.replace(tree, SourceCode.treeToString(selectExpression, state)));
  }
}
