package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
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
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import java.util.Optional;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags AssertJ {@code
 * assertThat(optional.orElseThrow()).isEqualTo(value)} and {@code
 * assertThat(optional.orElseThrow()).isSameAs(value)} expressions for simplification to {@code
 * assertThat(optional).hasValue(value)} and {@code assertThat(optional).containsSame(value)},
 * respectively.
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
      anyOf(
          instanceMethod().onDescendantOf("org.assertj.core.api.Assert").named("isEqualTo"),
          instanceMethod().onDescendantOf("org.assertj.core.api.Assert").named("isSameAs"));
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
        .map(orElseThrow -> describeMatch(tree, createFix(tree, orElseThrow, state)))
        .orElse(Description.NO_MATCH);
  }

  private static Optional<MethodInvocationTree> extractOrElseThrowTree(
      MethodInvocationTree isEqualToTree, VisitorState state) {
    ExpressionTree receiver = ASTHelpers.getReceiver(isEqualToTree);
    if (!(receiver instanceof MethodInvocationTree assertThatTree)) {
      return Optional.empty();
    }

    ExpressionTree assertThatArg = assertThatTree.getArguments().getFirst();
    if (!(assertThatArg instanceof MethodInvocationTree orElseThrow)
        || !OPTIONAL_OR_ELSE_THROW.matches(orElseThrow, state)) {
      return Optional.empty();
    }

    return Optional.of(orElseThrow);
  }

  private static SuggestedFix createFix(
      MethodInvocationTree assertMethodTree,
      MethodInvocationTree orElseThrowTree,
      VisitorState state) {
    MemberSelectTree methodSelect = (MemberSelectTree) orElseThrowTree.getMethodSelect();
    ExpressionTree optionalTree = methodSelect.getExpression();
    return SuggestedFixes.renameMethodInvocation(
            assertMethodTree, getReplacementMethod(assertMethodTree), state)
        .toBuilder()
        .replace(orElseThrowTree, SourceCode.treeToString(optionalTree, state))
        .build();
  }

  private static String getReplacementMethod(MethodInvocationTree tree) {
    String methodName = ASTHelpers.getSymbol(tree).getSimpleName().toString();
    return "isSameAs".equals(methodName) ? "containsSame" : "hasValue";
  }
}
