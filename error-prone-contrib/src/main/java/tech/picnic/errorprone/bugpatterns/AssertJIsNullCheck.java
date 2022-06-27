package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.argument;
import static com.google.errorprone.matchers.Matchers.argumentCount;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.nullLiteral;

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
 * A {@link BugChecker} which flags AssertJ {@code isEqualTo(null)} checks for simplification. <br>
 * This cannot be done using a refaster template, as refaster is unable to match the abstraction
 * layers in AssertJ
 *
 * <p>Example: <code>assertThat("foo").isEqualTo(null)</code> will not be matched by refaster.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "AssertJIsNull",
    summary = "Prefer `.isNull()` over `.isEqualTo(null)`",
    linkType = NONE,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class AssertJIsNullCheck extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<MethodInvocationTree> ASSERT_IS_EQUAL_TO_NULL =
      allOf(
          instanceMethod().onDescendantOf("org.assertj.core.api.Assert").named("isEqualTo"),
          argumentCount(1),
          argument(0, nullLiteral()));

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
