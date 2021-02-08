package tech.picnic.errorprone.bugpatterns;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.util.List;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.ALL;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anything;

/**
 * A {@link BugChecker} which flags annotations which aren't sorted lexicographically.
 *
 * <p>The idea behind this checker is that maintaining a sorted sequence simplifies conflict
 * resolution, and can even avoid it if two branches add the same entry.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "LexicographicalAnnotation",
    summary = "Where possible, sort annotations lexicographically",
    linkType = LinkType.NONE,
    severity = SeverityLevel.SUGGESTION,
    tags = StandardTags.STYLE,
    providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION)
public final class LexicographicalAnnotationCheck extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  //  https://github.com/google/error-prone/pull/2125/files

  private static final MultiMatcher<MethodTree, AnnotationTree> FINDER =
      annotations(ALL, anything());

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    List<Attribute.Compound> rawAttributes = ASTHelpers.getSymbol(tree).getRawAttributes();

    //    Stream<Attribute.Compound> declarationAndTypeAttributes =
    // ASTHelpers.getDeclarationAndTypeAttributes(ASTHelpers.getSymbol(tree));
    //    java.util.List<Attribute.Compound> collect1 =
    // declarationAndTypeAttributes.collect(Collectors.toList());
    if (rawAttributes.length() < 2) {
      return Description.NO_MATCH;
    }

    java.util.List<? extends AnnotationTree> annotations = tree.getModifiers().getAnnotations();

    ImmutableList<? extends AnnotationTree> sortedAnnotations =
        annotations.stream()
            .sorted(
                Comparator.comparing(
                    annotationTree -> ASTHelpers.getAnnotationName(annotationTree)))
            .collect(toImmutableList());

    if (Iterators.elementsEqual(annotations.iterator(), sortedAnnotations.iterator())) {
      return Description.NO_MATCH;
    }

    //    java.util.List<? extends AnnotationTree> getAnnotations();
    // 2 copy, 1ste hou je. 2de, sorteer je by string represantation.
    // loop n -1 , tree index 0 vervangen string repr
    //    MultiMatcher.MultiMatchResult<AnnotationTree> annotationTreeMultiMatchResult =
    // FINDER.multiMatchResult(tree, state);
    //    if (annotationTreeMultiMatchResult.matches())
    //    AssignmentTree assignmentTree = (AssignmentTree) argumentTree;
    //    if (ASTHelpers.getSymbol(assignmentTree.getVariable())
    //            .getSimpleName()
    //            .contentEquals("value")) {

//    String suggestion =
//        collect.stream()
//            .map(annotation -> "@" + annotation.type.tsym.name.toString() + "()")
//            .collect(Collectors.joining("\r\n"));
    //    String suggestion =
    //            collect.stream()
    //                        .map(comp -> Util.treeToString(comp, state))
    //            //            .collect(joining(", ", "{", "}"));
    //            //    return Optional.of(SuggestedFix.builder().replace(array, suggestion));

    //    SuggestedFix.builder().replace()

    return Description.NO_MATCH;
  }

  //    SuggestedFix.builder().
  //    return describeMatch(collect, SuggestedFix.replace(, ));

}
