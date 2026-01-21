package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.matchers.Matchers.IS_INTERFACE;
import static java.util.Comparator.comparing;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import java.util.List;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags permitted non-sealed interfaces that are not lexicographically
 * sorted.
 *
 * <p>The idea behind this checker is that maintaining a sorted sequence simplifies conflict
 * resolution, and can even avoid it if two branches add the same annotation.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Sort listed permitted non-sealed interfaces lexicographically where possible",
    link = BUG_PATTERNS_BASE_URL + "LexicographicalSealedInterfacePermitsListing",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = STYLE)
public final class LexicographicalSealedInterfacePermitsListing extends BugChecker
    implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link LexicographicalSealedInterfacePermitsListing} instance. */
  public LexicographicalSealedInterfacePermitsListing() {}

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    if (!IS_INTERFACE.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    List<? extends Tree> originalOrderPermitClauses = tree.getPermitsClause();
    ImmutableList<? extends Tree> sortedPermitClauses = sort(originalOrderPermitClauses, state);

    if (originalOrderPermitClauses.equals(sortedPermitClauses)) {
      return Description.NO_MATCH;
    }

    return describeMatch(
        originalOrderPermitClauses.getFirst(),
        fixOrdering(originalOrderPermitClauses, sortedPermitClauses, state));
  }

  private static ImmutableList<? extends Tree> sort(
      List<? extends Tree> permitClauses, VisitorState state) {
    return permitClauses.stream()
        .sorted(comparing(annotation -> SourceCode.treeToString(annotation, state)))
        .collect(toImmutableList());
  }

  private static Fix fixOrdering(
      List<? extends Tree> originalOrderPermitClauses,
      ImmutableList<? extends Tree> sortedPermitClauses,
      VisitorState state) {
    return Streams.zip(
            originalOrderPermitClauses.stream(),
            sortedPermitClauses.stream(),
            (original, replacement) ->
                SuggestedFix.builder()
                    .replace(original, SourceCode.treeToString(replacement, state)))
        .reduce(SuggestedFix.builder(), SuggestedFix.Builder::merge, SuggestedFix.Builder::merge)
        .build();
  }
}
