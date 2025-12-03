package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.matchers.Matchers.typePredicateMatcher;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.utils.MoreTypePredicates.isSubTypeOf;
import static tech.picnic.errorprone.utils.MoreTypes.generic;
import static tech.picnic.errorprone.utils.MoreTypes.type;

import com.google.auto.service.AutoService;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Type;
import java.util.function.Consumer;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A {@link BugChecker} that flags {@link Publisher} operations that are known to be vacuous, given
 * that they are invoked on a {@link Mono} or {@link Flux} that does not emit next signals.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Avoid vacuous operations on empty `Publisher`s",
    link = BUG_PATTERNS_BASE_URL + "EmptyReactivePublisher",
    linkType = CUSTOM,
    severity = WARNING,
    tags = SIMPLIFICATION)
public final class EmptyReactivePublisher extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Supplier<Type> MONO =
      Suppliers.typeFromString("reactor.core.publisher.Mono");
  private static final Supplier<Type> FLUX =
      Suppliers.typeFromString("reactor.core.publisher.Flux");
  private static final Matcher<Tree> CONSUMER =
      isSubtypeOf(type(Consumer.class.getCanonicalName()));
  private static final Supplier<Type> VOID = type(Void.class.getCanonicalName());

  private static final Matcher<ExpressionTree> EMPTY_FLUX =
      anyOf(
          staticMethod().onDescendantOf(FLUX).named("empty"),
          typePredicateMatcher(isSubTypeOf(generic(FLUX, VOID))));
  private static final Matcher<ExpressionTree> EMPTY_MONO =
      anyOf(
          staticMethod().onDescendantOf(MONO).named("empty"),
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
              "doOnNext", "filter", "flatMap", "flatMapMany", "flatMapIterable", "handle", "map");

  private static final Matcher<ExpressionTree> SUBSCRIBE =
      instanceMethod()
          .onDescendantOf(Suppliers.typeFromString("org.reactivestreams.Publisher"))
          .named("subscribe");

  /** Instantiates a new {@link EmptyReactivePublisher} instance. */
  public EmptyReactivePublisher() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    ExpressionTree receiver = ASTHelpers.getReceiver(tree);
    if (receiver == null
        || (!EMPTY_FLUX.matches(receiver, state) && !EMPTY_MONO.matches(receiver, state))) {
      return Description.NO_MATCH;
    }

    if (SUBSCRIBE.matches(tree, state)) {
      // First argument passed to `#subscribe` is always an on next signal `Consumer`.
      ExpressionTree firstArgument = Iterables.getFirst(tree.getArguments(), null);
      if ((firstArgument instanceof LambdaExpressionTree
              || firstArgument instanceof MemberReferenceTree)
          && CONSUMER.matches(firstArgument, state)) {
        return buildDescription(firstArgument)
            .setMessage("Passing an on next signal `Consumer` on empty `Publisher`s is a no-op")
            .build();
      }
    }

    if (EMPTY_FLUX.matches(receiver, state) && VACUOUS_EMPTY_FLUX_OPERATORS.matches(tree, state)) {
      return buildDescription(tree)
          .setMessage(
              "Operator `%s` on an empty `Flux`s is a no-op"
                  .formatted(ASTHelpers.getSymbol(tree).getSimpleName()))
          .build();
    }

    if (EMPTY_MONO.matches(receiver, state) && VACUOUS_EMPTY_MONO_OPERATORS.matches(tree, state)) {
      return buildDescription(tree)
          .setMessage(
              "Operator `%s` on an empty `Mono`s is a no-op"
                  .formatted(ASTHelpers.getSymbol(tree).getSimpleName()))
          .build();
    }
    return Description.NO_MATCH;
  }
}
