package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * A {@link BugChecker} that flags annotations that are not lexicographically sorted.
 *
 * <p>The idea behind this checker is that maintaining a sorted sequence simplifies conflict
 * resolution, and can even avoid it if two branches add the same entry.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "LexicographicalAnnotation",
    summary = "Sort annotations lexicographically where possible",
    linkType = LinkType.NONE,
    severity = SeverityLevel.SUGGESTION,
    tags = StandardTags.STYLE)
public final class LexicographicalAnnotationCheck extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    List<? extends AnnotationTree> originalOrdering = tree.getModifiers().getAnnotations();
    if (originalOrdering.size() < 2) {
      return Description.NO_MATCH;
    }

    ImmutableList<? extends AnnotationTree> sortedAnnotations = doSort(originalOrdering, state);
    if (originalOrdering.equals(sortedAnnotations)) {
      return Description.NO_MATCH;
    }

    Optional<Fix> fix = sortAnnotations(originalOrdering, sortedAnnotations, state);

    Description.Builder description = buildDescription(tree);
    fix.ifPresent(description::addFix);
    return description.build();
  }

  private ImmutableList<? extends AnnotationTree> doSort(
      List<? extends AnnotationTree> annotations, VisitorState state) {
    return annotations.stream()
        .sorted(Comparator.comparing(annotation -> Util.treeToString(annotation, state)))
        .collect(toImmutableList());
  }

  private Optional<Fix> sortAnnotations(
      List<? extends AnnotationTree> annotations,
      ImmutableList<? extends AnnotationTree> sortedAnnotations,
      VisitorState state) {
    return Streams.zip(
            annotations.stream(),
            sortedAnnotations.stream(),
            (original, replacement) ->
                SuggestedFix.builder().replace(original, Util.treeToString(replacement, state)))
        .reduce(SuggestedFix.Builder::merge)
        .map(SuggestedFix.Builder::build);
  }
}
