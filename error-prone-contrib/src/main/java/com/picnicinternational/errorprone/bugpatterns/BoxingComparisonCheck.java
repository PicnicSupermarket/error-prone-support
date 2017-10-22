package com.picnicinternational.errorprone.bugpatterns;

import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;

import com.google.auto.service.AutoService;
import com.google.common.base.VerifyException;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.method.MethodMatchers.MethodClassMatcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree.JCMemberReference;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;

// XXX: Add more documentation. Explain how this is useful in the face of refactoring to more specific types.
// XXX: Change this checker's name?
@AutoService(BugChecker.class)
@BugPattern(
    name = "PrimitiveComparison",
    summary =
            "Ensure invocations of `Comparator#comparing{,Double,Int,Long}` match the return type"
                    + " of the provided function",
    linkType = LinkType.NONE,
    severity = SeverityLevel.WARNING,
    tags = StandardTags.LIKELY_ERROR,
    providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION
)
public class BoxingComparisonCheck extends BugChecker implements MethodInvocationTreeMatcher {
    private static final Matcher<ExpressionTree> STATIC_MATCH = getStaticTargetMatcher();
    private static final Matcher<ExpressionTree> INSTANCE_MATCH = getInstanceTargetMatcher();

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
        boolean isStatic = STATIC_MATCH.matches(tree, state);
        if (!isStatic && !INSTANCE_MATCH.matches(tree, state)) {
            return Description.NO_MATCH;
        }

        Type potentiallyBoxedType = getPotentiallyBoxedReturnType(tree.getArguments().get(0));
        if (potentiallyBoxedType == null) {
            return Description.NO_MATCH;
        }

        String actualMethodName = ASTHelpers.getSymbol(tree).getSimpleName().toString();
        String preferredMethodName = getPreferredMethod(state, potentiallyBoxedType, isStatic);
        if (actualMethodName.equals(preferredMethodName)) {
            return Description.NO_MATCH;
        }

        Description.Builder description = buildDescription(tree);
        tryFix(description, tree, preferredMethodName);
        return description.build();
    }

    private String getPreferredMethod(VisitorState state, Type cmpType, boolean isStatic) {
        Types types = state.getTypes();
        Symtab symtab = state.getSymtab();

        if (types.isSubtype(cmpType, symtab.intType)) {
            return isStatic ? "comparingInt" : "thenComparingInt";
        }

        if (types.isSubtype(cmpType, symtab.longType)) {
            return isStatic ? "comparingLong" : "thenComparingLong";
        }

        if (types.isSubtype(cmpType, symtab.doubleType)) {
            return isStatic ? "comparingDouble" : "thenComparingDouble";
        }

        return isStatic ? "comparing" : "thenComparing";
    }

    @CheckForNull
    private Type getPotentiallyBoxedReturnType(ExpressionTree tree) {
        switch (tree.getKind()) {
            case LAMBDA_EXPRESSION:
                /* Return the lambda expression's actual return type. */
                return ASTHelpers.getType(((LambdaExpressionTree) tree).getBody());
            case MEMBER_REFERENCE:
                /* Return the method's declared return type. */
                // XXX: Very fragile. Do better.
                Type subType2 = ((JCMemberReference) tree).referentType;
                return ((MethodType) subType2).getReturnType();
            default:
                /* This appears to be a genuine `{,ToInt,ToLong,ToDouble}Function`. */
                return null;
        }
    }

    private void tryFix(
            Description.Builder description,
            MethodInvocationTree tree,
            String preferredMethodName) {
        ExpressionTree expr = tree.getMethodSelect();
        switch (expr.getKind()) {
            case IDENTIFIER:
                SuggestedFix.Builder fix = SuggestedFix.builder();
                fix.addStaticImport(
                        java.util.Comparator.class.getName() + '.' + preferredMethodName);
                fix.replace(expr, preferredMethodName);
                description.addFix(fix.build());
                return;
            case MEMBER_SELECT:
                MemberSelectTree ms = (MemberSelectTree) tree.getMethodSelect();
                description.addFix(
                        SuggestedFix.replace(ms, ms.getExpression() + "." + preferredMethodName));
                return;
            default:
                throw new VerifyException("Unexpected type of expression: " + expr.getKind());
        }
    }

    private static Matcher<ExpressionTree> getStaticTargetMatcher() {
        MethodClassMatcher clazz = staticMethod().onClass(Comparator.class.getName());

        return anyMatch(
                unaryMethod(clazz, "comparingInt", ToIntFunction.class),
                unaryMethod(clazz, "comparingLong", ToLongFunction.class),
                unaryMethod(clazz, "comparingDouble", ToDoubleFunction.class),
                unaryMethod(clazz, "comparing", Function.class));
    }

    private static Matcher<ExpressionTree> getInstanceTargetMatcher() {
        MethodClassMatcher instance = instanceMethod().onDescendantOf(Comparator.class.getName());

        return anyMatch(
                unaryMethod(instance, "thenComparingInt", ToIntFunction.class),
                unaryMethod(instance, "thenComparingLong", ToLongFunction.class),
                unaryMethod(instance, "thenComparingDouble", ToDoubleFunction.class),
                unaryMethod(instance, "thenComparing", Function.class));
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    private static Matcher<ExpressionTree> anyMatch(Matcher<ExpressionTree>... matchers) {
        return (ExpressionTree t, VisitorState s) ->
                Stream.of(matchers).anyMatch(m -> m.matches(t, s));
    }

    private static Matcher<ExpressionTree> unaryMethod(
            MethodClassMatcher classMatcher, String name, Class<?> paramType) {
        return classMatcher.named(name).withParameters(paramType.getName());
    }
}
