package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.tools.javac.code.Attribute;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import tech.picnic.errorprone.utils.AnnotationAttributeMatcher;
import tech.picnic.errorprone.utils.Flags;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags annotation array listings that contain duplicate entries.
 *
 * <p>Two entries are considered duplicates if they denote the same value, as determined by the
 * compiler. This means that e.g. {@code 'a'} and {@code 97} in a {@code char[]}, {@code 1} and
 * {@code 1L} in a {@code long[]}, and {@code TYPE} and {@code ElementType.TYPE} are duplicates.
 */
// XXX: If both an annotation array and an annotation nested inside one of its entries contain
// duplicates, then the suggested fixes overlap. Consider deduplicating nested listings as part of
// the enclosing fix.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Avoid duplicate entries in annotation array attributes",
    link = BUG_PATTERNS_BASE_URL + "DuplicateAnnotationAttributeListing",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
@SuppressWarnings("java:S2160" /* Super class equality definition suffices. */)
public final class DuplicateAnnotationAttributeListing extends BugChecker
    implements AnnotationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String FLAG_PREFIX = "DuplicateAnnotationAttributeListing:";
  private static final String INCLUDED_ANNOTATIONS_FLAG = FLAG_PREFIX + "Includes";
  private static final String EXCLUDED_ANNOTATIONS_FLAG = FLAG_PREFIX + "Excludes";

  private final AnnotationAttributeMatcher matcher;

  /** Instantiates a default {@link DuplicateAnnotationAttributeListing} instance. */
  public DuplicateAnnotationAttributeListing() {
    this(ErrorProneFlags.empty());
  }

  /**
   * Instantiates a customized {@link DuplicateAnnotationAttributeListing}.
   *
   * @param flags Any provided command line flags.
   */
  @Inject
  DuplicateAnnotationAttributeListing(ErrorProneFlags flags) {
    matcher = createAnnotationAttributeMatcher(flags);
  }

  @Override
  public Description matchAnnotation(AnnotationTree tree, VisitorState state) {
    Map<? extends ExecutableElement, ? extends AnnotationValue> values =
        ASTHelpers.getAnnotationMirror(tree).getElementValues();
    /* The compiler represents each argument as an assignment, even if `value =` is elided. */
    return matcher
        .extractMatchingArguments(tree)
        .map(AssignmentTree.class::cast)
        .flatMap(argument ->
            removeDuplicates(
                argument,
                requireNonNull(
                    values.get(ASTHelpers.getSymbol(argument.getVariable())),
                    "Explicit annotation argument lacks value"),
                state)
                .stream())
        .reduce(SuggestedFix.Builder::merge)
        .map(fix -> describeMatch(tree, fix.build()))
        .orElse(Description.NO_MATCH);
  }

  /**
   * Suggests to remove duplicate entries from the given annotation argument, if it is an array
   * listing.
   *
   * @implNote Entries are identified by the string representation of their compiler-computed {@link
   *     Attribute}, which unambiguously renders constants (with their element type),
   *     fully-qualified enum constants and class literals, and nested annotations.
   */
  private static Optional<SuggestedFix.Builder> removeDuplicates(
      AssignmentTree argument, AnnotationValue value, VisitorState state) {
    if (!(argument.getExpression() instanceof NewArrayTree array)) {
      return Optional.empty();
    }

    List<? extends ExpressionTree> entries = array.getInitializers();
    ImmutableCollection<? extends ExpressionTree> distinctEntries =
        Streams.zip(
                ((Attribute.Array) value).getValue().stream().map(Attribute::toString),
                entries.stream(),
                Map::entry)
            .collect(
                toImmutableMap(Map.Entry::getKey, Map.Entry::getValue, (first, second) -> first))
            .values();
    if (distinctEntries.size() == entries.size()) {
      return Optional.empty();
    }

    return Optional.of(
        SuggestedFix.builder()
            .replace(
                array,
                distinctEntries.stream()
                    .map(entry -> SourceCode.treeToString(entry, state))
                    .collect(joining(", ", "{", "}"))));
  }

  private static AnnotationAttributeMatcher createAnnotationAttributeMatcher(
      ErrorProneFlags flags) {
    return AnnotationAttributeMatcher.create(
        flags.get(INCLUDED_ANNOTATIONS_FLAG).isPresent()
            ? Optional.of(flags.getListOrEmpty(INCLUDED_ANNOTATIONS_FLAG))
            : Optional.empty(),
        Flags.getSet(flags, EXCLUDED_ANNOTATIONS_FLAG));
  }
}
