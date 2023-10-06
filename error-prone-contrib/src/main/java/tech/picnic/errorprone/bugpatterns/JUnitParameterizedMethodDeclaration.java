package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.isType;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.MethodTree;

/**
 * A {@link BugChecker} that flags test methods using {@link
 * org.junit.jupiter.params.ParameterizedTest} without actually having any arguments.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "JUnit parameterized test used without arguments",
    link = BUG_PATTERNS_BASE_URL + "JUnitParameterizedMethodDeclaration",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class JUnitParameterizedMethodDeclaration extends BugChecker
    implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<MethodTree> IS_PARAMETERIZED_TEST =
      annotations(AT_LEAST_ONE, isType("org.junit.jupiter.params.ParameterizedTest"));

  /** Instantiates a new {@link JUnitParameterizedMethodDeclaration} instance. */
  public JUnitParameterizedMethodDeclaration() {}

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (IS_PARAMETERIZED_TEST.matches(tree, state) && tree.getParameters().isEmpty()) {
      return describeMatch(tree);
    }

    return Description.NO_MATCH;
  }
}
