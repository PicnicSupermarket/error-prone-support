package com.picnicinternational.errorprone.bugpatterns;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree.Kind;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

// XXX: Also flag/drop trailing commas?
@AutoService(BugChecker.class)
@BugPattern(
    name = "CanonicalAnnotationSyntax",
    summary = "Omit redundant syntax from annotation declarations",
    severity = SeverityLevel.SUGGESTION,
    tags = StandardTags.STYLE,
    providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION
)
public final class CanonicalAnnotationSyntaxCheck extends BugChecker
        implements AnnotationTreeMatcher {
    private static final ImmutableSet<BiFunction<AnnotationTree, VisitorState, Optional<Fix>>>
            FIX_FACTORIES =
                    ImmutableSet.of(
                            CanonicalAnnotationSyntaxCheck::dropRedundantParentheses,
                            CanonicalAnnotationSyntaxCheck::dropRedundantValueAttribute,
                            CanonicalAnnotationSyntaxCheck::dropRedundantCurlies);

    @Override
    public Description matchAnnotation(AnnotationTree tree, VisitorState state) {
        return FIX_FACTORIES
                .stream()
                .map(op -> op.apply(tree, state))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .map(fix -> buildDescription(tree).addFix(fix).build())
                .orElse(Description.NO_MATCH);
    }

    private static Optional<Fix> dropRedundantParentheses(AnnotationTree tree, VisitorState state) {
        if (!tree.getArguments().isEmpty()) {
            /* Parentheses are necessary. */
            return Optional.empty();
        }

        String src = state.getSourceForNode(tree);
        if (src == null) {
            /* Without the source code there's not much we can do. */
            return Optional.empty();
        }

        int parenIndex = src.indexOf('(');
        if (parenIndex < 0) {
            /* There are no redundant parentheses. */
            return Optional.empty();
        }

        return Optional.of(SuggestedFix.replace(tree, src.substring(0, parenIndex)));
    }

    private static Optional<Fix> dropRedundantValueAttribute(
            AnnotationTree tree, VisitorState state) {
        List<? extends ExpressionTree> args = tree.getArguments();
        if (args.size() != 1) {
            /* The `value` attribute, if specified, cannot be dropped. */
            return Optional.empty();
        }

        ExpressionTree arg = args.get(0);
        if (arg.getKind() != Kind.ASSIGNMENT) {
            /* Evidently `value` isn't assigned to explicitly. */
            return Optional.empty();
        }

        AssignmentTree assignment = (AssignmentTree) arg;
        ExpressionTree variable = assignment.getVariable();
        if (variable.getKind() != Kind.IDENTIFIER
                || !((IdentifierTree) variable).getName().contentEquals("value")
                || state.getSourceForNode(variable) == null) {
            /* This is not an explicit assignment to the `value` attribute. */
            return Optional.empty();
        }

        /* Replace the assignment with (the simplified representation of) just its value. */
        ExpressionTree expr = assignment.getExpression();
        return Optional.of(
                SuggestedFix.replace(
                        arg, simplifyAttributeValue(expr, state).orElseGet(expr::toString)));
    }

    private static Optional<Fix> dropRedundantCurlies(AnnotationTree tree, VisitorState state) {
        List<SuggestedFix.Builder> fixes = new ArrayList<>();
        for (ExpressionTree arg : tree.getArguments()) {
            /*
             * We'll try to simplify each assignment's RHS; for non-assignment we'll try to simplify
             * the expression as a whole.
             */
            ExpressionTree value =
                    (arg.getKind() == Kind.ASSIGNMENT)
                            ? ((AssignmentTree) arg).getExpression()
                            : arg;

            /* Store a fix for each expression that was successfully simplified. */
            simplifyAttributeValue(value, state)
                    .ifPresent(expr -> fixes.add(SuggestedFix.builder().replace(value, expr)));
        }

        return fixes.stream().reduce(SuggestedFix.Builder::merge).map(SuggestedFix.Builder::build);
    }

    private static Optional<String> simplifyAttributeValue(
            ExpressionTree expr, VisitorState state) {
        if (expr.getKind() != Kind.NEW_ARRAY) {
            /* There are no curly braces to be dropped here. */
            return Optional.empty();
        }

        NewArrayTree newArray = (NewArrayTree) expr;
        if (newArray.getInitializers().size() != 1) {
            /* Only singleton arrays can be simplified. */
            return Optional.empty();
        }

        /* Return the expression describing the array's sole element. */
        return Optional.of(newArray.getInitializers().get(0).toString());
    }
}
