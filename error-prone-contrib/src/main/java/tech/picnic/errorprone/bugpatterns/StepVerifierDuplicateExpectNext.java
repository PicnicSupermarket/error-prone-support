package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * A {@link BugChecker} that flags duplicated usages of {@link StepVerifier.Step#expectNext} in
 * favor of the overloaded variant.
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
        "When chaining multiple `StepVerifier.Step#expectNext` calls, please use the varargs overload instead",
    link = BUG_PATTERNS_BASE_URL + "StepVerifierDuplicateExpectNext",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class StepVerifierDuplicateExpectNext extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Supplier<Type> STEP =
      Suppliers.typeFromString("reactor.test.StepVerifier.Step");
  private static final Matcher<ExpressionTree> STEP_EXPECTNEXT =
      instanceMethod().onDescendantOf(STEP).named("expectNext");

  /** Instantiates a new {@link StepVerifierDuplicateExpectNext} instance. */
  public StepVerifierDuplicateExpectNext() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!STEP_EXPECTNEXT.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    MethodInvocationTree parent = tree;
    List<ExpressionTree> args = new ArrayList<>();
    while(STEP_EXPECTNEXT.matches(getParent(parent), state)){
      parent = getParent(parent);
      args.addAll(parent.getArguments());
    }

    if (args.isEmpty()) {
      return Description.NO_MATCH;
    }

    String newArgument =
        tree.getArguments().stream().map(Object::toString).collect(Collectors.joining(", "));
    SuggestedFix.Builder argumentsFix =
        SuggestedFix.builder().postfixWith(args.get(args.size() - 1), ", " + newArgument);
    int startPosition = state.getEndPosition(parent);
    int endPosition = state.getEndPosition(tree);

    SuggestedFix removeDuplicateCall = SuggestedFix.replace(startPosition, endPosition, "");
    Description.Builder description = buildDescription(tree);
    description.addFix(argumentsFix.merge(removeDuplicateCall).build());
    return description.build();
  }

  private MethodInvocationTree getParent(MethodInvocationTree tree) {
    MemberSelectTree ms = (MemberSelectTree) tree.getMethodSelect();
    return (MethodInvocationTree) (ms).getExpression();
  }
}
