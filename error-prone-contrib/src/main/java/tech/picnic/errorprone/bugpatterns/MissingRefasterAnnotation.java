package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.isType;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;

/** A {@link BugChecker} that flags likely missing Refaster annotations. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "The Refaster template contains a method without any Refaster annotations",
    link = BUG_PATTERNS_BASE_URL + "MissingRefasterAnnotation",
    linkType = CUSTOM,
    severity = WARNING,
    tags = LIKELY_ERROR)
public final class MissingRefasterAnnotation extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final MultiMatcher<Tree, AnnotationTree> REFASTER_ANNOTATION =
      annotations(
          AT_LEAST_ONE,
          anyOf(
              isType("com.google.errorprone.refaster.annotation.Placeholder"),
              isType("com.google.errorprone.refaster.annotation.BeforeTemplate"),
              isType("com.google.errorprone.refaster.annotation.AfterTemplate")));

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    long methodTypes =
        tree.getMembers().stream()
            .filter(member -> member.getKind() == Tree.Kind.METHOD)
            .map(MethodTree.class::cast)
            .filter(method -> !ASTHelpers.isGeneratedConstructor(method))
            .map(method -> REFASTER_ANNOTATION.matches(method, state))
            .distinct()
            .count();

    return methodTypes < 2 ? Description.NO_MATCH : buildDescription(tree).build();
  }
}
