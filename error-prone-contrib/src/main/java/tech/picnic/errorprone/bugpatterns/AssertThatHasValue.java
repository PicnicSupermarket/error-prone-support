package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.argumentCount;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static java.util.Objects.requireNonNull;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import java.util.Optional;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags AssertJ usages of {@code OptionalAssert} for simplification.
 *
 * <p>This bug checker cannot be replaced with a simple Refaster rule, as the Refaster approach
 * would require that all overloads of the mentioned methods (such as {@link
 * org.assertj.core.api.AbstractStringAssert#isEqualTo(String)}) are explicitly enumerated. This bug
 * checker generically matches all such current and future overloads.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Prefer `assertThat(optional).hasValue(value)` over more verbose alternatives",
    link = BUG_PATTERNS_BASE_URL + "AssertThatHasValue",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class AssertThatHasValue extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<MethodInvocationTree> ASSERT_METHOD =
      allOf(
          instanceMethod()
              .onDescendantOf("org.assertj.core.api.Assert")
              .namedAnyOf("isEqualTo", "isSameAs"),
          argumentCount(1));
  private static final Matcher<ExpressionTree> OPTIONAL_OR_ELSE_THROW =
      instanceMethod()
          .onExactClass(Optional.class.getCanonicalName())
          .named("orElseThrow")
          .withNoParameters();

  /** Instantiates a new {@link AssertThatHasValue} instance. */
  public AssertThatHasValue() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!ASSERT_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    return extractOrElseThrowTree(tree, state)
        .map(orElseThrow -> describeMatch(tree, suggestFix(tree, orElseThrow, state)))
        .orElse(Description.NO_MATCH);
  }

  private static Optional<MethodInvocationTree> extractOrElseThrowTree(
      MethodInvocationTree isEqualToTree, VisitorState state) {
    ExpressionTree receiver = ASTHelpers.getReceiver(isEqualToTree);
    if (!(receiver instanceof MethodInvocationTree assertThatTree)
        || assertThatTree.getArguments().isEmpty()) {
      return Optional.empty();
    }

    ExpressionTree assertThatArg = assertThatTree.getArguments().getFirst();
    if (!(assertThatArg instanceof MethodInvocationTree orElseThrow)
        || !OPTIONAL_OR_ELSE_THROW.matches(orElseThrow, state)) {
      return Optional.empty();
    }

    return Optional.of(orElseThrow);
  }

  private static SuggestedFix suggestFix(
      MethodInvocationTree assertionTree,
      MethodInvocationTree orElseThrowTree,
      VisitorState state) {
    ExpressionTree optionalTree =
        requireNonNull(
            ASTHelpers.getReceiver(orElseThrowTree),
            "Method invocation must have receiver");
    return SuggestedFixes.renameMethodInvocation(
            assertionTree, getReplacementMethod(assertionTree), state)
        .toBuilder()
        .replace(orElseThrowTree, SourceCode.treeToString(optionalTree, state))
        .build();
  }

  private static String getReplacementMethod(MethodInvocationTree tree) {
    return ASTHelpers.getSymbol(tree).getSimpleName().contentEquals("isSameAs")
        ? "containsSame"
        : "hasValue";
  }
}
