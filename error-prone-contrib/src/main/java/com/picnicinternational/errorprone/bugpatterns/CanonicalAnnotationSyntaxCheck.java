package com.picnicinternational.errorprone.bugpatterns;

import static com.google.common.base.Verify.verify;

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
import com.sun.source.tree.Tree.Kind;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

// XXX: Drop redundant curly braces.
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
                            CanonicalAnnotationSyntaxCheck::dropRedundantValueAttribute);

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
            verify(
                    arg.getKind() == Kind.IDENTIFIER,
                    "Unexpected type of expression: %s" + arg.getKind());
            return Optional.empty();
        }

        ExpressionTree variable = ((AssignmentTree) arg).getVariable();
        if (variable.getKind() != Kind.IDENTIFIER
                || !((IdentifierTree) variable).getName().contentEquals("value")
                || state.getSourceForNode(variable) == null) {
            /* This is not an explicit assignment to the `value` attribute. */
            return Optional.empty();
        }

        /* Replace the assignment with just its value. */
        return Optional.of(
                SuggestedFix.replace(arg, ((AssignmentTree) arg).getExpression().toString()));
    }
}
