package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static java.util.stream.Collectors.joining;
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
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.tree.JCTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import reactor.test.StepVerifier;

/**
 * A {@link BugChecker} that flags chained usages of {@link StepVerifier.Step#expectNext} in favor
 * of the overloaded variant.
 *
 * <p>Chaining multiple calls of {@link StepVerifier.Step#expectNext} can make the code more verbose
 * than necessary. Instead of chaining multiple calls, the overloaded variants of {@link
 * StepVerifier.Step#expectNext} should be preferred.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Prefer `StepVerifier.Step#expectNext` varargs overload over chaining multiple calls",
    link = BUG_PATTERNS_BASE_URL + "StepVerifierExpectNextUsage",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class StepVerifierExpectNextUsage extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> STEP_EXPECT_NEXT =
      instanceMethod().onDescendantOf("reactor.test.StepVerifier.Step").named("expectNext");

  /** Instantiates a new {@link StepVerifierExpectNextUsage} instance. */
  public StepVerifierExpectNextUsage() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    // If the parent matches, this node will be considered when the parent parses its children, so
    // we don't match it.
    if (!STEP_EXPECT_NEXT.matches(tree, state)
        || getParent(tree).filter(t -> STEP_EXPECT_NEXT.matches(t, state)).isPresent()) {
      return Description.NO_MATCH;
    }

    @Var MethodInvocationTree child = tree;
    List<ExpressionTree> newArgs = new ArrayList<>();

    // The nodes are organized as MethodInvocationTree -> MemberSelectTree -> MethodInvocationTree
    // We skip 2 to find the next method call in the call chain.
    for (int nodeIndex = 2;
        getChild(nodeIndex, state).filter(t -> STEP_EXPECT_NEXT.matches(t, state)).isPresent();
        nodeIndex += 2) {
      // We checked in the loop condition that the child is present, so this is safe
      child = getChild(nodeIndex, state).orElseThrow();
      newArgs.addAll(child.getArguments());
    }

    if (newArgs.isEmpty()) {
      return Description.NO_MATCH;
    }

    String newArgument = newArgs.stream().map(Object::toString).collect(joining(", "));
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
        .filter(MemberSelectTree.class::isInstance)
        .map(ms -> ((MemberSelectTree) ms).getExpression())
        .filter(MethodInvocationTree.class::isInstance)
        .map(MethodInvocationTree.class::cast);
  }

  private static Optional<MethodInvocationTree> getChild(int skip, VisitorState state) {
    int startPosition = ASTHelpers.getStartPosition(state.getPath().getLeaf());
    return StreamSupport.stream(state.getPath().spliterator(), /* parallel= */ false)
        .skip(skip)
        .findFirst()
        .filter(MethodInvocationTree.class::isInstance)
        .map(MethodInvocationTree.class::cast)
        .filter(m -> ((JCTree) m).pos > startPosition);
  }
}
