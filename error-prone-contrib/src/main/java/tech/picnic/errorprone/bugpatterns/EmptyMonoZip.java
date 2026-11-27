package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.hasArguments;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.receiverOfInvocation;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.utils.MoreMatchers.isSubTypeOf;
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
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import reactor.core.publisher.Mono;

/**
 * A {@link BugChecker} that flags {@link Mono#zip} and {@link Mono#zipWith} invocations with a
 * {@code Mono<Void>} or {@link Mono#empty()} argument or receiver.
 *
 * <p>When a zipped reactive stream completes empty, then the other zipped streams will be cancelled
 * (or not subscribed to), and the operation as a whole will complete empty as well. This is
 * generally not what was intended.
 */
// XXX: Generalize this check to also cover `Flux` zip operations.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Don't pass a `Mono<Void>` or `Mono.empty()` argument to `Mono#{zip,With}`",
    link = BUG_PATTERNS_BASE_URL + "EmptyMonoZip",
    linkType = CUSTOM,
    severity = ERROR,
    tags = LIKELY_ERROR)
public final class EmptyMonoZip extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Supplier<Type> MONO =
      Suppliers.typeFromString("reactor.core.publisher.Mono");
  private static final Matcher<ExpressionTree> MONO_ZIP_OR_ZIP_WITH =
      anyOf(
          instanceMethod().onDescendantOf(MONO).named("zipWith"),
          staticMethod().onClass(MONO).named("zip"));
  private static final Matcher<ExpressionTree> EMPTY_MONO =
      anyOf(
          staticMethod().onDescendantOf(MONO).named("empty"),
          isSubTypeOf(generic(MONO, type(Void.class.getCanonicalName()))));
  private static final Matcher<MethodInvocationTree> HAS_EMPTY_RECEIVER =
      receiverOfInvocation(EMPTY_MONO);
  private static final Matcher<MethodInvocationTree> HAS_EMPTY_ARGUMENT =
      hasArguments(AT_LEAST_ONE, EMPTY_MONO);

  /** Instantiates a new {@link EmptyMonoZip} instance. */
  public EmptyMonoZip() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!MONO_ZIP_OR_ZIP_WITH.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    if (HAS_EMPTY_RECEIVER.matches(tree, state)) {
      return buildDescription(tree)
          .setMessage("Invoking `Mono#zipWith` on `Mono#empty()` or a `Mono<Void>` is a no-op")
          .build();
    }

    return HAS_EMPTY_ARGUMENT.matches(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
  }
}
