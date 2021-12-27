package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;

import com.google.auto.service.AutoService;
import com.google.common.base.VerifyException;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import tech.picnic.errorprone.annotations.TemplateCollection;

/**
 * A {@link BugChecker} which flags unnecessary {@link Refaster#anyOf(Object[])} usages.
 *
 * <p>Note that this logic can't be implemented as a Refaster template, as the {@link Refaster}
 * class is treated specially.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "RefasterTestAnnotations",
    summary = "`Refaster#anyOf` should be passed at least two parameters",
    linkType = LinkType.NONE,
    severity = SeverityLevel.WARNING,
    tags = StandardTags.STYLE)
public final class RefasterTestAnnotationsCheck extends BugChecker
    implements BugChecker.ClassTreeMatcher {
  private static final long serialVersionUID = 1L;

  private static final MultiMatcher<ClassTree, AnnotationTree> TEMPLATE_COLLECTION_ANNOTATION =
      annotations(AT_LEAST_ONE, hasAnnotation(TemplateCollection.class.toString()));

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    boolean b = ASTHelpers.hasAnnotation(tree, TemplateCollection.class.toString(), state);
    if (TEMPLATE_COLLECTION_ANNOTATION.matches(tree, state) || b) {
      throw new VerifyException("Test");
    }
    return Description.NO_MATCH;
    //    long methodTypes =
    //        tree.getMembers().stream()
    //            .filter(member -> member.getKind() == Tree.Kind.METHOD)
    //            .map(MethodTree.class::cast)
    //            .filter(method -> !ASTHelpers.isGeneratedConstructor(method))
    //            .map(method -> TEMPLATE_COLLECTION_ANNOTATION.matches(method, state))
    //            .distinct()
    //            .count();
    //
    //    return methodTypes < 2 ? Description.NO_MATCH : buildDescription(tree).build();
  }
}
