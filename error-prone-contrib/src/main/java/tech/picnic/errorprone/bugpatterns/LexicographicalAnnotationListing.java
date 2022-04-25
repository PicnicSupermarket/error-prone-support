package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static java.util.Comparator.comparing;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import java.util.List;
import java.util.Optional;

/**
 * A {@link BugChecker} that flags annotations that are not lexicographically sorted.
 *
 * <p>The idea behind this checker is that maintaining a sorted sequence simplifies conflict
 * resolution, and can even avoid it if two branches add the same annotation.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Sort annotations lexicographically where possible",
    linkType = NONE,
    severity = SUGGESTION,
    tags = STYLE)
public final class LexicographicalAnnotationListing extends BugChecker
    implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    List<? extends AnnotationTree> originalOrdering = tree.getModifiers().getAnnotations();
    if (originalOrdering.size() < 2) {
      return Description.NO_MATCH;
    }

    ImmutableList<? extends AnnotationTree> sortedAnnotations = sort(originalOrdering, state);
    if (originalOrdering.equals(sortedAnnotations)) {
      return Description.NO_MATCH;
    }

    Optional<Fix> fix = tryFixOrdering(originalOrdering, sortedAnnotations, state);

    Description.Builder description = buildDescription(originalOrdering.get(0));
    fix.ifPresent(description::addFix);
    return description.build();
  }

  private static ImmutableList<? extends AnnotationTree> sort(
      List<? extends AnnotationTree> annotations, VisitorState state) {
    return annotations.stream()
        .sorted(comparing(annotation -> Util.treeToString(annotation, state)))
        .collect(toImmutableList());
  }

  private static Optional<Fix> tryFixOrdering(
      List<? extends AnnotationTree> originalAnnotations,
      ImmutableList<? extends AnnotationTree> sortedAnnotations,
      VisitorState state) {
    return Streams.zip(
            originalAnnotations.stream(),
            sortedAnnotations.stream(),
            (original, replacement) ->
                SuggestedFix.builder().replace(original, Util.treeToString(replacement, state)))
        .reduce(SuggestedFix.Builder::merge)
        .map(SuggestedFix.Builder::build);
  }
}
