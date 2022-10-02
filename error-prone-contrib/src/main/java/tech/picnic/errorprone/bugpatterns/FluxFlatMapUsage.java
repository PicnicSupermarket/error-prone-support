package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
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
 * A {@link BugChecker} that flags usages of {@link Flux#flatMap(Function)} and {@link
 * Flux#flatMapSequential(Function)}.
 *
 * <p>{@link Flux#flatMap(Function)} and {@link Flux#flatMapSequential(Function)} eagerly perform up
 * to {@link reactor.util.concurrent.Queues#SMALL_BUFFER_SIZE} subscriptions. Additionally, the
 * former interleaves values as they are emitted, yielding nondeterministic results. In most cases
 * {@link Flux#concatMap(Function)} should be preferred, as it produces consistent results and
 * avoids potentially saturating the thread pool on which subscription happens. If {@code
 * concatMap}'s single-subscription semantics are undesirable one should invoke a {@code flatMap} or
 * {@code flatMapSequential} overload with an explicit concurrency level.
 *
 * <p>NB: The rarely-used overload {@link Flux#flatMap(Function, Function, Supplier)} is not flagged
 * by this check because there is no clear alternative to point to.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "`Flux#flatMap` and `Flux#flatMapSequential` have subtle semantics; "
            + "please use `Flux#concatMap` or explicitly specify the desired amount of concurrency",
    link = BUG_PATTERNS_BASE_URL + "FluxFlatMapUsage",
    linkType = CUSTOM,
    severity = ERROR,
    tags = LIKELY_ERROR)
public final class FluxFlatMapUsage extends BugChecker
    implements MethodInvocationTreeMatcher, MemberReferenceTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String MAX_CONCURRENCY_ARG_NAME = "MAX_CONCURRENCY";
  private static final Matcher<ExpressionTree> FLUX_FLATMAP =
      instanceMethod()
          .onDescendantOf("reactor.core.publisher.Flux")
          .namedAnyOf("flatMap", "flatMapSequential")
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
