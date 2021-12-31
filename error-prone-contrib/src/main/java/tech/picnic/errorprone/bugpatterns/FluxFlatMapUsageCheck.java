package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;

import com.google.auto.service.AutoService;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MemberReferenceTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MethodInvocationTree;
import java.util.function.Function;
import java.util.function.Supplier;
import reactor.core.publisher.Flux;

/**
 * A {@link BugChecker} which flags usages of {@link Flux#flatMap(Function)}.
 *
 * <p>{@link Flux#flatMap(Function)} eagerly performs up to {@link
 * reactor.util.concurrent.Queues#SMALL_BUFFER_SIZE} subscriptions, interleaving the results as they
 * are emitted. In most cases {@link Flux#concatMap(Function)} should be preferred, as it produces
 * consistent results and avoids potentially saturating the thread pool on which subscription
 * happens. If {@code concatMap}'s single-subscription semantics are undesirable one should invoke a
 * {@code flatMap} or {@code flatMapSequential} overload with an explicit concurrency level.
 *
 * <p>NB: The rarely-used overload {@link Flux#flatMap(Function, Function, Supplier)} is not flagged
 * by this check because there is no clear alternative to point to.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "FluxFlatMapUsage",
    summary =
        "`Flux#flatMap` has subtle semantics; please use `Flux#concatMap` or explicitly specify the desired amount of concurrency",
    linkType = LinkType.NONE,
    severity = SeverityLevel.ERROR,
    tags = StandardTags.LIKELY_ERROR)
public final class FluxFlatMapUsageCheck extends BugChecker
    implements MethodInvocationTreeMatcher, MemberReferenceTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String MAX_CONCURRENCY_ARG_NAME = "MAX_CONCURRENCY";
  private static final Matcher<ExpressionTree> FLUX_FLATMAP =
      instanceMethod()
          .onDescendantOf("reactor.core.publisher.Flux")
          .named("flatMap")
          .withParameters(Function.class.getName());

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!FLUX_FLATMAP.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .addFix(SuggestedFixes.renameMethodInvocation(tree, "concatMap", state))
        .addFix(
            SuggestedFix.builder()
                .postfixWith(
                    Iterables.getOnlyElement(tree.getArguments()), ", " + MAX_CONCURRENCY_ARG_NAME)
                .build())
        .build();
  }

  @Override
  public Description matchMemberReference(MemberReferenceTree tree, VisitorState state) {
    if (!FLUX_FLATMAP.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    // Method references are expected to occur very infrequently; generating both variants of
    // suggested fixes is not worth the trouble.
    return describeMatch(tree);
  }
}
