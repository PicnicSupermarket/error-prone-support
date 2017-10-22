package com.picnicinternational.errorprone.bugpatterns;

import static java.util.Comparator.naturalOrder;

import com.google.auto.service.AutoService;
import com.google.common.base.Joiner;
import com.google.common.collect.Comparators;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
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
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

// XXX: Add documentation. Explain that sorting reduces chances of conflicts and simplifies their resolution when they do happen.
// XXX: Add support for inclusions and exclusions.
@AutoService(BugChecker.class)
@BugPattern(
    name = "LexicographicalAnnotationAttributeListing",
    summary = "Where possible, sort annotation array attributes lexicographically",
    linkType = LinkType.NONE,
    severity = SeverityLevel.SUGGESTION,
    tags = StandardTags.STYLE
)
public final class LexicographicalAnnotationAttributeListingCheck extends BugChecker
        implements AnnotationTreeMatcher {
    @Override
    public Description matchAnnotation(AnnotationTree tree, VisitorState state) {
        return sortArrayElements(tree, state)
                .map(fix -> buildDescription(tree).addFix(fix).build())
                .orElse(Description.NO_MATCH);
    }

    private Optional<Fix> sortArrayElements(AnnotationTree tree, VisitorState state) {
        List<SuggestedFix.Builder> fixes = new ArrayList<>();

        /*
         * For each argument, see whether it contains an unsorted array and emit a suggested fix if
         * so.
         */
        for (ExpressionTree arg : tree.getArguments()) {
            extractArray(arg).flatMap(arr -> suggestSorting(arr, state)).ifPresent(fixes::add);
        }

        /* Return a single compound fix, if any. */
        return fixes.stream().reduce(SuggestedFix.Builder::merge).map(SuggestedFix.Builder::build);
    }

    private Optional<NewArrayTree> extractArray(ExpressionTree expr) {
        if (expr.getKind() == Kind.ASSIGNMENT) {
            return extractArray(((AssignmentTree) expr).getExpression());
        }

        return Optional.of(expr)
                .filter(e -> e.getKind() == Kind.NEW_ARRAY)
                .map(NewArrayTree.class::cast);
    }

    private Optional<SuggestedFix.Builder> suggestSorting(NewArrayTree array, VisitorState state) {
        if (array.getInitializers().size() < 2 || !canSort(array, state)) {
            /* There's nothing to sort, or we don't want to sort. */
            return Optional.empty();
        }

        /* We're sorting based on each expression's string representation. */
        List<String> expressions = new ArrayList<>();
        for (ExpressionTree expr : array.getInitializers()) {
            expressions.add(expr.toString());
        }

        if (Comparators.isInOrder(expressions, naturalOrder())) {
            /* In the (presumably) common case the elements are already sorted. */
            return Optional.empty();
        }

        /* The elements aren't sorted yet. Do so now. */
        expressions.sort(naturalOrder());

        /* Re-assemble the expressions into an array. */
        StringBuilder sb = new StringBuilder("{");
        Joiner.on(", ").appendTo(sb, expressions);
        sb.append("}");

        return Optional.of(SuggestedFix.builder().replace(array, sb.toString()));
    }

    private boolean canSort(NewArrayTree array, VisitorState state) {
        Type elemType = state.getTypes().elemtype(ASTHelpers.getType(array));
        Symtab symtab = state.getSymtab();

        /* For now we don't force sorting on numeric types. */
        return Stream.of(
                        symtab.annotationType,
                        symtab.classType,
                        symtab.enumSym.type,
                        symtab.stringType)
                .anyMatch(t -> ASTHelpers.isSubtype(elemType, t, state));
    }
}
