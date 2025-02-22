package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.nullLiteral;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.matchers.Matchers.typePredicateMatcher;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.utils.MoreTypePredicates.isSubTypeOf;
import static tech.picnic.errorprone.utils.MoreTypes.generic;
import static tech.picnic.errorprone.utils.MoreTypes.type;
import static tech.picnic.errorprone.utils.MoreTypes.unbound;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.predicates.TypePredicate;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import java.util.function.Consumer;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags {@link Publisher} operations that are known to be vacuous, given
 * that they are applied to a {@link Mono} or {@link Flux} that does not emit {@code onNext}
 * signals.
 */
// XXX: Also match (effectively) final variables that reference provably-empty publishers.
// XXX: Also handle `#subscribe` invocations with a non-null value consumer.
// XXX: Suggest a fix, or document why we don't (e.g. because this this requires inference of the
// type of the `Mono` or `Flux`).
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Avoid vacuous operations on empty `Publisher`s",
    link = BUG_PATTERNS_BASE_URL + "EmptyPublisher",
    linkType = CUSTOM,
    severity = WARNING,
    tags = SIMPLIFICATION)
public final class EmptyPublisher extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Supplier<Type> VOID = type(Void.class.getCanonicalName());
  private static final Supplier<Type> MONO =
      Suppliers.typeFromString("reactor.core.publisher.Mono");
  private static final Supplier<Type> FLUX =
      Suppliers.typeFromString("reactor.core.publisher.Flux");
  private static final TypePredicate CONSUMER =
      isSubTypeOf(generic(type(Consumer.class.getCanonicalName()), unbound()));
  // XXX: There are many operators that do not change the number of `onNext` signals, such as
  // `onErrorComplete()` or `publishOn(Scheduler)`. Cover those cases as well.
  private static final Matcher<ExpressionTree> EMPTY_FLUX =
      anyOf(
          staticMethod().onDescendantOf(FLUX).namedAnyOf("empty", "never"),
          typePredicateMatcher(isSubTypeOf(generic(FLUX, VOID))));
  private static final Matcher<ExpressionTree> EMPTY_MONO =
      anyOf(
          staticMethod().onDescendantOf(MONO).namedAnyOf("empty", "never"),
          typePredicateMatcher(isSubTypeOf(generic(MONO, VOID))));

  private static final Matcher<ExpressionTree> VACUOUS_EMPTY_FLUX_OPERATORS =
      instanceMethod()
          .onDescendantOf(FLUX)
          .namedAnyOf(
              "concatMap",
              "doOnNext",
              "filter",
              "flatMap",
              "flatMapIterable",
              "flatMapSequential",
              "handle",
              "map");
  private static final Matcher<ExpressionTree> VACUOUS_EMPTY_MONO_OPERATORS =
      instanceMethod()
          .onDescendantOf(MONO)
          .namedAnyOf(
              "delayElement",
              "delayUntil",
              "doOnNext",
              "expand",
              "expandDeep",
              "filter",
              "filterWhen",
              "flatMap",
              "flatMapMany",
              "flatMapIterable",
              "handle",
              "ignoreElement",
              "map",
              "mapNotNull");

  private static final Matcher<ExpressionTree> SUBSCRIBE =
      instanceMethod()
          .onDescendantOf(Suppliers.typeFromString("org.reactivestreams.Publisher"))
          .named("subscribe");

  /** Instantiates a new {@link EmptyPublisher} instance. */
  public EmptyPublisher() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    ExpressionTree receiver = ASTHelpers.getReceiver(tree);

    // XXX: Factor our the matcher result for reuse below.
    if (receiver == null
        || (!EMPTY_FLUX.matches(receiver, state) && !EMPTY_MONO.matches(receiver, state))) {
      return Description.NO_MATCH;
    }

    if (!tree.getArguments().isEmpty() && SUBSCRIBE.matches(tree, state)) {
      /*
       * The first argument to `#subscribe` overloads is either an `onNext` signal consumer or a
       * `Subscriber`. In the former case, for known-empty publishers, this argument should be
       * `null`.
       */
      ExpressionTree firstArgument = tree.getArguments().get(0);
      if (!nullLiteral().matches(firstArgument, state)
          && CONSUMER.apply(ASTHelpers.getSymbol(tree).getParameters().get(0).asType(), state)) {
        return buildDescription(firstArgument)
            .setMessage("Passing an on next signal `Consumer` on empty `Publisher`s is a no-op")
            .build();
      }
    }

    if (EMPTY_FLUX.matches(receiver, state) && VACUOUS_EMPTY_FLUX_OPERATORS.matches(tree, state)) {
      // XXX: Do the same as for `Mono` below, and update the tests.
      return buildDescription(tree)
          .setMessage(
              String.format(
                  "Operator `%s` on an empty `Flux`s is a no-op",
                  ASTHelpers.getSymbol(tree).getSimpleName()))
          .build();
    }

    if (EMPTY_MONO.matches(receiver, state) && VACUOUS_EMPTY_MONO_OPERATORS.matches(tree, state)) {
      Description.Builder description = buildDescription(tree);
      if (state.getTypes().isSubtype(ASTHelpers.getType(receiver), ASTHelpers.getType(tree))) {
        description.addFix(SuggestedFix.replace(tree, SourceCode.treeToString(receiver, state)));
      }

      return description
          .setMessage(
              String.format(
                  "Operator `%s` on an empty `Mono`s is a no-op",
                  ASTHelpers.getSymbol(tree).getSimpleName()))
          .build();
    }
    return Description.NO_MATCH;
  }
}
