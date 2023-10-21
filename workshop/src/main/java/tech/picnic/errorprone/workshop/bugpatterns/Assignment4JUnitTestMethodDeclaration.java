package tech.picnic.errorprone.workshop.bugpatterns;

import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.isType;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.MultiMatcher;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import javax.lang.model.element.Modifier;

/** A {@link BugChecker} that flags non-canonical JUnit method declarations. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "JUnit method declaration can likely be improved",
    severity = WARNING,
    tags = SIMPLIFICATION)
@SuppressWarnings({
  "UnusedMethod",
  "UnusedVariable"
} /* This check is yet to be implemented as part of the demo. */)
public final class Assignment4JUnitTestMethodDeclaration extends BugChecker
    implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final ImmutableSet<Modifier> ILLEGAL_MODIFIERS =
      Sets.immutableEnumSet(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC);
  private static final MultiMatcher<MethodTree, AnnotationTree> TEST_METHOD =
      annotations(AT_LEAST_ONE, anyOf(isType("org.junit.jupiter.api.Test")));

  /** Instantiates a new {@link Assignment4JUnitTestMethodDeclaration} instance. */
  public Assignment4JUnitTestMethodDeclaration() {}

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    // XXX: Part 1: Return `Description.NO_MATCH` if the method is not a `TEST_METHOD`.

    SuggestedFix.Builder fixBuilder = SuggestedFix.builder();

    // XXX: Part 2: Make sure that JUnit test methods don't use any of the modifiers from the
    // `ILLEGAL_MODIFIERS` field, by using `SuggestedFixes#removeModifiers` and
    // `SuggestedFix.Builder#merge`.

    if (fixBuilder.isEmpty()) {
      return Description.NO_MATCH;
    }

    return describeMatch(tree, fixBuilder.build());
  }
}
