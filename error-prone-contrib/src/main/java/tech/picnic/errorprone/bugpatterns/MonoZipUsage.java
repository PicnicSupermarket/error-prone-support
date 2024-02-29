package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;
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
import com.google.errorprone.suppliers.Suppliers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * A {@link BugChecker} that flags usages of {@link Mono#zip(Mono, Mono)} and {@link
 * Mono#zipWith(Mono)} with {@link Mono#empty()} as arguments.
 *
 * <p>Usage of {@code Mono.zip} and {@code Mono.zipWith} methods with {@code Mono.empty()} as
 * arguments can lead to unintended behavior. When a Mono completes empty or encounters an error,
 * the reactive chain may prematurely terminate, which might not align with the intended logic. This
 * BugChecker helps identify such cases, allowing developers to review and refactor their code to
 * avoid potential issues.
 *
 * @apiNote While {@code Mono<?>.zipWith(Mono<Void>)} is technically allowed by the Reactor API, it
 *     is considered an incorrect usage as it can lead to unexpected behavior.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Don't pass an `Mono<Void>` argument or `Mono.empty()` to Mono#{zip,With}`",
    link = BUG_PATTERNS_BASE_URL + "MonoZipUsage",
    linkType = CUSTOM,
    severity = ERROR,
    tags = LIKELY_ERROR)
public final class MonoZipUsage extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Supplier<Type> MONO =
      Suppliers.typeFromString("reactor.core.publisher.Mono");
  private static final Matcher<ExpressionTree> MONO_EMPTY =
      staticMethod().onDescendantOf(MONO).named("empty");
  private static final Supplier<Type> MONO_VOID =
      VisitorState.memoize(generic(MONO, type(Void.class.getCanonicalName())));

  private static final Matcher<ExpressionTree> MONO_ZIP_AND_ZIP_WITH =
      anyOf(
          instanceMethod().onDescendantOf(type("reactor.core.publisher.Mono")).named("zipWith"),
          staticMethod().onClass(type("reactor.core.publisher.Mono")).named("zip"));

  /** Instantiates a new {@link MonoZipUsage} instance. */
  public MonoZipUsage() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!MONO_ZIP_AND_ZIP_WITH.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    if (ownerIsMonoEmpty(tree, state)) {
      return buildDescription(tree)
          .setMessage("Invoking a `Mono#zip` or `Mono#zipWith` on a `Mono#empty()` is a no-op.")
          .build();
    }

    List<? extends ExpressionTree> arguments = tree.getArguments();
    if (arguments.stream()
        .noneMatch(
            arg ->
                // XXX: We can probably provide a single `Matcher` for these two things.
                MONO_EMPTY.matches(arg, state)
                    || state
                        .getTypes()
                        .isSameType(ASTHelpers.getType(arg), MONO_VOID.get(state)))) {
      return Description.NO_MATCH;
    }
    return describeMatch(tree);
  }

  // XXX: Can we do this in a nicer way?
  private static boolean ownerIsMonoEmpty(MethodInvocationTree tree, VisitorState state) {
    MemberSelectTree methodSelect = (MemberSelectTree) tree.getMethodSelect();
    return MONO_EMPTY.matches(methodSelect.getExpression(), state);
  }
}
