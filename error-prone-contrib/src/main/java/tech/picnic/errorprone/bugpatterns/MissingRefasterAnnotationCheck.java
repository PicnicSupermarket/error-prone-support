package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.isType;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
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
import java.util.ArrayList;
import java.util.List;

/** A {@link BugChecker} that flags likely missing Refaster annotations. */
@AutoService(BugChecker.class)
@BugPattern(
    name = "MissingRefasterAnnotation",
    summary = "The Refaster template contains a method without Refaster annotation",
    linkType = LinkType.NONE,
    severity = SeverityLevel.WARNING,
    tags = StandardTags.LIKELY_ERROR)
public final class MissingRefasterAnnotationCheck extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final MultiMatcher<Tree, AnnotationTree> HAS_REFASTER_ANNOTATION =
      annotations(
          AT_LEAST_ONE,
          anyOf(
              isType("com.google.errorprone.refaster.annotation.Placeholder"),
              isType("com.google.errorprone.refaster.annotation.BeforeTemplate"),
              isType("com.google.errorprone.refaster.annotation.AfterTemplate")));

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    List<MethodTree> refasterAnnotatedMethods = new ArrayList<>();
    List<MethodTree> otherMethods = new ArrayList<>();

    tree.getMembers().stream()
        .filter(member -> member.getKind() == Tree.Kind.METHOD)
        .map(MethodTree.class::cast)
        .filter(member -> !ASTHelpers.getSymbol(member).isConstructor())
        .forEach(
            member -> {
              if (HAS_REFASTER_ANNOTATION.matches(member, state)) {
                refasterAnnotatedMethods.add(member);
              } else {
                otherMethods.add(member);
              }
            });

    if (refasterAnnotatedMethods.isEmpty() || otherMethods.isEmpty()) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree).build();
  }
}
