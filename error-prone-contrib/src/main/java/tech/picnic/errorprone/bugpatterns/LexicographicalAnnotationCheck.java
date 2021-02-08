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
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.util.List;

import javax.lang.model.element.Modifier;
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

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    java.util.List<? extends AnnotationTree> annotations = tree.getModifiers().getAnnotations();
    if (annotations.size() < 2) {
      return Description.NO_MATCH;
    }

    ImmutableList<? extends AnnotationTree> sortedAnnotations =
        annotations.stream()
            .sorted(
                Comparator.comparing(
                    annotationTree -> ASTHelpers.getAnnotationName(annotationTree)))
            .collect(toImmutableList());

    if (Iterators.elementsEqual(annotations.iterator(), sortedAnnotations.iterator())) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder fix = SuggestedFix.builder();
    for (int i = 0; i < annotations.size(); i++) {
      SuggestedFix.Builder test = SuggestedFix.builder();
      test.replace(annotations.get(i), sortedAnnotations.get(i).toString());
      fix = fix.merge(test);
    }

    return describeMatch(tree, fix.build());
    //    SuggestedFix.Builder fix = SuggestedFix.builder();
    //    relevantMembers.forEach(
    //            m -> SuggestedFixes.removeModifiers(m, state,
    // Modifier.PROTECTED).ifPresent(fix::merge));

    //    SuggestedFixes.removeModifiers(tree.getModifiers(), state);
    //    SuggestedFixes.addModifiers(sortedAnnotations, state);
    //    SuggestedFixes.removeModifiers(tree.getModifiers(), state, ILLEGAL_MODIFIERS)
    //            .ifPresent(builder::merge);

//    String suggestion =
    //            collect.stream()
    //                        .map(comp -> Util.treeToString(comp, state))
    //            //            .collect(joining(", ", "{", "}"));
    //            //    return Optional.of(SuggestedFix.builder().replace(array, suggestion));

  }
}
