package tech.picnic.errorprone.workshop.bugpatterns;

import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.isType;

import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.matchers.MultiMatcher.MultiMatchResult;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.List;
import tech.picnic.errorprone.workshop.bugpatterns.util.SourceCode;

/** A {@link BugChecker} that flags redundant {@code @Autowired} constructor annotations. */
@BugPattern(
    summary = "Omit `@Autowired` on a class' sole constructor, as it is redundant",
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class Assignment3DropAutowiredConstructorAnnotation extends BugChecker
    implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final MultiMatcher<Tree, AnnotationTree> AUTOWIRED_ANNOTATION =
      annotations(AT_LEAST_ONE, isType("org.springframework.beans.factory.annotation.Autowired"));

  /** Instantiates a new {@link Assignment3DropAutowiredConstructorAnnotation} instance. */
  public Assignment3DropAutowiredConstructorAnnotation() {}

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    // XXX: Using the `ASTHelpers#getConstructors` method, return `Description.NO_MATCH` if we do
    // not have exactly 1 constructor (so drop the `new ArrayList<>()` part).
    List<MethodTree> constructors = new ArrayList<>();

    MultiMatchResult<AnnotationTree> hasAutowiredAnnotation =
        AUTOWIRED_ANNOTATION.multiMatchResult(Iterables.getOnlyElement(constructors), state);
    if (!hasAutowiredAnnotation.matches()) {
      return Description.NO_MATCH;
    }

    AnnotationTree annotation = hasAutowiredAnnotation.onlyMatchingNode();
    return describeMatch(annotation, SourceCode.deleteWithTrailingWhitespace(annotation, state));
  }
}
