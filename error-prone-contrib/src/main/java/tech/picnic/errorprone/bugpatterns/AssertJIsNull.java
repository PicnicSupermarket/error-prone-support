package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.argument;
import static com.google.errorprone.matchers.Matchers.argumentCount;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.nullLiteral;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.MethodInvocationTree;

/**
 * A {@link BugChecker} that flags AssertJ {@code isEqualTo(null)} checks for simplification.
 *
 * <p>This bug checker cannot be replaced with a simple Refaster rule, as the Refaster approach
 * would require that all overloads of {@link org.assertj.core.api.Assert#isEqualTo(Object)} (such
 * as {@link org.assertj.core.api.AbstractStringAssert#isEqualTo(String)}) are explicitly
 * enumerated. This bug checker generically matches all such current and future overloads.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Prefer `.isNull()` over `.isEqualTo(null)`",
    link = BUG_PATTERNS_BASE_URL + "AssertJIsNull",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class AssertJIsNull extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<MethodInvocationTree> ASSERT_IS_EQUAL_TO_NULL =
      allOf(
          instanceMethod().onDescendantOf("org.assertj.core.api.Assert").named("isEqualTo"),
          argumentCount(1),
          argument(0, nullLiteral()));

  /** Instantiates the default {@link AssertJIsNull}. */
  public AssertJIsNull() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!ASSERT_IS_EQUAL_TO_NULL.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder fix =
        SuggestedFix.builder().merge(SuggestedFixes.renameMethodInvocation(tree, "isNull", state));
    tree.getArguments().forEach(arg -> fix.merge(SuggestedFix.delete(arg)));

    return describeMatch(tree, fix.build());
  }
}
