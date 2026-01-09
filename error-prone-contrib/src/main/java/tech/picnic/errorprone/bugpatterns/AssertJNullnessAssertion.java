package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.argument;
import static com.google.errorprone.matchers.Matchers.argumentCount;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.nullLiteral;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodInvocationTree;

/**
 * A {@link BugChecker} that flags AssertJ {@code isEqualTo(null)}, {@code isSameAs(null)}, {@code
 * isNotEqualTo(null)} and {@code isNotSameAs(null)} checks for simplification.
 *
 * <p>This bug checker cannot be replaced with a simple Refaster rule, as the Refaster approach
 * would require that all overloads of the mentioned methods (such as {@link
 * org.assertj.core.api.AbstractStringAssert#isEqualTo(String)}) are explicitly enumerated. This bug
 * checker generically matches all such current and future overloads.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Prefer `.isNull()` and `.isNotNull()` over more verbose alternatives",
    link = BUG_PATTERNS_BASE_URL + "AssertJNullnessAssertion",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class AssertJNullnessAssertion extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final ImmutableSet<String> POSITIVE_ASSERTION_METHODS =
      ImmutableSet.of("isEqualTo", "isSameAs");
  private static final ImmutableSet<String> NEGATIVE_ASSERTION_METHODS =
      ImmutableSet.of("isNotEqualTo", "isNotSameAs");
  private static final Matcher<MethodInvocationTree> VERBOSE_NULL_ASSERTION =
      allOf(
          instanceMethod()
              .onDescendantOf("org.assertj.core.api.Assert")
              .namedAnyOf(Sets.union(POSITIVE_ASSERTION_METHODS, NEGATIVE_ASSERTION_METHODS)),
          argumentCount(1),
          argument(0, nullLiteral()));

  /** Instantiates a new {@link AssertJNullnessAssertion} instance. */
  public AssertJNullnessAssertion() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!VERBOSE_NULL_ASSERTION.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    String replacementAssertion = isPositiveAssertion(tree) ? "isNull" : "isNotNull";
    SuggestedFix fix =
        SuggestedFixes.renameMethodInvocation(tree, replacementAssertion, state).toBuilder()
            .merge(SuggestedFix.delete(Iterables.getOnlyElement(tree.getArguments())))
            .build();

    return describeMatch(tree, fix);
  }

  private static boolean isPositiveAssertion(MethodInvocationTree tree) {
    return POSITIVE_ASSERTION_METHODS.contains(
        ASTHelpers.getSymbol(tree).getSimpleName().toString());
  }
}
