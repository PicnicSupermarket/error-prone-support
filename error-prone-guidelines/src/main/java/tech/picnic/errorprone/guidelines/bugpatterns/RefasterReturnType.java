package tech.picnic.errorprone.guidelines.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;
import javax.lang.model.type.TypeKind;
import org.jspecify.annotations.Nullable;

/**
 * A {@link BugChecker} that flags Refaster template methods whose declared return type is not the
 * most specific denotable type, as inferred from the method's return expression(s).
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Refaster template methods should declare the most specific return type that is denotable",
    link = BUG_PATTERNS_BASE_URL + "RefasterReturnType",
    linkType = CUSTOM,
    severity = WARNING,
    tags = LIKELY_ERROR)
public final class RefasterReturnType extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<Tree> REFASTER_TEMPLATE_METHOD =
      anyOf(hasAnnotation(BeforeTemplate.class), hasAnnotation(AfterTemplate.class));

  /** Instantiates a new {@link RefasterReturnType} instance. */
  public RefasterReturnType() {}

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (!REFASTER_TEMPLATE_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Type declaredReturnType = ASTHelpers.getSymbol(tree).getReturnType();
    if (declaredReturnType.getKind() == TypeKind.VOID) {
      return Description.NO_MATCH;
    }

    ImmutableList<Type> returnTypes = collectReturnExpressionTypes(tree);
    if (returnTypes.isEmpty()) {
      return Description.NO_MATCH;
    }

    Type inferredType =
        returnTypes.size() == 1
            ? returnTypes.getFirst()
            : state.getTypes().lub(List.from(returnTypes));

    if (!isDenotable(inferredType)) {
      return Description.NO_MATCH;
    }

    if (!state.getTypes().isSubtype(inferredType, declaredReturnType)
        || state.getTypes().isSameType(inferredType, declaredReturnType)) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder fix = SuggestedFix.builder();
    String prettyType = SuggestedFixes.prettyType(state, fix, inferredType);
    fix.replace(tree.getReturnType(), prettyType);
    return describeMatch(tree, fix.build());
  }

  private static ImmutableList<Type> collectReturnExpressionTypes(MethodTree tree) {
    ImmutableList.Builder<Type> types = ImmutableList.builder();
    new TreeScanner<@Nullable Void, @Nullable Void>() {
      @Override
      public @Nullable Void visitReturn(ReturnTree node, @Nullable Void unused) {
        if (node.getExpression() != null) {
          Type type = ASTHelpers.getType(node.getExpression());
          if (type != null) {
            types.add(type);
          }
        }
        return super.visitReturn(node, null);
      }

      @Override
      public @Nullable Void visitClass(ClassTree node, @Nullable Void unused) {
        return null;
      }

      @Override
      public @Nullable Void visitLambdaExpression(
          LambdaExpressionTree node, @Nullable Void unused) {
        return null;
      }
    }.scan(tree.getBody(), null);
    return types.build();
  }

  private static boolean isDenotable(Type type) {
    TypeKind kind = type.getKind();
    if (kind == TypeKind.NULL || kind == TypeKind.ERROR || kind == TypeKind.NONE) {
      return false;
    }
    if (type.isCompound()) {
      return false;
    }
    return type.tsym == null || !type.tsym.isAnonymous();
  }
}
