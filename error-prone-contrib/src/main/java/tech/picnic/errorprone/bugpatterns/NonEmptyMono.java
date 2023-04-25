package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import java.util.function.BiFunction;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} that flags {@link Mono} operations that are known to be vacuous, given that
 * they are invoked on a {@link Mono} that is known not to complete empty.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Avoid vacuous operations on known non-empty `Mono`s",
    link = BUG_PATTERNS_BASE_URL + "NonEmptyMono",
    linkType = CUSTOM,
    severity = WARNING,
    tags = SIMPLIFICATION)
// XXX: This check does not simplify `someFlux.defaultIfEmpty(T).{defaultIfEmpty(T),hasElements()}`,
// as `someFlux.defaultIfEmpty(T)` yields a `Flux` rather than a `Mono`. Consider adding support for
// these cases.
// XXX: Given more advanced analysis many more expressions could be flagged. Consider
// `Mono.just(someValue)`, `Flux.just(someNonEmptySequence)`,
// `someMono.switchIfEmpty(someProvablyNonEmptyMono)` and many other variants.
// XXX: Consider implementing a similar check for `Publisher`s that are known to complete without
// emitting a value (e.g. `Mono.empty()`, `someFlux.then()`, ...), or known not to complete normally
// (`Mono.never()`, `someFlux.repeat()`, `Mono.error(...)`, ...). The latter category could
// potentially be split out further.
@SuppressWarnings("java:S1192" /* Factoring out repeated method names impacts readability. */)
public final class NonEmptyMono extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> MONO_SIZE_CHECK =
      instanceMethod()
          .onDescendantOf("reactor.core.publisher.Mono")
          .namedAnyOf("defaultIfEmpty", "single", "switchIfEmpty");
  private static final Matcher<ExpressionTree> NON_EMPTY_MONO =
      anyOf(
          instanceMethod()
              .onDescendantOf("reactor.core.publisher.Flux")
              .namedAnyOf(
                  "all",
                  "any",
                  "collect",
                  "collectList",
                  "collectMap",
                  "collectMultimap",
                  "collectSortedList",
                  "count",
                  "elementAt",
                  "hasElement",
                  "hasElements",
                  "last",
                  "reduceWith",
                  "single"),
          instanceMethod()
              .onDescendantOf("reactor.core.publisher.Flux")
              .named("reduce")
              .withParameters(Object.class.getName(), BiFunction.class.getName()),
          instanceMethod()
              .onDescendantOf("reactor.core.publisher.Mono")
              .namedAnyOf("defaultIfEmpty", "hasElement", "single"));

  /** Instantiates a new {@link NonEmptyMono} instance. */
  public NonEmptyMono() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!MONO_SIZE_CHECK.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    ExpressionTree receiver = ASTHelpers.getReceiver(tree);
    if (!NON_EMPTY_MONO.matches(receiver, state)) {
      return Description.NO_MATCH;
    }

    return describeMatch(
        tree, SuggestedFix.replace(tree, SourceCode.treeToString(receiver, state)));
  }
}
