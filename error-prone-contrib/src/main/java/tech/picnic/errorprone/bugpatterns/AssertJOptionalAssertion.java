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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
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
 * A {@link BugChecker} that flags AssertJ equality and identity checks on unconditionally unwrapped
 * {@link Optional} instances for simplification.
 *
 * <p>This bug checker cannot be replaced with a simple Refaster rule, as the Refaster approach
 * would require that all overloads of the mentioned methods (such as {@link
 * org.assertj.core.api.AbstractStringAssert#isEqualTo(String)}) are explicitly enumerated. This bug
 * checker generically matches all such current and future overloads.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Prefer `.hasValue(value)` and `.containsSame(value)` over more verbose alternatives",
    link = BUG_PATTERNS_BASE_URL + "AssertJOptionalAssertion",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class AssertJOptionalAssertion extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final ImmutableMap<String, String> REPLACEMENT_METHODS =
      ImmutableMap.of("isEqualTo", "hasValue", "isSameAs", "containsSame");
  private static final Matcher<MethodInvocationTree> ASSERTION =
      allOf(
          instanceMethod()
              .onDescendantOf("org.assertj.core.api.Assert")
              .namedAnyOf(REPLACEMENT_METHODS.keySet()),
          argumentCount(1));
  private static final Matcher<ExpressionTree> OPTIONAL_UNWRAP =
      instanceMethod()
          .onExactClass(Optional.class.getCanonicalName())
          .namedAnyOf("get", "orElseThrow");

  /** Instantiates a new {@link AssertJOptionalAssertion} instance. */
  public AssertJOptionalAssertion() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!ASSERTION.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    return extractOptionalUnwrap(tree, state)
        .map(optionalUnwrap -> describeMatch(tree, suggestFix(tree, optionalUnwrap, state)))
        .orElse(Description.NO_MATCH);
  }

  private static Optional<MethodInvocationTree> extractOptionalUnwrap(
      MethodInvocationTree tree, VisitorState state) {
    if (!(ASTHelpers.getReceiver(tree) instanceof MethodInvocationTree receiver)
        || receiver.getArguments().size() != 1
        || !ASTHelpers.getSymbol(receiver).isStatic()) {
      /* This doesn't look like the start of an assertion statement. */
      return Optional.empty();
    }

    if (!(Iterables.getOnlyElement(receiver.getArguments()) instanceof MethodInvocationTree subject)
        || !OPTIONAL_UNWRAP.matches(subject, state)) {
      /* The assertion doesn't involve the unconditional unwrapping of an `Optional`. */
      return Optional.empty();
    }

    return Optional.of(subject);
  }

  private static SuggestedFix suggestFix(
      MethodInvocationTree assertion, MethodInvocationTree optionalUnwrap, VisitorState state) {
    ExpressionTree optional =
        requireNonNull(
            ASTHelpers.getReceiver(optionalUnwrap), "Method invocation must have receiver");
    return SuggestedFixes.renameMethodInvocation(assertion, getReplacementMethod(assertion), state)
        .toBuilder()
        .replace(optionalUnwrap, SourceCode.treeToString(optional, state))
        .build();
  }

  private static String getReplacementMethod(MethodInvocationTree tree) {
    return requireNonNull(
        REPLACEMENT_METHODS.get(ASTHelpers.getSymbol(tree).getSimpleName().toString()),
        "Unexpected method name");
  }
}
