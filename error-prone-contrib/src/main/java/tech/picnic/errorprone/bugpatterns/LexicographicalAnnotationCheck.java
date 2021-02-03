package tech.picnic.errorprone.bugpatterns;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.util.List;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;

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
//  https://github.com/google/error-prone/pull/2125/files
  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    List<Attribute.Compound> rawAttributes = ASTHelpers.getSymbol(tree).getRawAttributes();
    if (rawAttributes.length() < 2) {
      return Description.NO_MATCH;
    }

    ImmutableList<Attribute.Compound> collect = rawAttributes.stream()
            .sorted(Comparator.comparing(e -> e.type.tsym.getQualifiedName().toString()))
            .collect(toImmutableList());

    boolean doAnnotationsMatch = IntStream.range(0, rawAttributes.size())
            .allMatch(i -> collect.get(i).equals(rawAttributes.get(i)));

    Attribute.Compound value = rawAttributes.get(0).getValue();




    return Description.NO_MATCH;
  }

//    private Optional<Fix> sortArrayElements(AnnotationTree tree, VisitorState state) {
  //    /*
  //     * We loop over the array's attributes, trying to sort each array associated with a
  //     * non-blacklisted attribute. A single compound fix, if any, is returned.
  //     */
  //    return matcher
  //        .extractMatchingArguments(tree)
  //        .map(expr -> extractArray(expr).flatMap(arr -> suggestSorting(arr, state)))
  //        .flatMap(Optional::stream)
  //        .reduce(SuggestedFix.Builder::merge)
  //        .map(SuggestedFix.Builder::build);
  //  }

//    private static Optional<SuggestedFix.Builder> suggestSorting(
//        NewArrayTree array, VisitorState state) {
  //    if (array.getInitializers().size() < 2 || !canSort(array, state)) {
  //      /* There's nothing to sort, or we don't want to sort. */
  //      return Optional.empty();
  //    }
  //
  //    List<? extends ExpressionTree> actualOrdering = array.getInitializers();
  //    ImmutableList<? extends ExpressionTree> desiredOrdering = doSort(actualOrdering, state);
  //    if (actualOrdering.equals(desiredOrdering)) {
  //      /* In the (presumably) common case the elements are already sorted. */
  //      return Optional.empty();
  //    }
  //
  //    /* The elements aren't sorted. Suggest the sorted alternative. */
  //    String suggestion =
  //        desiredOrdering.stream()
  //            .map(expr -> Util.treeToString(expr, state))
  //            .collect(joining(", ", "{", "}"));
  //    return Optional.of(SuggestedFix.builder().replace(array, suggestion));
  //  }

  //  private static boolean canSort(Tree array, VisitorState state) {
  //    Type type = ASTHelpers.getType(array);
  //    if (type == null) {
  //      return false;
  //    }
  //
  //    Symtab symtab = state.getSymtab();
  //    Type elemType = state.getTypes().elemtype(type);
  //
  //    /* For now we don't force sorting on numeric types. */
  //    return Stream.of(
  //            symtab.annotationType, symtab.classType, symtab.enumSym.type, symtab.stringType)
  //        .anyMatch(t -> ASTHelpers.isSubtype(elemType, t, state));
  //  }

//    private static ImmutableList<? extends ExpressionTree> doSort(
//            Iterable<? extends ExpressionTree> elements, VisitorState state) {
//      // XXX: Perhaps we should use `Collator` with `.setStrength(Collator.PRIMARY)` and
//      // `getCollationKey`. Not clear whether that's worth the hassle at this point.
//      return ImmutableList.sortedCopyOf(
//          Comparator.comparing(
//              e -> getStructure(e, state),
//              Comparators.lexicographical(
//                  Comparators.lexicographical(
//                      String.CASE_INSENSITIVE_ORDER.thenComparing(naturalOrder())))),
//          elements);
//
//      }
}
