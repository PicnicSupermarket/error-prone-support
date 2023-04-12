package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * A {@link BugChecker} that flags Refaster expression templates for which the
 * {@code @AfterTemplate} return type is not a subtype of the `@BeforeTemplate` return type.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Refaster templates should have invariant return types",
    link = BUG_PATTERNS_BASE_URL + "RefasterExpressionTypeInvariance",
    linkType = CUSTOM,
    severity = WARNING,
    tags = LIKELY_ERROR)
public final class RefasterExpressionTypeInvariance extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<Tree> BEFORE_TEMPLATE_METHOD = hasAnnotation(BeforeTemplate.class);
  private static final Matcher<Tree> AFTER_TEMPLATE_METHOD = hasAnnotation(AfterTemplate.class);
  private static final Matcher<ExpressionTree> REFASTER_ANY_OF =
      staticMethod().onClass(Refaster.class.getName()).named("anyOf");

  // XXX: False positive in `OptionalMap`. First write a rule to simplify such case.
  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    Optional<Type> afterTemplateResultType = getAfterTemplateResultType(tree, state);
    if (afterTemplateResultType.isEmpty()) {
      return Description.NO_MATCH;
    }

    ImmutableSet<Type> beforeTemplateResultTypes = getBeforeTemplateResultTypes(tree, state);
    if (beforeTemplateResultTypes.isEmpty()) {
      return Description.NO_MATCH;
    }

    Type replacementType = afterTemplateResultType.orElseThrow();
    if (areAllSuperTypeOf(replacementType, beforeTemplateResultTypes, state)) {
      return Description.NO_MATCH;
    }

    Type alternativeReplacementType =
        (replacementType.isPrimitive()
            ? state.getTypes().boxedTypeOrType(replacementType)
            : state.getTypes().unboxedType(replacementType));

    boolean requiresBoxingOrUnboxing =
        areAllSuperTypeOf(alternativeReplacementType, beforeTemplateResultTypes, state);

    // @Considerations

    // enum ConsiderationType:
    // MAY_CAUSE_BOXING
    // MAY_CAUSE_UNBOXING
    // MAY_CAUSE_COMPILATION_ERROR\

    if (requiresBoxingOrUnboxing) {
      if (replacementType.isPrimitive()) {
        // MAY_CAUSE_UNBOXING
        // XXX: Flag this using a custom annotation.
        return Description.NO_MATCH;
      }

      // MAY_CAUSE_
      // XXX: Flag this using a custom annotation.
      return Description.NO_MATCH;
    }

    // MAY_CAUSE_COMPILATION_ERROR
    // XXX: Flag this using a custom annotation.
    // XXX: This case contains false positives; maybe leave out for now?
    return describeMatch(tree);
  }

  private static boolean areAllSuperTypeOf(
      Type subType, ImmutableSet<Type> superTypes, VisitorState state) {
    return superTypes.stream().allMatch(type -> state.getTypes().isSuperType(type, subType));
  }

  private static ImmutableSet<Type> getBeforeTemplateResultTypes(
      ClassTree tree, VisitorState state) {
    return tree.getMembers().stream()
        .filter(m -> BEFORE_TEMPLATE_METHOD.matches(m, state))
        .map(m -> getResultType((MethodTree) m, state))
        .collect(toImmutableSet());
  }

  private static Optional<Type> getAfterTemplateResultType(ClassTree tree, VisitorState state) {
    return tree.getMembers().stream()
        .filter(m -> AFTER_TEMPLATE_METHOD.matches(m, state))
        .map(m -> getResultType((MethodTree) m, state))
        .reduce(state.getTypes()::lub);
  }

  // XXX: A variant of this pattern also occurs a few times inside Error Prone; contribute upstream.
  // XXX: Copied from `JUnitValueSource`.
  private static Type getResultType(MethodTree methodTree, VisitorState state) {
    List<Type> resultTypes = new ArrayList<>();
    new TreeScanner<@Nullable Void, @Nullable Void>() {
      @Override
      public @Nullable Void visitClass(ClassTree node, @Nullable Void unused) {
        /* Ignore `return` statements inside anonymous/local classes. */
        return null;
      }

      @Override
      public @Nullable Void visitReturn(ReturnTree node, @Nullable Void unused) {
        if (REFASTER_ANY_OF.matches(node.getExpression(), state)) {
          for (ExpressionTree arg : ((MethodInvocationTree) node.getExpression()).getArguments()) {
            resultTypes.add(ASTHelpers.getType(arg));
          }
        } else if (ASTHelpers.stripParentheses(node.getExpression()).getKind()
            != Tree.Kind.NULL_LITERAL) {
          resultTypes.add(ASTHelpers.getType(node.getExpression()));
        }

        return super.visitReturn(node, unused);
      }

      @Override
      public @Nullable Void visitLambdaExpression(
          LambdaExpressionTree node, @Nullable Void unused) {
        /* Ignore `return` statements inside lambda expressions. */
        return null;
      }
    }.scan(methodTree, null);

    return resultTypes.stream()
        .reduce(state.getTypes()::lub)
        .orElseGet(() -> state.getSymtab().botType);
  }
}
