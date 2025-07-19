package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
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
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.tools.JavaFileObject.Kind;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.utils.AnnotationAttributeMatcher;
import tech.picnic.errorprone.utils.Flags;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags and removes duplicate listings inside declared annotations.
 *
 * <p><b>Example:</b>
 *
 * <pre>
 *   {@code @JsonPropertyOrder({ "a", "b", "a" })}
 * </pre>
 *
 * <p><b>Will be flagged and fixed to the following:</b>
 *
 * <pre>
 *   {@code @JsonPropertyOrder({ "a", "b"})}
 * </pre>
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Duplicate listings within an annotation are often a mistake.",
    link = BUG_PATTERNS_BASE_URL + "DuplicateAnnotationAttributeListing",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = STYLE)
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
    return removeDuplicateAnnotationEntries(tree, state)
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  private Optional<Fix> removeDuplicateAnnotationEntries(AnnotationTree tree, VisitorState state) {
    return matcher
        .extractMatchingArguments(tree)
        .map(expr -> extractArray(expr).flatMap(arr -> removeDuplicates(arr, tree, state)))
        .flatMap(Optional::stream)
        .reduce(SuggestedFix.Builder::merge)
        .map(SuggestedFix.Builder::build);
  }

  private static Optional<NewArrayTree> extractArray(ExpressionTree expr) {
    return expr instanceof AssignmentTree assignment
        ? extractArray(assignment.getExpression())
        : Optional.of(expr).filter(NewArrayTree.class::isInstance).map(NewArrayTree.class::cast);
  }

  private static Optional<SuggestedFix.Builder> removeDuplicates(
      NewArrayTree array, AnnotationTree tree, VisitorState state) {
    if (array.getInitializers().size() < 2) {
      /* There's only one element, no duplicates are expected. */
      return Optional.empty();
    }

    List<? extends ExpressionTree> actualEntries = array.getInitializers();

    ImmutableSet<Tree> nonDuplicateEntries = getNonDuplicateEntries(array, state);

    if (actualEntries.size() == nonDuplicateEntries.size()) {
      /* In the (presumably) common case that no duplicates are found. */
      return Optional.empty();
    }

    String prefix = shouldOmitBrackets(tree, nonDuplicateEntries) ? "" : "{";
    String suffix = prefix.isBlank() ? "" : "}";

    String suggestion =
        nonDuplicateEntries.stream()
            .map(expr -> extractName(state, expr))
            .collect(joining(", ", prefix, suffix));
    return Optional.of(SuggestedFix.builder().replace(array, suggestion));
  }

  /**
   * Extracts from the given array element expression a distinct set of {@link Tree nodes} omitting
   * the duplicate entries.
   *
   * @implNote Annotations are a special case in which we want to make sure we get the distinct
   *     value of the full annotation expression instead of the annotation name. For example, we
   *     want to flag the following as a duplicate annotation entry:
   *     <pre>
   *       {@code @Foo(anns = { @Bar("a"), @Bar("a") })}
   *      </pre>
   *     but not:
   *     <pre>
   *       {@code @Foo(anns = { @Bar("a"), @Bar("b") })}
   *      </pre>
   *     To do this, we conditionally visit the identifiers, literals and primitive types of the
   *     original annotation but not those of the annotation entry being visited, given that the
   *     listings of each nested annotation will be the visited through the {@link
   *     AnnotationTreeMatcher}.
   */
  private static ImmutableSet<Tree> getNonDuplicateEntries(
      ExpressionTree array, VisitorState state) {
    // Helper collections to identify when a node has been visited by its identifier, to avoid
    // including duplicate nodes.
    Set<String> visitedAnnotations = new HashSet<>();
    Set<String> visitedIdentifiers = new HashSet<>();
    Set<String> visitedLiterals = new HashSet<>();
    Set<String> visitPrimitiveType = new HashSet<>();

    ImmutableSet.Builder<Tree> nodes = ImmutableSet.builder();

    new TreeScanner<@Nullable Void, Set<String>>() {
      @Override
      public @Nullable Void visitAnnotation(AnnotationTree node, Set<String> visitedAnnotations) {
        String annotation = SourceCode.treeToString(node, state);
        if (!visitedAnnotations.contains(annotation)) {
          nodes.add(node);
        }
        visitedAnnotations.add(annotation);

        return super.visitAnnotation(node, visitedAnnotations);
      }

      @Override
      public @Nullable Void visitIdentifier(IdentifierTree node, Set<String> visitedAnnotations) {
        String identifier = node.getName().toString();

        if (visitedAnnotations.isEmpty() && !visitedIdentifiers.contains(identifier)) {
          nodes.add(node);
        }
        visitedIdentifiers.add(identifier);

        return super.visitIdentifier(node, visitedAnnotations);
      }

      @Override
      public @Nullable Void visitLiteral(LiteralTree node, Set<String> visitedAnnotations) {
        String literal = String.valueOf(ASTHelpers.constValue(node));

        if (visitedAnnotations.isEmpty() && !visitedLiterals.contains(literal)) {
          nodes.add(node);
        }
        visitedLiterals.add(literal);

        return super.visitLiteral(node, visitedAnnotations);
      }

      @Override
      public @Nullable Void visitPrimitiveType(
          PrimitiveTypeTree node, Set<String> visitedAnnotations) {
        String primitiveTypeKind = node.getPrimitiveTypeKind().toString();

        if (visitedAnnotations.isEmpty() && !visitPrimitiveType.contains(primitiveTypeKind)) {
          nodes.add(node);
        }
        visitPrimitiveType.add(primitiveTypeKind);

        return super.visitPrimitiveType(node, visitedAnnotations);
      }
    }.scan(array, visitedAnnotations);

    return nodes.build();
  }

  // In the case of entries defined for the special attribute name value, if after removing the
  // duplicates in a listing array, there's only one element left, then we can omit the brackets.
  private static boolean shouldOmitBrackets(
      AnnotationTree tree, ImmutableSet<Tree> nonDuplicateEntries) {
    boolean hasValueKeyword =
        tree.getArguments().stream()
            .filter(arg -> arg.getKind() == Tree.Kind.ASSIGNMENT)
            .map(AssignmentTree.class::cast)
            .map(AssignmentTree::getVariable)
            .map(IdentifierTree.class::cast)
            .map(IdentifierTree::getName)
            .anyMatch(name -> name.contentEquals("value"));
    return nonDuplicateEntries.size() == 1 && hasValueKeyword;
  }

  private static String extractName(VisitorState state, Tree expr) {
    String exprString = SourceCode.treeToString(expr, state);

    Symbol symbol = ASTHelpers.getSymbol(expr);
    if (symbol != null) {
      return (symbol.getKind() == INTERFACE || symbol.getKind() == CLASS)
          ? exprString + Kind.CLASS.extension
          : exprString;
    }

    return exprString;
  }

  private static AnnotationAttributeMatcher createAnnotationAttributeMatcher(
      ErrorProneFlags flags) {
    return AnnotationAttributeMatcher.create(
        flags.get(INCLUDED_ANNOTATIONS_FLAG).isPresent()
            ? Optional.of(flags.getListOrEmpty(INCLUDED_ANNOTATIONS_FLAG))
            : Optional.empty(),
        Flags.getList(flags, EXCLUDED_ANNOTATIONS_FLAG));
  }
}
