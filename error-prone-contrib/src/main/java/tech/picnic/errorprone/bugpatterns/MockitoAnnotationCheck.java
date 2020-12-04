package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Description.NO_MATCH;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.hasArgumentWithValue;
import static com.google.errorprone.matchers.Matchers.isSameType;
import static com.google.errorprone.matchers.Matchers.isType;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;

/** A {@link BugChecker} which flags classes importing Mockito, but not enforcing strict mocks. */
@AutoService(BugChecker.class)
@BugPattern(
    name = "MockitoAnnotation",
    summary = "Prefer using strict stubs with Mockito",
    linkType = LinkType.NONE,
    severity = SeverityLevel.SUGGESTION,
    tags = StandardTags.STYLE,
    providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION)
public final class MockitoAnnotationCheck extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String MOCKITO_SETTINGS = "org.mockito.junit.jupiter.MockitoSettings";
  private static final String STRICT_STUBS = "org.mockito.quality.Strictness.STRICT_STUBS";
  private static final String MOCKITO_ANNOTATION = "@MockitoSettings(strictness = STRICT_STUBS)";
  private static final MultiMatcher<Tree, AnnotationTree> HAS_STRICT_STUBS_ANNOTATION =
      annotations(
          AT_LEAST_ONE,
          allOf(
              isType(MOCKITO_SETTINGS),
              hasArgumentWithValue("strictness", isSameType(STRICT_STUBS))));

  @Override
  public Description matchClass(ClassTree clazz, VisitorState state) {
    if (ASTHelpers.findEnclosingNode(state.getPath(), ClassTree.class) != null
        || HAS_STRICT_STUBS_ANNOTATION.matches(clazz, state)
        || !importsMockito(state)) {
      return NO_MATCH;
    }

    return describeMatch(clazz, buildFix(clazz));
  }

  private static boolean importsMockito(VisitorState state) {
    return state.getPath().getCompilationUnit().getImports().stream()
        .map(ImportTree::getQualifiedIdentifier)
        .map(Object::toString)
        .anyMatch(importLine -> importLine.startsWith("org.mockito"));
  }

  private static SuggestedFix buildFix(ClassTree clazz) {
    return SuggestedFix.builder()
        .addImport(MOCKITO_SETTINGS)
        .addStaticImport(STRICT_STUBS)
        .prefixWith(clazz, MOCKITO_ANNOTATION)
        .build();
  }
}
