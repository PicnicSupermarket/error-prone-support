package tech.picnic.errorprone.guidelines.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static com.google.errorprone.predicates.TypePredicates.isExactType;
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
  private static final Matcher<ExpressionTree> REFASTER_ANY_OF =
      staticMethod().onClass(Refaster.class.getCanonicalName()).named("anyOf");
  private static final Matcher<Tree> HAS_ALSO_NEGATION = hasAnnotation(AlsoNegation.class);

  /** Instantiates a new {@link RefasterReturnType} instance. */
  public RefasterReturnType() {}

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (!REFASTER_TEMPLATE_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    return inferReturnType(tree, state)
        .flatMap(type -> toDenotable(type, /* isTypeArg= */ false, state))
        .map(type -> ensureAlsoNegationCompatibility(type, state))
        .map(type -> mapVoidTypeArgsToWildcard(type, state))
        .filter(
            type -> !state.getTypes().isSameType(type, ASTHelpers.getSymbol(tree).getReturnType()))
        .map(type -> describeMatch(tree, createFix(tree, type, state)))
        .orElse(Description.NO_MATCH);
  }

  private static Optional<Type> inferReturnType(MethodTree tree, VisitorState state) {
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
        .map(l -> unboxLubIfPossible(l, returnTypes, state));
  }

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
  // XXX: Also handle arrays.
  private static Optional<Type> toDenotable(Type type, boolean isTypeArg, VisitorState state) {
    TypeKind kind = type.getKind();
    if (kind == TypeKind.NULL || kind == TypeKind.ERROR || kind == TypeKind.NONE) {
      return Optional.empty();
    }

    // XXX: Add tests for union types (e.g. from multi-catch) and intersection types (e.g. from type
    // inference with multiple bounds).
    // XXX: Add test with local classes and anonymous classes that implement multiple interfaces.
    if ((!isTypeArg && type.isCompound()) || (type.tsym != null && type.tsym.isAnonymous())) {
      // XXX: Instead of only checking the first direct supertype, consider all direct supertypes.
      // Any matches the declared return type, prefer that one.
      return toDenotable(state.getTypes().supertype(type), isTypeArg, state);
    }

    Symtab symtab = state.getSymtab();
    return switch (type) {
      case CapturedType capturedType -> {
        if (!isTypeArg) {
          yield Optional.empty();
        }

        WildcardType wildcard = capturedType.wildcard;
        yield (wildcard.kind == BoundKind.UNBOUND
                ? Optional.of(symtab.objectType)
                : toDenotable(wildcard.type, isTypeArg, state))
            .map(bound -> new WildcardType(bound, wildcard.kind, symtab.boundClass));
      }
      case ClassType classType ->
          Optional.of(
              mapTypeArguments(
                  classType,
                  arg ->
                      toDenotable(arg, /* isTypeArg= */ true, state)
                          .orElseGet(
                              () ->
                                  new WildcardType(
                                      symtab.objectType, BoundKind.UNBOUND, symtab.boundClass))));

      case WildcardType wildcard when wildcard.type != null ->
          toDenotable(wildcard.type, isTypeArg, state)
              .map(
                  bound ->
                      state.getTypes().isSameType(bound, wildcard.type)
                          ? wildcard
                          : new WildcardType(bound, wildcard.kind, wildcard.tsym));
      default -> Optional.of(type);
    };
  }

  /**
   * Replaces the given type with a primitive {@code boolean}, if it is a boxed {@link Boolean} in
   * the context of an {@link AlsoNegation} rule.
   *
   * <p>This to meet a Refaster DSL requirement.
   */
  private static Type ensureAlsoNegationCompatibility(Type type, VisitorState state) {
    Types types = state.getTypes();
    Symtab symtab = state.getSymtab();
    Type boxedBoolean = Suppliers.JAVA_LANG_BOOLEAN_TYPE.get(state);

    return types.isSameType(type, boxedBoolean) && enclosingClassHasAlsoNegation(state)
        ? symtab.booleanType
        : type;
  }

  private static boolean enclosingClassHasAlsoNegation(VisitorState state) {
    ClassTree enclosingClass = state.findEnclosing(ClassTree.class);
    return enclosingClass != null
        && enclosingClass.getMembers().stream()
            .anyMatch(member -> HAS_ALSO_NEGATION.matches(member, state));
  }

  /**
   * Replaces all {@link Void} type arguments in the given type to {@code ? extends Void}.
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
                ASTHelpers.isSameType(arg, Suppliers.JAVA_LANG_VOID_TYPE.get(state), state)
                    ? new WildcardType(arg, BoundKind.EXTENDS, state.getSymtab().boundClass)
                    : arg)
        : type;
  }

  private static SuggestedFix createFix(MethodTree tree, Type newReturnType, VisitorState state) {
    SuggestedFix.Builder fix = SuggestedFix.builder();

    String prettyType = SuggestedFixes.prettyType(state, fix, newReturnType);

    /* Make sure that the new return type satisfies Error Prone's `VoidMissingNullable` check. */
    String replacementType =
        referencesVoidType(newReturnType, state)
            ? prettyType.replace(
                "Void",
                '@'
                    + SuggestedFixes.qualifyType(state, fix, "org.jspecify.annotations.Nullable")
                    + " Void")
            : prettyType;

    return fix.replace(tree.getReturnType(), replacementType).build();
  }

  private static boolean referencesVoidType(Type type, VisitorState state) {
    return referencesType(type, isExactType(Suppliers.JAVA_LANG_VOID_TYPE), state);
  }

  private static boolean referencesType(Type type, TypePredicate predicate, VisitorState state) {
    if (type instanceof WildcardType wildcard && wildcard.type != null) {
      return referencesType(wildcard.type, predicate, state);
    }

    return predicate.apply(type, state)
        || type.getTypeArguments().stream().anyMatch(arg -> referencesType(arg, predicate, state));
  }

  private static ClassType mapTypeArguments(ClassType classType, Function<Type, Type> typeMapper) {
    return new ClassType(
        classType.getEnclosingType(),
        classType.getTypeArguments().stream().map(typeMapper).collect(List.collector()),
        classType.tsym,
        classType.getMetadata());
  }
}
