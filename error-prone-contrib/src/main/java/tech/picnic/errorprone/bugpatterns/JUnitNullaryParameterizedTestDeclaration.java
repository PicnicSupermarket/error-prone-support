package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.isType;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.bugpatterns.util.MoreMatchers.hasMetaAnnotation;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.matchers.MultiMatcher.MultiMatchResult;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} that flags nullary {@link
 * org.junit.jupiter.params.ParameterizedTest @ParameterizedTest} test methods.
 *
 * <p>Such tests are unnecessarily executed more than necessary. This checker suggests annotating
 * the method with {@link org.junit.jupiter.api.Test @Test}, and to drop all declared {@link
 * org.junit.jupiter.params.provider.ArgumentsSource argument sources}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Nullary JUnit test methods need not be parameterized",
    link = BUG_PATTERNS_BASE_URL + "JUnitNullaryParameterizedTestDeclaration",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class JUnitNullaryParameterizedTestDeclaration extends BugChecker
    implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final MultiMatcher<MethodTree, AnnotationTree> IS_PARAMETERIZED_TEST =
      annotations(AT_LEAST_ONE, isType("org.junit.jupiter.params.ParameterizedTest"));
  private static final Matcher<AnnotationTree> IS_ARGUMENT_SOURCE =
      anyOf(
          isType("org.junit.jupiter.params.provider.ArgumentsSource"),
          isType("org.junit.jupiter.params.provider.ArgumentsSources"),
          hasMetaAnnotation("org.junit.jupiter.params.provider.ArgumentsSource"));

  /** Instantiates a new {@link JUnitNullaryParameterizedTestDeclaration} instance. */
  public JUnitNullaryParameterizedTestDeclaration() {}

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (!tree.getParameters().isEmpty()) {
      return Description.NO_MATCH;
    }

    MultiMatchResult<AnnotationTree> isParameterizedTest =
        IS_PARAMETERIZED_TEST.multiMatchResult(tree, state);
    if (!isParameterizedTest.matches()) {
      return Description.NO_MATCH;
    }

    /*
     * This method is vacuously parameterized. Suggest replacing `@ParameterizedTest` with `@Test`.
     * (As each method is checked independently, we cannot in general determine whether this
     * suggestion makes a `ParameterizedTest` type import obsolete; that task is left to Error
     * Prone's `RemoveUnusedImports` check.)
     */
    SuggestedFix.Builder fix = SuggestedFix.builder();
    fix.merge(
        SuggestedFix.replace(
            isParameterizedTest.onlyMatchingNode(),
            '@' + SuggestedFixes.qualifyType(state, fix, "org.junit.jupiter.api.Test")));

    /*
     * Also suggest dropping all (explicit and implicit) `@ArgumentsSource`s. No attempt is made to
     * assess whether a dropped `@MethodSource` also makes the referenced factory method(s) unused.
     */
    tree.getModifiers().getAnnotations().stream()
        .filter(a -> IS_ARGUMENT_SOURCE.matches(a, state))
        .forEach(a -> fix.merge(SourceCode.deleteWithTrailingWhitespace(a, state)));

    return describeMatch(tree, fix.build());
  }
}
