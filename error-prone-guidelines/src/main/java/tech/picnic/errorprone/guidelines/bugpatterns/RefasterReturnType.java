package tech.picnic.errorprone.guidelines.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.lang.model.type.TypeKind;
import tech.picnic.errorprone.utils.MoreASTHelpers;

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
  private static final Matcher<Tree> HAS_ALSO_NEGATION = hasAnnotation(AlsoNegation.class);
  private static final Supplier<Type> VOID_TYPE_SUPPLIER =
      VisitorState.memoize(state -> state.getTypeFromString(Void.class.getCanonicalName()));

  /** Instantiates a new {@link RefasterReturnType} instance. */
  public RefasterReturnType() {}

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (!REFASTER_TEMPLATE_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Optional<Type> lub = getLubOfReturnExpressionTypes(tree, state);
    if (lub.isEmpty()) {
      return Description.NO_MATCH;
    }

    Type inferredType = lub.orElseThrow();
    if (!isDenotable(inferredType, /* isTypeArg= */ false)) {
      // XXX: In this case, can we do better than just giving up? For example, if the inferred type
      // is an anonymous class, we could suggest using the nearest non-anonymous superclass or
      // interface that is a supertype of all return expression types.
      return Description.NO_MATCH;
    }

    @Var Type typeForSuggestion = mapVoidTypeArgsToWildcard(inferredType, state);

    // @AlsoNegation semantically requires a primitive boolean return type. When the
    // LUB auto-boxes boolean to Boolean, unbox it back to the primitive.
    // XXX: More generally, `Types#lub` auto-boxes primitive types. Consider unboxing the
    // inferred type whenever all return expression types are the same primitive type.
    if (enclosingClassHasAlsoNegation(state)) {
      Type unboxed = state.getTypes().unboxedType(typeForSuggestion);
      if (unboxed.getKind() != TypeKind.NONE) {
        typeForSuggestion = unboxed;
      }
    }

    Type declaredReturnType = ASTHelpers.getSymbol(tree).getReturnType();
    if (state.getTypes().isSameType(typeForSuggestion, declaredReturnType)) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder fix = SuggestedFix.builder();
    @Var String prettyType = SuggestedFixes.prettyType(state, fix, typeForSuggestion);

    if (containsVoidType(inferredType, state)) {
      String nullable = SuggestedFixes.qualifyType(state, fix, "org.jspecify.annotations.Nullable");
      prettyType = prettyType.replace("Void", '@' + nullable + " Void");
    }

    fix.replace(tree.getReturnType(), prettyType);
    return describeMatch(tree, fix.build());
  }

  private static boolean enclosingClassHasAlsoNegation(VisitorState state) {
    ClassTree enclosingClass = state.findEnclosing(ClassTree.class);
    return enclosingClass != null
        && enclosingClass.getMembers().stream()
            .anyMatch(member -> HAS_ALSO_NEGATION.matches(member, state));
  }

  private static final Matcher<ExpressionTree> REFASTER_ANY_OF =
      staticMethod().onClass(Refaster.class.getCanonicalName()).named("anyOf");

  // XXX: Rename.
  private static Optional<Type> getLubOfReturnExpressionTypes(MethodTree tree, VisitorState state) {
    return MoreASTHelpers.findDirectReturnStatements(tree).stream()
        .map(ReturnTree::getExpression)
        .flatMap(
            t ->
                REFASTER_ANY_OF.matches(t, state)
                    ? ((MethodInvocationTree) t).getArguments().stream()
                    : Stream.of(t))
        .map(ASTHelpers::getType)
        .filter(Objects::nonNull)
        .reduce(state.getTypes()::lub);
  }

  private static boolean containsVoidType(Type type, VisitorState state) {
    return state.getTypes().isSameType(type, VOID_TYPE_SUPPLIER.get(state))
        || type.getTypeArguments().stream().anyMatch(arg -> containsVoidType(arg, state));
  }

  private static Type mapVoidTypeArgsToWildcard(Type type, VisitorState state) {
    List<Type> typeArgs = type.getTypeArguments();
    if (typeArgs.isEmpty()) {
      return type;
    }

    Type voidType = VOID_TYPE_SUPPLIER.get(state);
    @Var boolean changed = false;
    ListBuffer<Type> newArgs = new ListBuffer<>();
    for (Type arg : typeArgs) {
      if (ASTHelpers.isSameType(arg, voidType, state)) {
        newArgs.add(new Type.WildcardType(arg, BoundKind.EXTENDS, state.getSymtab().boundClass));
        changed = true;
      } else {
        newArgs.add(arg);
      }
    }

    if (!changed) {
      return type;
    }

    Type.ClassType classType = (Type.ClassType) type;
    return new Type.ClassType(classType.getEnclosingType(), newArgs.toList(), classType.tsym);
  }

  // XXX: Instead of giving up on suggesting a fix when the inferred type is not denotable, we could
  // try to
  // find the most specific denotable supertype of the inferred type and suggest that instead, as
  // long as it is
  // still more specific than the declared return type. Any non-denotable type paramter could at
  // least be replaced with `?.
  private static boolean isDenotable(Type type, boolean isTypeArg) {
    TypeKind kind = type.getKind();
    if (kind == TypeKind.NULL || kind == TypeKind.ERROR || kind == TypeKind.NONE) {
      return false;
    }
    if (!isTypeArg && type.isCompound()) {
      // XXX: Compound types are denotable as type parameters, but `SuggestedFixes.prettyType` does
      // not seems to be able to pretty-print them in all cases. Review whether we can do better.
      // XXX: Drop this comment or the `isTypeArg` parameter if we can eventually support compound
      // types in all contexts.
      return false;
    }
    if (type instanceof Type.CapturedType) {
      return false;
    }
    if (type.tsym != null && type.tsym.isAnonymous()) {
      return false;
    }
    for (Type typeArg : type.getTypeArguments()) {
      if (!isDenotable(typeArg, /* isTypeArg= */ true)) {
        // XXX: We might want to allow certain non-denotable type arguments, such as captured
        // wildcard type arguments, if they are part of an otherwise denotable return type.
        return false;
      }
    }
    if (type instanceof Type.WildcardType wildcardType) {
      Type bound = wildcardType.type;
      return bound == null || isDenotable(bound, isTypeArg);
    }

    return true;
  }
}
