package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.isType;

import com.google.auto.service.AutoService;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
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
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.util.List;

/** A {@link BugChecker} which flags redundant {@code @Autowired} constructor annotations. */
@AutoService(BugChecker.class)
@BugPattern(
    name = "AutowiredConstructor",
    summary = "Omit `@Autowired` on a class' sole constructor, as it is redundant",
    linkType = LinkType.NONE,
    severity = SeverityLevel.SUGGESTION,
    tags = StandardTags.SIMPLIFICATION)
public final class AutowiredConstructorCheck extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final MultiMatcher<Tree, AnnotationTree> AUTOWIRED_ANNOTATION =
      annotations(AT_LEAST_ONE, isType("org.springframework.beans.factory.annotation.Autowired"));

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    List<MethodTree> constructors = ASTHelpers.getConstructors(tree);
    if (constructors.size() != 1) {
      return Description.NO_MATCH;
    }

    List<AnnotationTree> annotations =
        AUTOWIRED_ANNOTATION
            .multiMatchResult(Iterables.getOnlyElement(constructors), state)
            .matchingNodes();
    if (annotations.size() != 1) {
      return Description.NO_MATCH;
    }

    /*
     * This is the only `@Autowired` constructor: suggest that it be removed. Note that this likely
     * means that the associated import can be removed as well. Rather than adding code for this case we
     * leave flagging the unused import to Error Prone's `RemoveUnusedImports` check.
     */
    AnnotationTree annotation = Iterables.getOnlyElement(annotations);
    return describeMatch(annotation, SuggestedFix.delete(annotation));
  }
}
