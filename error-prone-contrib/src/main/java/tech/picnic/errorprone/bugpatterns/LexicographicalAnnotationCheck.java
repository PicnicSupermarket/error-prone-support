package tech.picnic.errorprone.bugpatterns;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.toImmutableList;

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
            .sorted(Comparator.comparing(ASTHelpers::getAnnotationName))
            .collect(toImmutableList());

    if (Iterators.elementsEqual(annotations.iterator(), sortedAnnotations.iterator())) {
      return Description.NO_MATCH;
    }

    Optional<Fix> fix = orderAnnotations(annotations, sortedAnnotations);
    if (fix.isEmpty()) {
      return Description.NO_MATCH;
    }
    return describeMatch(tree, fix);
  }

  @SuppressWarnings("UnstableApiUsage")
  private Optional<Fix> orderAnnotations(
      List<? extends AnnotationTree> annotations,
      ImmutableList<? extends AnnotationTree> sortedAnnotations) {
    List<SuggestedFix.Builder> collect =
        Streams.mapWithIndex(
                annotations.stream(),
                (annotation, i) ->
                    SuggestedFix.builder()
                        .replace(annotation, sortedAnnotations.get(Math.toIntExact(i)).toString()))
            .collect(Collectors.toUnmodifiableList());

    return collect.stream().reduce(SuggestedFix.Builder::merge).map(SuggestedFix.Builder::build);

    // Other option:
    //    SuggestedFix.Builder fix = SuggestedFix.builder();
    //    for (int i = 0; i < annotations.size(); i++) {
    //      fix.replace(annotations.get(i), sortedAnnotations.get(i).toString());
    //    }
    //    return describeMatch(tree, fix.build());
  }
}
