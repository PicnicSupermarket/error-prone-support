package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.REFACTORING;
import static com.google.errorprone.matchers.Matchers.isType;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;

/**
 * A {@link BugChecker} that replaces {@link org.testng.annotations.Test} annotations with Jupiter
 * test annotations.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Migrate TestNG test annotation to Jupiter",
    linkType = NONE,
    severity = WARNING,
    tags = REFACTORING)
public final class TestNGAnnotation extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<AnnotationTree> TESTNG_ANNOTATION =
      isType("org.testng.annotations.Test");

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    SuggestedFix.Builder fix = SuggestedFix.builder();
    ASTHelpers.getAnnotations(tree).stream()
        .filter(annotation -> TESTNG_ANNOTATION.matches(annotation, state))
        .filter(annotation -> annotation.getArguments().isEmpty())
        .forEach(annotation -> fix.replace(annotation, "@org.junit.jupiter.api.Test"));

    return fix.isEmpty() ? Description.NO_MATCH : describeMatch(tree, fix.build());
  }
}
