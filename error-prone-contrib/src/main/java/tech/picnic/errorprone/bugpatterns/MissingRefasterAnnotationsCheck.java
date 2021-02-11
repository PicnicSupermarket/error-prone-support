package tech.picnic.errorprone.bugpatterns;

import com.google.auto.service.AutoService;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.MustBeClosed;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;

import java.util.ArrayList;
import java.util.List;

import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.*;

/** A {@link BugChecker} which flags probable missing Refaster annotations. */
@AutoService(BugChecker.class)
@BugPattern(
    name = "MissingRefasterAnnotations",
    summary = "The Refaster template contains a method without annotation",
    linkType = LinkType.NONE,
    severity = SeverityLevel.SUGGESTION,
    tags = StandardTags.SIMPLIFICATION)
public final class MissingRefasterAnnotationsCheck extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final MultiMatcher<Tree, AnnotationTree> REFASTER_ANNOTATION =
      annotations(
          AT_LEAST_ONE,
          anyOf(
              isType("com.google.errorprone.refaster.annotation.BeforeTemplate"),
              isType("com.google.errorprone.refaster.annotation.AfterTemplate"),
              isType("com.google.errorprone.refaster.annotation.Placeholder")));
  // this one is more performant. 

//  protected static final MultiMatcher<Tree, AnnotationTree> HAS_REFASTER_ANNOTATION =
//      annotations(
//          AT_LEAST_ONE,
//          anyOf(
//              symbolHasAnnotation(BeforeTemplate.class.getCanonicalName()),
//              symbolHasAnnotation(AfterTemplate.class.getCanonicalName()),
//              symbolHasAnnotation(Placeholder.class.getCanonicalName())));

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    MultiMatcher.MultiMatchResult<AnnotationTree> annotationTreeMultiMatchResult =
            REFASTER_ANNOTATION.multiMatchResult(tree, state);
    //    if () {
    //      System.err.println("IS een refaster template?");
    //    }
    // now it checks the class annotations.
    List<MethodTree> methods = new ArrayList<>();
    for (Tree member : tree.getMembers()) {
      if (member instanceof MethodTree) {
        MethodTree methodTree = (MethodTree) member;
        methods.add(methodTree);
      }
    }
//  THIS WORKS::::::::
//    REFASTER_ANNOTATION.matches(member, state);
//Now try to get it to work with this: 
//    REFASTER_ANNOTATION.multiMatchResult()

    //    List<AnnotationTree> annotations =
    //        AUTOWIRED_ANNOTATION
    //            .multiMatchResult(Iterables.getOnlyElement(constructors), state)
    //            .matchingNodes();
    //    if (annotations.size() != 1) {
    //      return Description.NO_MATCH;
    //    }

    /*
     * This is the only `@Autowired` constructor: suggest that it be removed. Note that this likely
     * means that the associated import can be removed as well. Rather than adding code for this case we
     * leave flagging the unused import to Error Prone's `RemoveUnusedImports` check.
     */
    //    AnnotationTree annotation = Iterables.getOnlyElement(annotations);
    //    return describeMatch(annotation, SuggestedFix.delete(annotation));
    return Description.NO_MATCH;
  }
}
