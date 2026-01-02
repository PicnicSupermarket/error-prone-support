package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.bugpatterns.util.MoreJUnitMatchers.TEST_METHOD;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreeScanner;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.mockito.Mockito;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} that flags multiple usages of {@link Mockito#verifyNoInteractions} in favor
 * of one call with varargs.
 *
 * <p>Multiple calls of {@link Mockito#verifyNoInteractions} can make the code more verbose than
 * necessary. Instead of multiple calls, because {@link Mockito#verifyNoInteractions} accepts
 * varargs, one call should be preferred.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Prefer one call to `verifyNoInteractions(varargs...)` over multiple calls",
    link = BUG_PATTERNS_BASE_URL + "MockitoVerifyNoInteractionsUsage",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class MockitoVerifyNoInteractionsUsage extends BugChecker
    implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> VERIFY_NO_INTERACTIONS =
      staticMethod().onClass("org.mockito.Mockito").named("verifyNoInteractions");

  /** Instantiates a new {@link MockitoVerifyNoInteractionsUsage} instance. */
  public MockitoVerifyNoInteractionsUsage() {}

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (!TEST_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }
    ImmutableList<MethodInvocationTree> verifyNoInteractionsInvocations =
        getVerifyNoInteractionsInvocations(tree, state);
    if (verifyNoInteractionsInvocations.size() < 2) {
      return Description.NO_MATCH;
    }
    String combinedArgument =
        verifyNoInteractionsInvocations.stream()
            .map(MethodInvocationTree::getArguments)
            .flatMap(List::stream)
            .map(Object::toString)
            .collect(joining(", "));

    SuggestedFix.Builder fixBuilder = SuggestedFix.builder();
    MethodInvocationTree lastInvocation =
        verifyNoInteractionsInvocations.get(verifyNoInteractionsInvocations.size() - 1);
    verifyNoInteractionsInvocations.forEach(
        invocationTree -> {
          if (!invocationTree.equals(lastInvocation)) {
            fixBuilder.replace(
                ASTHelpers.getStartPosition(invocationTree),
                state.getEndPosition(invocationTree) + 1,
                "");
          }
        });

    String callAsString = SourceCode.treeToString(lastInvocation, state);
    fixBuilder.replace(
        lastInvocation,
        callAsString.startsWith("Mockito.")
            ? "Mockito.verifyNoInteractions(" + combinedArgument + ")"
            : "verifyNoInteractions(" + combinedArgument + ")");

    return describeMatch(tree, fixBuilder.build());
  }

  private static ImmutableList<MethodInvocationTree> getVerifyNoInteractionsInvocations(
      MethodTree methodTree, VisitorState state) {
    ImmutableList.Builder<MethodInvocationTree> invocationTreeBuilder = ImmutableList.builder();

    new TreeScanner<@Nullable Void, @Nullable Void>() {
      @Override
      public @Nullable Void visitMethodInvocation(
          MethodInvocationTree node, @Nullable Void unused) {
        if (VERIFY_NO_INTERACTIONS.matches(node, state)) {
          invocationTreeBuilder.add(node);
        }
        return super.visitMethodInvocation(node, unused);
      }
    }.scan(methodTree, null);

    return invocationTreeBuilder.build();
  }
}
