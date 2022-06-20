package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.instanceMethod;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.sun.source.tree.MethodInvocationTree;

/** A {@link BugChecker} which flags {@code Assert.isEqualTo(null)} for further simplification */
@AutoService(BugChecker.class)
@BugPattern(
    name = "AssertThatIsNullUsage",
    summary = "`asserThat(...).isEqualTo(null)` should be `assertThat(...).isNull()`",
    linkType = NONE,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class AssertThatIsNullUsageCheck extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;

  private static final Matcher<MethodInvocationTree> ASSERT_IS_EQUAL =
      Matchers.allOf(
          instanceMethod().onDescendantOf("org.assertj.core.api.Assert").named("isEqualTo"),
          Matchers.argumentCount(1),
          Matchers.argument(0, Matchers.nullLiteral()));

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!ASSERT_IS_EQUAL.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    return describeMatch(tree, SuggestedFixes.renameMethodInvocation(tree, "isNull()", state));
  }
}
