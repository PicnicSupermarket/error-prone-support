package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.sun.tools.javac.code.TypeAnnotations.AnnotationType.DECLARATION;
import static com.sun.tools.javac.code.TypeAnnotations.AnnotationType.TYPE;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ModifiersTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeAnnotations.AnnotationType;
import java.util.Comparator;
import java.util.List;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags annotations that are not lexicographically sorted.
 *
 * <p>The checker currently considers only annotations that are part of a {@link ModifiersTree},
 * such as class-, field-, method- and parameter-level annotations.
 *
 * <p>The idea behind this checker is that maintaining a sorted sequence simplifies conflict
 * resolution, and can even avoid it if two branches add the same annotation.
 */
// XXX: Consider also flagging annotations that aren't part of a `ModifiersTree`, such as those on
//  `AnnotatedTypeTree`, `ModuleTree`, `NewArrayTree`, `PackageTree` and `TypeParameterTree`.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Sort annotations lexicographically where possible",
    link = BUG_PATTERNS_BASE_URL + "LexicographicalAnnotationListing",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = STYLE)
public final class LexicographicalAnnotationListing extends BugChecker
    implements ModifiersTreeMatcher {
  private static final long serialVersionUID = 1L;

  /**
   * A comparator that minimally reorders {@link AnnotationType}s, such that declaration annotations
   * are placed before type annotations.
   */
  @SuppressWarnings({
    "java:S1067",
    "java:S3358"
  } /* Avoiding the nested ternary operator hurts readability. */)
  private static final Comparator<@Nullable AnnotationType> BY_ANNOTATION_TYPE =
      (a, b) ->
          (a == null || a == DECLARATION) && b == TYPE
              ? -1
              : (a == TYPE && b == DECLARATION ? 1 : 0);

  /** Instantiates a new {@link LexicographicalAnnotationListing} instance. */
  public LexicographicalAnnotationListing() {}

  @Override
  public Description matchModifiers(ModifiersTree tree, VisitorState state) {
    List<? extends AnnotationTree> originalOrdering = tree.getAnnotations();
    if (originalOrdering.size() < 2) {
      return Description.NO_MATCH;
    }

    Symbol symbol =
        requireNonNull(
            ASTHelpers.getSymbol(ASTHelpers.findEnclosingNode(state.getPath(), Tree.class)),
            "Cannot find enclosing symbol");

    ImmutableList<? extends AnnotationTree> sortedAnnotations =
        sort(originalOrdering, symbol, state);
    if (originalOrdering.equals(sortedAnnotations)) {
      return Description.NO_MATCH;
    }

    return describeMatch(
        originalOrdering.get(0), fixOrdering(originalOrdering, sortedAnnotations, state));
  }

  private static ImmutableList<? extends AnnotationTree> sort(
      List<? extends AnnotationTree> annotations, Symbol symbol, VisitorState state) {
    return annotations.stream()
        .sorted(
            comparing(
                    (AnnotationTree annotation) ->
                        ASTHelpers.getAnnotationType(annotation, symbol, state),
                    BY_ANNOTATION_TYPE)
                .thenComparing(annotation -> SourceCode.treeToString(annotation, state)))
        .collect(toImmutableList());
  }

  private static Fix fixOrdering(
      List<? extends AnnotationTree> originalAnnotations,
      ImmutableList<? extends AnnotationTree> sortedAnnotations,
      VisitorState state) {
    return Streams.zip(
            originalAnnotations.stream(),
            sortedAnnotations.stream(),
            (original, replacement) ->
                SuggestedFix.builder()
                    .replace(original, SourceCode.treeToString(replacement, state)))
        .reduce(SuggestedFix.Builder::merge)
        .map(SuggestedFix.Builder::build)
        .orElseThrow(() -> new VerifyException("No annotations were provided"));
  }
}
