package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import tech.picnic.errorprone.bugpatterns.util.AnnotationAttributeMatcher;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} that flags annotation array listings which aren't sorted lexicographically.
 *
 * <p>The idea behind this checker is that maintaining a sorted sequence simplifies conflict
 * resolution, and can even avoid it if two branches add the same entry.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Where possible, sort annotation array attributes lexicographically",
    link = BUG_PATTERNS_BASE_URL + "LexicographicalAnnotationAttributeListing",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = STYLE)
public final class LexicographicalAnnotationAttributeListing extends BugChecker
    implements AnnotationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final ImmutableSet<String> BLACKLISTED_ANNOTATIONS =
      ImmutableSet.of(
          // XXX: unless JsonPropertyOrder#alphabetic is true...
          "com.fasterxml.jackson.annotation.JsonPropertyOrder#value",
          "io.swagger.annotations.ApiImplicitParams#value",
          "io.swagger.v3.oas.annotations.Parameters#value",
          "javax.xml.bind.annotation.XmlType#propOrder",
          "org.springframework.context.annotation.PropertySource#value",
          "org.springframework.test.context.TestPropertySource#locations",
          "org.springframework.test.context.TestPropertySource#value");
  private static final String FLAG_PREFIX = "LexicographicalAnnotationAttributeListing:";
  private static final String INCLUDED_ANNOTATIONS_FLAG = FLAG_PREFIX + "Includes";
  private static final String EXCLUDED_ANNOTATIONS_FLAG = FLAG_PREFIX + "Excludes";

  private final AnnotationAttributeMatcher matcher;

  /** Instantiates the default {@link LexicographicalAnnotationAttributeListing}. */
  public LexicographicalAnnotationAttributeListing() {
    this(ErrorProneFlags.empty());
  }

  /**
   * Instantiates a customized {@link LexicographicalAnnotationAttributeListing}.
   *
   * @param flags Any provided command line flags.
   */
  public LexicographicalAnnotationAttributeListing(ErrorProneFlags flags) {
    matcher = createAnnotationAttributeMatcher(flags);
  }

  @Override
  public Description matchAnnotation(AnnotationTree tree, VisitorState state) {
    return sortArrayElements(tree, state)
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  private Optional<Fix> sortArrayElements(AnnotationTree tree, VisitorState state) {
    /*
     * We loop over the array's attributes, trying to sort each array associated with a
     * non-blacklisted attribute. A single compound fix, if any, is returned.
     */
    return matcher
        .extractMatchingArguments(tree)
        .map(expr -> extractArray(expr).flatMap(arr -> suggestSorting(arr, state)))
        .flatMap(Optional::stream)
        .reduce(SuggestedFix.Builder::merge)
        .map(SuggestedFix.Builder::build);
  }

  private static Optional<NewArrayTree> extractArray(ExpressionTree expr) {
    if (expr.getKind() == Kind.ASSIGNMENT) {
      return extractArray(((AssignmentTree) expr).getExpression());
    }

    return Optional.of(expr)
        .filter(e -> e.getKind() == Kind.NEW_ARRAY)
        .map(NewArrayTree.class::cast);
  }

  private static Optional<SuggestedFix.Builder> suggestSorting(
      NewArrayTree array, VisitorState state) {
    if (array.getInitializers().size() < 2 || !canSort(array, state)) {
      /* There's nothing to sort, or we don't want to sort. */
      return Optional.empty();
    }

    List<? extends ExpressionTree> actualOrdering = array.getInitializers();
    ImmutableList<? extends ExpressionTree> desiredOrdering = doSort(actualOrdering, state);
    if (actualOrdering.equals(desiredOrdering)) {
      /* In the (presumably) common case the elements are already sorted. */
      return Optional.empty();
    }

    /* The elements aren't sorted. Suggest the sorted alternative. */
    String suggestion =
        desiredOrdering.stream()
            .map(expr -> SourceCode.treeToString(expr, state))
            .collect(joining(", ", "{", "}"));
    return Optional.of(SuggestedFix.builder().replace(array, suggestion));
  }

  private static boolean canSort(Tree array, VisitorState state) {
    Type type = ASTHelpers.getType(array);
    if (type == null) {
      return false;
    }

    Symtab symtab = state.getSymtab();
    Type elemType = state.getTypes().elemtype(type);

    /* For now we don't force sorting on numeric types. */
    return Stream.of(
            symtab.annotationType, symtab.classType, symtab.enumSym.type, symtab.stringType)
        .anyMatch(t -> ASTHelpers.isSubtype(elemType, t, state));
  }

  private static ImmutableList<? extends ExpressionTree> doSort(
      Iterable<? extends ExpressionTree> elements, VisitorState state) {
    // XXX: Perhaps we should use `Collator` with `.setStrength(Collator.PRIMARY)` and
    // `getCollationKey`. Not clear whether that's worth the hassle at this point.
    return ImmutableList.sortedCopyOf(
        comparing(
            e -> getStructure(e, state),
            Comparators.lexicographical(
                Comparators.lexicographical(
                    String.CASE_INSENSITIVE_ORDER.thenComparing(naturalOrder())))),
        elements);
  }

  /**
   * Extracts from the given array element expression the tokens on which sorting should be
   * performed. This approach disregards e.g. irrelevant whitespace. It also allows special
   * structure within string literals to be respected.
   */
  private static ImmutableList<ImmutableList<String>> getStructure(
      ExpressionTree array, VisitorState state) {
    ImmutableList.Builder<ImmutableList<String>> nodes = ImmutableList.builder();

    new TreeScanner<Void, Void>() {
      @Nullable
      @Override
      public Void visitIdentifier(IdentifierTree node, @Nullable Void ctx) {
        nodes.add(tokenize(node));
        return super.visitIdentifier(node, ctx);
      }

      @Nullable
      @Override
      public Void visitLiteral(LiteralTree node, @Nullable Void ctx) {
        nodes.add(tokenize(node));
        return super.visitLiteral(node, ctx);
      }

      @Nullable
      @Override
      public Void visitPrimitiveType(PrimitiveTypeTree node, @Nullable Void ctx) {
        nodes.add(tokenize(node));
        return super.visitPrimitiveType(node, ctx);
      }

      private ImmutableList<String> tokenize(Tree node) {
        /*
         * Tokens are split on `=` so that e.g. inline Spring property declarations are properly
         * sorted by key, then value.
         */
        return ImmutableList.copyOf(SourceCode.treeToString(node, state).split("=", -1));
      }
    }.scan(array, null);

    return nodes.build();
  }

  private static AnnotationAttributeMatcher createAnnotationAttributeMatcher(
      ErrorProneFlags flags) {
    return AnnotationAttributeMatcher.create(
        flags.getList(INCLUDED_ANNOTATIONS_FLAG), excludedAnnotations(flags));
  }

  private static ImmutableList<String> excludedAnnotations(ErrorProneFlags flags) {
    Set<String> exclusions = new HashSet<>();
    flags.getList(EXCLUDED_ANNOTATIONS_FLAG).ifPresent(exclusions::addAll);
    exclusions.addAll(BLACKLISTED_ANNOTATIONS);
    return ImmutableList.copyOf(exclusions);
  }
}
