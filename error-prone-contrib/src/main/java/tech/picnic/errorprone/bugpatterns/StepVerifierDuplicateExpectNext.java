package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Var;
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
import com.sun.tools.javac.tree.JCTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import reactor.test.StepVerifier;

/**
 * A {@link BugChecker} that flags duplicated usages of {@link StepVerifier.Step#expectNext} in
 * favor of the overloaded variant.
 *
 * <p>Chaining {@link StepVerifier.Step#expectNext} calls can make the code more verbose than it has
 * to be. Since {@link StepVerifier.Step#expectNext} offers several overload functions, those should
 * be preferred over chaining {@link StepVerifier.Step#expectNext} calls.
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
    // If the parent matches, this node will be considered when the parent parses its children, so
    // we don't match it.
    if (!STEP_EXPECTNEXT.matches(tree, state)
        || getParent(tree).map(t -> STEP_EXPECTNEXT.matches(t, state)).orElse(false)) {
      return Description.NO_MATCH;
    }

    @Var MethodInvocationTree child = tree;
    List<ExpressionTree> newArgs = new ArrayList<>();

    // The nodes are organized as MethodInvocationTree -> MemberSelectTree -> MethodInvocationTree
    // We skip 2 to find the next method call in the call chain.
    for (int nodeIndex = 2;
        getChild(state, nodeIndex).filter(t -> STEP_EXPECTNEXT.matches(t, state)).isEmpty();
        nodeIndex += 2) {
      // We checked in the loop condition that the child is present, so this is safe
      child = getChild(state, nodeIndex).orElseThrow();
      newArgs.addAll(child.getArguments());
    }

    if (newArgs.isEmpty()) {
      return Description.NO_MATCH;
    }

    String newArgument = newArgs.stream().map(Object::toString).collect(Collectors.joining(", "));
    List<? extends ExpressionTree> myArgs = tree.getArguments();
    SuggestedFix.Builder argumentsFix =
        SuggestedFix.builder().postfixWith(myArgs.get(myArgs.size() - 1), ", " + newArgument);
    int startPosition = state.getEndPosition(tree);
    int endPosition = state.getEndPosition(child);

    SuggestedFix removeDuplicateCall = SuggestedFix.replace(startPosition, endPosition, "");
    Description.Builder description = buildDescription(tree);
    description.addFix(argumentsFix.merge(removeDuplicateCall).build());
    return description.build();
  }

  private static Optional<MethodInvocationTree> getParent(MethodInvocationTree tree) {
    return Optional.of(tree.getMethodSelect())
        .filter(ms -> ms instanceof MemberSelectTree)
        .map(ms -> ((MemberSelectTree) ms).getExpression())
        .filter(expr -> expr instanceof MethodInvocationTree)
        .map(expr -> (MethodInvocationTree) expr);
  }

  private static Optional<MethodInvocationTree> getChild(VisitorState state, int skip) {
    int startPos = ((JCTree) state.getPath().getLeaf()).pos;
    return StreamSupport.stream(state.getPath().spliterator(), /* parallel= */false)
        .skip(skip)
        .findFirst()
        .filter(expr -> expr instanceof MethodInvocationTree)
        .map(expr -> (MethodInvocationTree) expr)
        .filter(m -> ((JCTree) m).pos > startPos);
  }
}
