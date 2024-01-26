package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.utils.MoreTypes.generic;
import static tech.picnic.errorprone.utils.MoreTypes.subOf;
import static tech.picnic.errorprone.utils.MoreTypes.type;
import static tech.picnic.errorprone.utils.MoreTypes.unbound;

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
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import java.util.function.Function;
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
 * concatMap}'s sequential-subscription semantics are undesirable one should invoke a {@code
 * flatMap} or {@code flatMapSequential} overload with an explicit concurrency level.
 *
 * <p>NB: The rarely-used overload {@link Flux#flatMap(Function, Function,
 * java.util.function.Supplier)} is not flagged by this check because there is no clear alternative
 * to point to.
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
  private static final Supplier<Type> FLUX =
      Suppliers.typeFromString("reactor.core.publisher.Flux");
  private static final Matcher<ExpressionTree> FLUX_FLATMAP =
      instanceMethod()
          .onDescendantOf(FLUX)
          .namedAnyOf("flatMap", "flatMapSequential")
          .withParameters(Function.class.getCanonicalName());
  private static final Supplier<Type> FLUX_OF_PUBLISHERS =
      VisitorState.memoize(
          generic(FLUX, subOf(generic(type("org.reactivestreams.Publisher"), unbound()))));

  /** Instantiates a new {@link FluxFlatMapUsage} instance. */
  public FluxFlatMapUsage() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!FLUX_FLATMAP.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    SuggestedFix serializationFix = SuggestedFixes.renameMethodInvocation(tree, "concatMap", state);
    SuggestedFix concurrencyCapFix =
        SuggestedFix.postfixWith(
            Iterables.getOnlyElement(tree.getArguments()), ", " + MAX_CONCURRENCY_ARG_NAME);

    Description.Builder description = buildDescription(tree);

    if (state.getTypes().isSubtype(ASTHelpers.getType(tree), FLUX_OF_PUBLISHERS.get(state))) {
      /*
       * Nested publishers may need to be subscribed to eagerly in order to avoid a deadlock, e.g.
       * if they are produced by `Flux#groupBy`. In this case we suggest specifying an explicit
       * concurrently bound, in favour of sequential subscriptions using `Flux#concatMap`.
       */
      description.addFix(concurrencyCapFix).addFix(serializationFix);
    } else {
      description.addFix(serializationFix).addFix(concurrencyCapFix);
    }

    return description.build();
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
