package tech.picnic.errorprone.guidelines.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static com.google.errorprone.predicates.TypePredicates.isExactType;
import static java.util.Objects.requireNonNullElse;
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
import com.google.errorprone.predicates.TypePredicate;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.suppliers.Suppliers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.CapturedType;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.WildcardType;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeVariable;
import tech.picnic.errorprone.utils.MoreASTHelpers;

/**
 * A {@link BugChecker} that flags Refaster template methods whose declared return type is not the
 * most specific denotable type, as inferred from the method's return expression(s).
 */
// XXX: Finalize reviewing the identification tests w.r.t. naming and scope.
// XXX: Simplify the replacement tests.
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
  private static final Matcher<ExpressionTree> REFASTER_ANY_OF =
      staticMethod().onClass(Refaster.class.getCanonicalName()).named("anyOf");
  private static final Matcher<Tree> ALSO_NEGATION = hasAnnotation(AlsoNegation.class);
  private static final TypePredicate IS_BOXED_VOID = isExactType(Suppliers.JAVA_LANG_VOID_TYPE);
  private static final TypePredicate IS_BOXED_BOOLEAN =
      isExactType(Suppliers.JAVA_LANG_BOOLEAN_TYPE);

  /** Instantiates a new {@link RefasterReturnType} instance. */
  public RefasterReturnType() {}

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (!REFASTER_TEMPLATE_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    /*
     * Determine the most specific denotable return type that is also compatible with Refaster DSL
     * and JSpecify requirements.
     */
    Type suggestion =
        mapVoidTypeArgsToWildcard(
            ensureAlsoNegationCompatibility(
                toDenotable(inferReturnType(tree, state), /* isTypeArg= */ false, state), state),
            state);

    /*
     * Suggest that the method's return type is replaced only if a more specific denotable type is
     * found. Note that in case of intersection types the declared return type may be a *subtype* of
     * the inferred return type. In that case we don't suggest a change either.
     */
    return state.getTypes().isSuperType(suggestion, ASTHelpers.getSymbol(tree).getReturnType())
        ? Description.NO_MATCH
        : describeMatch(tree, createFix(tree, suggestion, state));
  }

  /**
   * Infers the most specific return type that may be associated with the given method definition,
   * without considering whether this type is denotable.
   */
  private static Type inferReturnType(MethodTree tree, VisitorState state) {
    ImmutableList<Type> returnTypes =
        MoreASTHelpers.findDirectReturnStatements(tree).stream()
            .map(ReturnTree::getExpression)
            .flatMap(
                t ->
                    REFASTER_ANY_OF.matches(t, state)
                        ? ((MethodInvocationTree) t).getArguments().stream()
                        : Stream.of(t))
            .map(ASTHelpers::getType)
            .filter(Objects::nonNull)
            .collect(toImmutableList());

    Types types = state.getTypes();
    return returnTypes.stream()
        .map(types::boxedTypeOrType)
        .reduce(types::lub)
        .map(l -> unboxLubIfPossible(l, returnTypes, state))
        .orElseGet(() -> requireNonNullElse(ASTHelpers.getType(tree.getReturnType()), Type.noType));
  }

  /**
   * Returns the primitive variant of the given LUB type, if the types from which it is derived are
   * all primitive.
   */
  private static Type unboxLubIfPossible(
      Type lub, ImmutableList<Type> allTypes, VisitorState state) {
    Types types = state.getTypes();
    Type unboxedLub = types.unboxedType(lub);
    return unboxedLub.isPrimitive() && allTypes.stream().allMatch(Type::isPrimitive)
        ? unboxedLub
        : lub;
  }

  /**
   * Attempts to convert a possibly non-denotable type into its most specific denotable
   * approximation. Returns {@link Optional#empty()} if no denotable type can be constructed.
   */
  // XXX: Also handle arrays; TEST and recurse!!!.
  private static Type toDenotable(Type type, boolean isTypeArg, VisitorState state) {
    Types types = state.getTypes();
    if (type.tsym.isAnonymous() || isLocalClass(type)) {
      // XXX: Here, should we also consider interfaces? Add some tests to document current behavior.
      return toDenotable(types.supertype(type), isTypeArg, state);
    }

    Symtab symtab = state.getSymtab();
    return switch (type) {
      case CapturedType capturedType ->
          isTypeArg
              ? toDenotable(capturedType.wildcard, isTypeArg, state)
              : toDenotable(capturedType.getUpperBound(), isTypeArg, state);
      case ClassType classType ->
          mapTypeArguments(classType, arg -> toDenotable(arg, /* isTypeArg= */ true, state));
      case WildcardType wildcard -> toDenotable(wildcard, isTypeArg, state);
      case NoType ignored -> symtab.voidType;
      case TypeVariable ignored -> type;
      case PrimitiveType ignored -> type;
      case ArrayType ignored -> type;
      default -> symtab.objectType;
    };
  }

  private static Type toDenotable(WildcardType type, boolean isTypeArg, VisitorState state) {
    Type bound =
        type.kind == BoundKind.EXTENDS
            ? toDenotable(type.type, isTypeArg, state)
            : state.getSymtab().objectType;
    return isTypeArg ? new WildcardType(bound, type.kind, type.tsym) : bound;
  }

  private static boolean isLocalClass(Type type) {
    return type.tsym instanceof ClassSymbol cs && cs.owner.getKind() == ElementKind.METHOD;
  }

  /**
   * Replaces the given type with a primitive {@code boolean}, if it is a boxed {@link Boolean} in
   * the context of an {@link AlsoNegation} rule.
   *
   * <p>This to meet a Refaster DSL requirement.
   */
  private static Type ensureAlsoNegationCompatibility(Type type, VisitorState state) {
    return IS_BOXED_BOOLEAN.apply(type, state) && hasAlsoNegation(state)
        ? state.getSymtab().booleanType
        : type;
  }

  private static boolean hasAlsoNegation(VisitorState state) {
    ClassTree enclosingClass = state.findEnclosing(ClassTree.class);
    return enclosingClass != null
        && enclosingClass.getMembers().stream()
            .anyMatch(member -> ALSO_NEGATION.matches(member, state));
  }

  /**
   * Replaces all {@link Void} type arguments in the given type with {@code ? extends Void}.
   *
   * <p>This does not meaningfully change the type (as {@link Void} is {@code final}), but in
   * combination with {@link #createFix(MethodTree, Type, VisitorState)} it ensures that suggested
   * types are JSpecify-compatible.
   */
  private static Type mapVoidTypeArgsToWildcard(Type type, VisitorState state) {
    return type instanceof ClassType classType
        ? mapTypeArguments(
            classType,
            arg ->
                IS_BOXED_VOID.apply(arg, state)
                    ? new WildcardType(arg, BoundKind.EXTENDS, state.getSymtab().boundClass)
                    : arg)
        : type;
  }

  private static SuggestedFix createFix(MethodTree tree, Type newReturnType, VisitorState state) {
    SuggestedFix.Builder fix = SuggestedFix.builder();

    /* Make sure that the new return type satisfies Error Prone's `VoidMissingNullable` check. */
    String replacementType =
        SuggestedFixes.prettyType(state, fix, newReturnType)
            .replace(
                "Void",
                '@'
                    + SuggestedFixes.qualifyType(state, fix, "org.jspecify.annotations.Nullable")
                    + " Void");

    return fix.replace(tree.getReturnType(), replacementType).build();
  }

  private static ClassType mapTypeArguments(ClassType classType, Function<Type, Type> typeMapper) {
    return new ClassType(
        classType.getEnclosingType(),
        classType.getTypeArguments().stream().map(typeMapper).collect(List.collector()),
        classType.tsym,
        classType.getMetadata());
  }
}
