package tech.picnic.errorprone.workshop.bugpatterns;

import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.argument;
import static com.google.errorprone.matchers.Matchers.argumentCount;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.nullLiteral;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
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
@BugPattern(
    summary = "Prefer `.isNull()` over `.isEqualTo(null)`",
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class Assignment1AssertJIsNullMethod extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<MethodInvocationTree> ASSERT_IS_EQUAL_TO_NULL =
      allOf(
          instanceMethod().onDescendantOf("org.assertj.core.api.Assert").named("isEqualTo"),
          argumentCount(1),
          argument(0, nullLiteral()));

  /** Instantiates a new {@link Assignment1AssertJIsNullMethod} instance. */
  public Assignment1AssertJIsNullMethod() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    // This statement filters out `MethodInvocation`s that are *not* `assertThat().isEqualTo(null)`
    // statements.
    if (!ASSERT_IS_EQUAL_TO_NULL.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder fix = SuggestedFix.builder();

    // XXX: Using `fix.merge(<some code>);` make sure we rename the method invocation to `isNull`.
    // See the `SuggestedFixes` class ;).

    tree.getArguments().forEach(arg -> fix.merge(SuggestedFix.delete(arg)));

    return describeMatch(tree, fix.build());
  }
}
