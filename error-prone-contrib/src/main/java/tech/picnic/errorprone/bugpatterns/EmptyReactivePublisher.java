package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.matchers.Matchers.typePredicateMatcher;
import static com.google.errorprone.suppliers.Suppliers.JAVA_LANG_VOID_TYPE;
import static java.util.Objects.requireNonNull;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.utils.MoreTypePredicates.isSubTypeOf;
import static tech.picnic.errorprone.utils.MoreTypes.generic;
import static tech.picnic.errorprone.utils.MoreTypes.type;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.suppliers.Supplier;
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
  private static final Supplier<Type> PUBLISHER = type("org.reactivestreams.Publisher");
  private static final Matcher<Tree> CONSUMER =
      isSubtypeOf(type(Consumer.class.getCanonicalName()));
  private static final Matcher<ExpressionTree> EMPTY_PUBLISHER =
      anyOf(
          staticMethod().onDescendantOf(PUBLISHER).named("empty"),
          typePredicateMatcher(isSubTypeOf(generic(PUBLISHER, JAVA_LANG_VOID_TYPE))));
  private static final Matcher<ExpressionTree> VACUOUS_EMPTY_FLUX_OPERATORS =
      instanceMethod()
          .onDescendantOf(type("reactor.core.publisher.Flux"))
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
          .onDescendantOf(type("reactor.core.publisher.Mono"))
          .namedAnyOf(
              "doOnNext", "filter", "flatMap", "flatMapMany", "flatMapIterable", "handle", "map");
  private static final Matcher<ExpressionTree> SUBSCRIBE =
      instanceMethod().onDescendantOf(PUBLISHER).named("subscribe");

  /** Instantiates a new {@link EmptyReactivePublisher} instance. */
  public EmptyReactivePublisher() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    ExpressionTree receiver =
        requireNonNull(
            ASTHelpers.getReceiver(tree), "Instance method invocation must have receiver");
    if (!EMPTY_PUBLISHER.matches(receiver, state)) {
      return Description.NO_MATCH;
    }

    if (SUBSCRIBE.matches(tree, state)) {
      // First argument passed to `#subscribe` is always an on next signal `Consumer`.
      ExpressionTree firstArgument = tree.getArguments().getFirst();
      if ((firstArgument instanceof LambdaExpressionTree
              || firstArgument instanceof MemberReferenceTree)
          && CONSUMER.matches(firstArgument, state)) {
        return buildDescription(firstArgument)
            .setMessage("Passing an on next signal `Consumer` on empty `Publisher` is a no-op")
            .build();
      }
    }

    if (VACUOUS_EMPTY_FLUX_OPERATORS.matches(tree, state)) {
      return buildDescriptionForType(tree, "Flux");
    }

    if (VACUOUS_EMPTY_MONO_OPERATORS.matches(tree, state)) {
      return buildDescriptionForType(tree, "Mono");
    }

    return Description.NO_MATCH;
  }

  private Description buildDescriptionForType(MethodInvocationTree tree, String type) {
    return buildDescription(tree)
        .setMessage(
            "Operator `%s` on an empty `%s` is a no-op"
                .formatted(ASTHelpers.getSymbol(tree).getSimpleName(), type))
        .build();
  }
}
