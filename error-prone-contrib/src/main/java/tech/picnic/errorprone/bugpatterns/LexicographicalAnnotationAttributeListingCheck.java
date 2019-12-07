package tech.picnic.errorprone.bugpatterns;

import static java.util.stream.Collectors.joining;

import com.google.auto.service.AutoService;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A {@link BugChecker} which flags annotation array listings which aren't sorted lexicographically.
 */
// XXX: Add more documentation. Explain that sorting reduces chances of conflicts and simplifies
// their resolution when they do happen.
@AutoService(BugChecker.class)
@BugPattern(
    name = "LexicographicalAnnotationAttributeListing",
    summary = "Where possible, sort annotation array attributes lexicographically",
    linkType = LinkType.NONE,
    severity = SeverityLevel.SUGGESTION,
    tags = StandardTags.STYLE,
    providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION)
public final class LexicographicalAnnotationAttributeListingCheck extends BugChecker
    implements AnnotationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final ImmutableSet<String> BLACKLISTED_ANNOTATIONS =
      ImmutableSet.of(
          // XXX: unless JsonPropertyOrder#alphabetic is true...
          "com.fasterxml.jackson.annotation.JsonPropertyOrder#value",
          "io.swagger.annotations.ApiImplicitParams#value",
          "javax.xml.bind.annotation.XmlType#propOrder");
  private static final String FLAG_PREFIX = "LexicographicalAnnotationAttributeListing:";
  private static final String INCLUDED_ANNOTATIONS_FLAG = FLAG_PREFIX + "Includes";
  private static final String EXCLUDED_ANNOTATIONS_FLAG = FLAG_PREFIX + "Excludes";

  private final AnnotationAttributeMatcher matcher;

  /** Instantiates default {@link LexicographicalAnnotationAttributeListingCheck}. */
  public LexicographicalAnnotationAttributeListingCheck() {
    this(ErrorProneFlags.empty());
  }

  /**
   * Instantiates a customized {@link LexicographicalAnnotationAttributeListingCheck}.
   *
   * @param flags Any provided command line flags.
   */
  public LexicographicalAnnotationAttributeListingCheck(ErrorProneFlags flags) {
    this.matcher = createAnnotationAttributeMatcher(flags);
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
    return this.matcher
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
            .map(expr -> Util.treeToString(expr, state))
            .collect(joining(", ", "{", "}"));
    return Optional.of(SuggestedFix.builder().replace(array, suggestion));
  }

  private static boolean canSort(NewArrayTree array, VisitorState state) {
    Type elemType = state.getTypes().elemtype(ASTHelpers.getType(array));
    Symtab symtab = state.getSymtab();

    /* For now we don't force sorting on numeric types. */
    return Stream.of(
            symtab.annotationType, symtab.classType, symtab.enumSym.type, symtab.stringType)
        .anyMatch(t -> ASTHelpers.isSubtype(elemType, t, state));
  }

  private static ImmutableList<? extends ExpressionTree> doSort(
      List<? extends ExpressionTree> elements, VisitorState state) {
    return ImmutableList.sortedCopyOf(
        Comparator.comparing(
            e -> getStructure(e, state),
            Comparators.lexicographical(
                Comparators.lexicographical(Comparator.<String>naturalOrder()))),
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
      @Override
      public Void visitIdentifier(IdentifierTree node, Void ctx) {
        nodes.add(tokenize(node));
        return super.visitIdentifier(node, ctx);
      }

      @Override
      public Void visitLiteral(LiteralTree node, Void ctx) {
        nodes.add(tokenize(node));
        return super.visitLiteral(node, ctx);
      }

      @Override
      public Void visitPrimitiveType(PrimitiveTypeTree node, Void ctx) {
        nodes.add(tokenize(node));
        return super.visitPrimitiveType(node, ctx);
      }

      private ImmutableList<String> tokenize(Tree node) {
        /*
         * Tokens are split on `=` so that e.g. inline Spring property declarations are properly
         * sorted by key, then value.
         */
        return ImmutableList.copyOf(Util.treeToString(node, state).split("=", -1));
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
