package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.REFACTORING;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.isType;
import static com.google.errorprone.matchers.Matchers.methodHasVisibility;
import static com.google.errorprone.matchers.Matchers.not;
import static com.google.errorprone.matchers.MethodVisibility.Visibility.PUBLIC;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import java.util.function.Predicate;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/** A {@link BugChecker} which flags class level `@Test` annotations from TestNG. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "A bug pattern to migrate TestNG Test annotations to methods",
    linkType = NONE,
    tags = REFACTORING,
    severity = ERROR)
public final class TestNGClassLevelTestAnnotation extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ClassTree> CLASS_TREE = hasAnnotation("org.testng.annotations.Test");
  private static final Matcher<MethodTree> UNMIGRATED_TEST_METHOD =
      allOf(methodHasVisibility(PUBLIC), not(hasAnnotation("org.testng.annotations.Test")));
  private static final Matcher<AnnotationTree> TESTNG_ANNOTATION =
      isType("org.testng.annotations.Test");

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    if (!CLASS_TREE.matches(tree, state) || tree.getExtendsClause() != null) {
      return Description.NO_MATCH;
    }

    Optional<? extends AnnotationTree> testAnnotation =
        ASTHelpers.getAnnotations(tree).stream()
            .filter(annotation -> TESTNG_ANNOTATION.matches(annotation, state))
            .findFirst();
    if (testAnnotation.isEmpty()) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder fix = SuggestedFix.builder();
    tree.getMembers().stream()
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast)
        .filter(method -> UNMIGRATED_TEST_METHOD.matches(method, state))
        .filter(Predicate.not(ASTHelpers::isGeneratedConstructor))
        .forEach(
            methodTree ->
                fix.merge(
                    SuggestedFix.prefixWith(
                        methodTree,
                        String.format(
                            "%s\n", SourceCode.treeToString(testAnnotation.get(), state)))));

    fix.delete(testAnnotation.get());

    return describeMatch(testAnnotation.get(), fix.build());
  }
}
