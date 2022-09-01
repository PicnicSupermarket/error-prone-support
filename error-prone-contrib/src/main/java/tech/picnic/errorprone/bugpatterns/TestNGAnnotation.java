package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.REFACTORING;
import static com.google.errorprone.matchers.Matchers.isType;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
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
 * A {@link BugChecker} that replaces TestNG annotations with their JUnit counterpart, if one exists
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Migrate TestNG test annotations to JUnit",
    linkType = NONE,
    severity = WARNING,
    tags = REFACTORING)
public final class TestNGAnnotation extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final ImmutableMap<Matcher<AnnotationTree>, String>
      TESTNG_ANNOTATION_REPLACEMENT_MAP =
          ImmutableMap.<Matcher<AnnotationTree>, String>builder()
              .put(isType("org.testng.annotations.AfterClass"), "@org.junit.jupiter.api.AfterAll")
              .put(isType("org.testng.annotations.AfterMethod"), "@org.junit.jupiter.api.AfterEach")
              .put(isType("org.testng.annotations.BeforeClass"), "@org.junit.jupiter.api.BeforeAll")
              .put(
                  isType("org.testng.annotations.BeforeMethod"),
                  "@org.junit.jupiter.api.BeforeEach")
              .put(isType("org.testng.annotations.Test"), "@org.junit.jupiter.api.Test")
              .build();

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    SuggestedFix.Builder fix = SuggestedFix.builder();
    ASTHelpers.getAnnotations(tree).stream()
        .filter(annotation -> annotation.getArguments().isEmpty())
        .forEach(
            annotation ->
                TESTNG_ANNOTATION_REPLACEMENT_MAP.entrySet().stream()
                    .filter(entry -> entry.getKey().matches(annotation, state))
                    .forEach(entry -> fix.replace(annotation, entry.getValue())));

    return fix.isEmpty() ? Description.NO_MATCH : describeMatch(tree, fix.build());
  }
}
