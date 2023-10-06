package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} that flags uses of {@link
 * org.junit.jupiter.params.provider.Arguments#arguments(Object...)} with a single (or no) argument;
 * in such cases the use of {@link org.junit.jupiter.params.provider.Arguments} is not required as a
 * {@link java.util.stream.Stream} of objects can be directly provided to a {@link
 * org.junit.jupiter.params.provider.MethodSource}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "JUnit arguments wrapping a single object are redundant",
    link = BUG_PATTERNS_BASE_URL + "JUnitSingleArguments",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class JUnitSingleArguments extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> ARGUMENTS_ARGUMENTS =
      staticMethod().onClass("org.junit.jupiter.params.provider.Arguments").named("arguments");

  /** Instantiates a new {@link JUnitSingleArguments} instance. */
  public JUnitSingleArguments() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (ARGUMENTS_ARGUMENTS.matches(tree, state) && tree.getArguments().size() <= 1) {
      return describeMatch(tree, SourceCode.unwrapMethodInvocation(tree, state));
    }

    return Description.NO_MATCH;
  }
}
