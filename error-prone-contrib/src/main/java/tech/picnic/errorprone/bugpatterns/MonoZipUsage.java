package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.matchers.Matchers.typePredicateMatcher;
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
import com.google.errorprone.suppliers.Suppliers;
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
 * arguments can lead to unintended behavior. When a {@code Mono} completes empty or encounters an
 * error, the reactive chain may prematurely terminate, which might not align with the intended
 * logic.
 *
 * @apiNote While {@code Mono<?>.zipWith(Mono<Void>)} is technically allowed by the Reactor API, it
 *     is considered an incorrect usage as it can lead to unexpected behavior.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Don't pass a `Mono<Void>` argument or `Mono.empty()` to Mono#{zip,With}`",
    link = BUG_PATTERNS_BASE_URL + "MonoZipUsage",
    linkType = CUSTOM,
    severity = ERROR,
    tags = LIKELY_ERROR)
public final class MonoZipUsage extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Supplier<Type> MONO =
      Suppliers.typeFromString("reactor.core.publisher.Mono");
  private static final Matcher<ExpressionTree> MONO_VOID_OR_MONO_EMPTY =
      anyOf(
          staticMethod().onDescendantOf(MONO).named("empty"),
          typePredicateMatcher(isSubTypeOf(generic(MONO, type(Void.class.getCanonicalName())))));
  private static final Matcher<ExpressionTree> MONO_ZIP_OR_ZIP_WITH =
      anyOf(
          instanceMethod().onDescendantOf(MONO).named("zipWith"),
          staticMethod().onClass(MONO).named("zip"));

  /** Instantiates a new {@link MonoZipUsage} instance. */
  public MonoZipUsage() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!MONO_ZIP_OR_ZIP_WITH.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    if (isInvokedOnMonoEmpty(tree, state)) {
      return buildDescription(tree)
          .setMessage(
              "Invoking a `Mono#zip` or `Mono#zipWith` on a `Mono#empty()` or `Mono<Void>` is a no-op.")
          .build();
    }

    List<? extends ExpressionTree> arguments = tree.getArguments();
    if (arguments.stream().noneMatch(arg -> MONO_VOID_OR_MONO_EMPTY.matches(arg, state))) {
      return Description.NO_MATCH;
    }
    return describeMatch(tree);
  }

  private static boolean isInvokedOnMonoEmpty(MethodInvocationTree tree, VisitorState state) {
    MemberSelectTree methodSelect = (MemberSelectTree) tree.getMethodSelect();
    return MONO_VOID_OR_MONO_EMPTY.matches(methodSelect.getExpression(), state);
  }
}
