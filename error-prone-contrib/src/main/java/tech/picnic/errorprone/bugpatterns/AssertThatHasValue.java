package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.argumentCount;
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
import com.google.errorprone.matchers.method.MethodMatchers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import java.util.Optional;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags AssertJ {@code
 * assertThat(optional.orElseThrow()).isEqualTo(value)} expressions for simplification to {@code
 * assertThat(optional).hasValue(value)}.
 *
 * <p>This bug checker cannot be replaced with a simple Refaster rule, as the Refaster approach
 * would require that all overloads of the mentioned methods (such as {@link
 * org.assertj.core.api.AbstractStringAssert#isEqualTo(String)}) are explicitly enumerated. This bug
 * checker generically matches all such current and future overloads.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Prefer `assertThat(optional).hasValue(value)` over `assertThat(optional.orElseThrow()).isEqualTo(value)`",
    link = BUG_PATTERNS_BASE_URL + "AssertThatHasValue",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class AssertThatHasValue extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<MethodInvocationTree> IS_EQUAL_TO_ON_ASSERT =
      allOf(
          instanceMethod().onDescendantOf("org.assertj.core.api.Assert").named("isEqualTo"),
          argumentCount(1));
  private static final Matcher<ExpressionTree> OPTIONAL_OR_ELSE_THROW =
      MethodMatchers.instanceMethod()
          .onExactClass(Optional.class.getCanonicalName())
          .named("orElseThrow")
          .withNoParameters();

  /** Instantiates a new {@link AssertThatHasValue} instance. */
  public AssertThatHasValue() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!IS_EQUAL_TO_ON_ASSERT.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    return getOrElseThrowInvocation(tree, state)
        .flatMap(AssertThatHasValue::getOptionalExpression)
        .map(optionalExpr -> buildFix(tree, optionalExpr, state))
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  /**
   * Returns the {@code optional.orElseThrow()} invocation if the receiver of the given {@code
   * isEqualTo} call is an {@code assertThat} call with exactly one argument that is a call to
   * {@link Optional#orElseThrow()}.
   */
  private static Optional<MethodInvocationTree> getOrElseThrowInvocation(
      MethodInvocationTree isEqualToCall, VisitorState state) {
    ExpressionTree receiver = ASTHelpers.getReceiver(isEqualToCall);
    if (!(receiver instanceof MethodInvocationTree assertThatCall)
        || assertThatCall.getArguments().size() != 1) {
      return Optional.empty();
    }

    ExpressionTree assertThatArg = assertThatCall.getArguments().getFirst();
    if (!(assertThatArg instanceof MethodInvocationTree orElseThrowCall)
        || !OPTIONAL_OR_ELSE_THROW.matches(orElseThrowCall, state)) {
      return Optional.empty();
    }

    return Optional.of(orElseThrowCall);
  }

  /** Extracts the Optional expression from an {@code optional.orElseThrow()} call. */
  private static Optional<ExpressionTree> getOptionalExpression(
      MethodInvocationTree orElseThrowCall) {
    ExpressionTree methodSelect = orElseThrowCall.getMethodSelect();
    if (!(methodSelect instanceof MemberSelectTree memberSelect)) {
      return Optional.empty();
    }
    return Optional.of(memberSelect.getExpression());
  }

  private static SuggestedFix buildFix(
      MethodInvocationTree isEqualToCall, ExpressionTree optionalExpr, VisitorState state) {
    MethodInvocationTree assertThatCall =
        (MethodInvocationTree) ASTHelpers.getReceiver(isEqualToCall);
    MethodInvocationTree orElseThrowCall =
        (MethodInvocationTree) assertThatCall.getArguments().getFirst();

    return SuggestedFixes.renameMethodInvocation(isEqualToCall, "hasValue", state).toBuilder()
        .replace(orElseThrowCall, SourceCode.treeToString(optionalExpr, state))
        .build();
  }
}
