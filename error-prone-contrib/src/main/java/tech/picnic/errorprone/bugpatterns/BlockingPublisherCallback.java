package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.CONCURRENCY;
import static com.google.errorprone.BugPattern.StandardTags.PERFORMANCE;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.BaseStream;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags blocking calls inside callbacks passed to {@link
 * org.reactivestreams.Publisher} operators.
 *
 * <p>Operator callbacks are invoked on whichever thread happens to deliver the associated signal.
 * In a typical WebFlux or Reactor Netty application this is an event loop thread or a {@link
 * reactor.core.scheduler.Schedulers#parallel() parallel} scheduler thread. Blocking such a thread
 * stalls all other work multiplexed onto it, while Reactor's own blocking operators (e.g. {@code
 * Mono#block()}) outright throw an {@link IllegalStateException} when invoked on a {@link
 * reactor.core.scheduler.NonBlocking} thread. Neither problem surfaces at compile time, and the
 * latter typically only surfaces under production threading conditions.
 *
 * <p>The following cases are deliberately not flagged:
 *
 * <ul>
 *   <li>Callbacks passed to factory methods whose purpose is to adapt blocking code, such as {@code
 *       Mono#fromCallable}, {@code Flux#create} and {@code Flux#using}. Such publishers are
 *       expected to be combined with e.g. {@code subscribeOn(Schedulers.boundedElastic())}.
 *   <li>Callbacks that are not known to be invoked synchronously by the enclosing operator
 *       callback, such as lambda expressions that are assigned, cast or passed to {@code
 *       CompletableFuture#supplyAsync}, and anonymous class bodies.
 * </ul>
 *
 * <p>Callbacks passed to {@code Stream}, {@code Optional}, {@code Iterable#forEach}, {@code
 * Flux#as} and {@code Flux#transform} (and their {@code Mono} and {@code ParallelFlux}
 * counterparts) are assumed to be invoked synchronously, and are thus analyzed in the context of
 * their enclosing callback.
 *
 * <p>If a {@code Mono#map} or {@code Flux#map} callback consists solely of a {@code Mono#block()}
 * invocation, then a fix is suggested that composes the inner {@code Mono} using {@code
 * Mono#flatMap} or {@code Flux#concatMap}, respectively. The latter preserves {@code Flux#map}'s
 * element ordering and sequential processing. Note that this fix is not strictly behavior
 * preserving: if the inner {@code Mono} completes empty, the original code fails with a {@link
 * NullPointerException}, while the rewritten code completes empty (in case of {@code Mono}) or
 * skips the element (in case of {@code Flux}).
 */
// XXX: Also flag method references whose target is declared in the same compilation unit and
// (transitively) performs a blocking call, as in `flux.map(this::lookupBlocking)`.
// XXX: Consider also flagging `CountDownLatch#await`, `Semaphore#acquire` and
// `BlockingQueue#{put,take}`.
// XXX: Consider suppressing the diagnostic if the callback is known to execute on a scheduler that
// supports blocking, e.g. because the operator is directly preceded by
// `publishOn(Schedulers.boundedElastic())`.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Avoid blocking calls inside `Publisher` operator callbacks",
    link = BUG_PATTERNS_BASE_URL + "BlockingPublisherCallback",
    linkType = CUSTOM,
    severity = WARNING,
    tags = {CONCURRENCY, PERFORMANCE})
public final class BlockingPublisherCallback extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String FLUX = "reactor.core.publisher.Flux";
  private static final String MONO = "reactor.core.publisher.Mono";
  private static final String PARALLEL_FLUX = "reactor.core.publisher.ParallelFlux";
  private static final Matcher<ExpressionTree> BLOCKING_CALL =
      anyOf(
          instanceMethod().onDescendantOf(MONO).namedAnyOf("block", "blockOptional"),
          instanceMethod().onDescendantOf(FLUX).namedAnyOf("blockFirst", "blockLast"),
          instanceMethod().onDescendantOf(Future.class.getCanonicalName()).named("get"),
          instanceMethod().onDescendantOf(CompletableFuture.class.getCanonicalName()).named("join"),
          staticMethod().onClass(Thread.class.getCanonicalName()).named("sleep"));
  private static final Matcher<ExpressionTree> BLOCKING_CODE_ADAPTER =
      staticMethod()
          .onClassAny(FLUX, MONO)
          .namedAnyOf(
              "create",
              "fromCallable",
              "fromRunnable",
              "fromStream",
              "fromSupplier",
              "generate",
              "push",
              "using");
  private static final Matcher<ExpressionTree> SYNCHRONOUS_CALLBACK_HOST =
      anyOf(
          instanceMethod()
              .onDescendantOfAny(FLUX, MONO, PARALLEL_FLUX)
              .namedAnyOf("as", "transform"),
          instanceMethod().onDescendantOf(BaseStream.class.getCanonicalName()),
          instanceMethod().onExactClass(Optional.class.getCanonicalName()),
          instanceMethod().onDescendantOf(Iterable.class.getCanonicalName()).named("forEach"));
  private static final Matcher<ExpressionTree> REACTIVE_CALLBACK_HOST =
      anyOf(
          instanceMethod().onDescendantOf("org.reactivestreams.Publisher"),
          staticMethod().onClassAny(FLUX, MONO, PARALLEL_FLUX));
  private static final Matcher<ExpressionTree> MONO_BLOCK =
      instanceMethod().onDescendantOf(MONO).named("block").withNoParameters();
  private static final Matcher<ExpressionTree> MONO_MAP =
      instanceMethod().onDescendantOf(MONO).named("map");
  private static final Matcher<ExpressionTree> FLUX_MAP =
      instanceMethod().onDescendantOf(FLUX).named("map");

  /** Instantiates a new {@link BlockingPublisherCallback} instance. */
  public BlockingPublisherCallback() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!BLOCKING_CALL.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    return findReactiveCallbackHost(state.getPath(), state)
        .map(
            host ->
                trySuggestComposition(tree, host, state)
                    .map(fix -> describeMatch(tree, fix))
                    .orElseGet(() -> describeMatch(tree)))
        .orElse(Description.NO_MATCH);
  }

  /**
   * Finds the method invocation that registers the reactive callback within which the tree
   * identified by the given path is (synchronously) executed, if any.
   */
  private static Optional<MethodInvocationTree> findReactiveCallbackHost(
      TreePath path, VisitorState state) {
    Tree leaf = path.getLeaf();
    if (leaf instanceof MethodTree || leaf instanceof ClassTree) {
      return Optional.empty();
    }

    TreePath parent = path.getParentPath();
    if (!(leaf instanceof LambdaExpressionTree)) {
      return findReactiveCallbackHost(parent, state);
    }

    if (!(parent.getLeaf() instanceof MethodInvocationTree host)
        || BLOCKING_CODE_ADAPTER.matches(host, state)) {
      return Optional.empty();
    }

    if (SYNCHRONOUS_CALLBACK_HOST.matches(host, state)) {
      return findReactiveCallbackHost(parent, state);
    }

    return REACTIVE_CALLBACK_HOST.matches(host, state) ? Optional.of(host) : Optional.empty();
  }

  /**
   * Attempts to rewrite {@code mono.map(v -> inner(v).block())} and {@code flux.map(v ->
   * inner(v).block())} to {@code mono.flatMap(v -> inner(v))} and {@code flux.concatMap(v ->
   * inner(v))}, respectively.
   */
  private static Optional<SuggestedFix> trySuggestComposition(
      MethodInvocationTree tree, MethodInvocationTree host, VisitorState state) {
    if (!(state.getPath().getParentPath().getLeaf() instanceof LambdaExpressionTree lambda)
        || !host.getArguments().contains(lambda)
        || !MONO_BLOCK.matches(tree, state)) {
      return Optional.empty();
    }

    return getComposingOperatorName(host, state)
        .flatMap(
            operatorName ->
                Optional.ofNullable(ASTHelpers.getReceiver(tree))
                    .map(
                        innerMono ->
                            SuggestedFixes.renameMethodInvocation(host, operatorName, state)
                                .toBuilder()
                                .replace(tree, SourceCode.treeToString(innerMono, state))
                                .build()));
  }

  private static Optional<String> getComposingOperatorName(
      MethodInvocationTree host, VisitorState state) {
    if (MONO_MAP.matches(host, state)) {
      return Optional.of("flatMap");
    }

    return FLUX_MAP.matches(host, state) ? Optional.of("concatMap") : Optional.empty();
  }
}
