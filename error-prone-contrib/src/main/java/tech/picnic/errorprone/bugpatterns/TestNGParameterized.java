package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.REFACTORING;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.hasArgumentWithValue;
import static com.google.errorprone.matchers.Matchers.isType;
import static com.google.errorprone.matchers.Matchers.stringLiteral;
import static com.sun.source.tree.Tree.Kind.STRING_LITERAL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.AnnotationMatcherUtils;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;

/**
 * A {@link BugChecker} that will flag TestNG {@link org.testng.annotations.Test} annotations that
 * can be migrated to a JUnit {@link org.junit.jupiter.params.ParameterizedTest}. These methods will
 * only be flagged if a migrated version of the data provider is available, these are migrated using
 * {@link TestNGDataProvider}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Migrate TestNG parameterized tests to JUnit",
    linkType = NONE,
    tags = REFACTORING,
    severity = ERROR)
public final class TestNGParameterized extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<AnnotationTree> SUPPRESS_WARNINGS_ANNOTATION =
      allOf(
          isType("java.lang.SuppressWarnings"),
          hasArgumentWithValue("value", stringLiteral("UnusedMethod")));
  private static final Matcher<AnnotationTree> TESTNG_ANNOTATION =
      isType("org.testng.annotations.Test");

  // XXX: Just placed it here but drop all "old" migration code.

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    Optional<? extends AnnotationTree> testAnnotation =
        ASTHelpers.getAnnotations(tree).stream()
            .filter(annotation -> TESTNG_ANNOTATION.matches(annotation, state))
            .findFirst();
    if (testAnnotation.isEmpty() || testAnnotation.get().getArguments().size() != 1) {
      return Description.NO_MATCH;
    }

    ExpressionTree argumentExpression =
        AnnotationMatcherUtils.getArgument(testAnnotation.get(), "dataProvider");
    if (argumentExpression == null || argumentExpression.getKind() != STRING_LITERAL) {
      return Description.NO_MATCH;
    }

    ClassTree classTree = state.findEnclosing(ClassTree.class);
    if (classTree == null) {
      return Description.NO_MATCH;
    }

    String providerName = ((LiteralTree) argumentExpression).getValue().toString();
    Optional<MethodTree> providerMethod = findMethodInClassWithName(classTree, providerName);
    Optional<MethodTree> migratedMethod =
        findMethodInClassWithName(classTree, providerName + "Junit");

    if (migratedMethod.isEmpty() || providerMethod.isEmpty()) {
      return Description.NO_MATCH;
    }

    Optional<? extends AnnotationTree> suppressWarningsAnnotation =
        ASTHelpers.getAnnotations(migratedMethod.orElseThrow()).stream()
            .filter(annotation -> SUPPRESS_WARNINGS_ANNOTATION.matches(annotation, state))
            .findFirst();

    if (suppressWarningsAnnotation.isEmpty()) {
      return Description.NO_MATCH;
    }

    return describeMatch(
        testAnnotation.get(),
        SuggestedFix.builder()
            .addImport("org.junit.jupiter.params.ParameterizedTest")
            .addImport("org.junit.jupiter.params.provider.MethodSource")
            .merge(SuggestedFixes.renameMethod(migratedMethod.orElseThrow(), providerName, state))
            .delete(providerMethod.orElseThrow())
            .delete(suppressWarningsAnnotation.get())
            .replace(
                testAnnotation.get(), "@ParameterizedTest\n@MethodSource(\"" + providerName + "\")")
            .build());
  }

  private static Optional<MethodTree> findMethodInClassWithName(ClassTree classTree, String name) {
    return classTree.getMembers().stream()
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast)
        .filter(method -> method.getName().contentEquals(name))
        .findFirst();
  }
}
