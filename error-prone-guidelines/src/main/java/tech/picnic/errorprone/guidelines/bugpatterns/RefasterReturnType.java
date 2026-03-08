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
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.CapturedType;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.WildcardType;
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
  private static final Supplier<Type> VOID_TYPE_SUPPLIER =
      VisitorState.memoize(state -> state.getTypeFromString(Void.class.getCanonicalName()));

  /** Instantiates a new {@link RefasterReturnType} instance. */
  public RefasterReturnType() {}

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (!REFASTER_TEMPLATE_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Optional<Type> inferredType = inferReturnType(tree, state);
    if (inferredType.isEmpty()) {
      return Description.NO_MATCH;
    }

    Optional<Type> denotableType =
        toDenotable(inferredType.orElseThrow(), /* isTypeArg= */ false, state);
    if (denotableType.isEmpty()) {
      return Description.NO_MATCH;
    }

    Type denotable = denotableType.orElseThrow();
    @Var Type typeForSuggestion = mapVoidTypeArgsToWildcard(denotable, state);

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

    if (containsVoidType(denotable, state)) {
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

  private static Optional<Type> inferReturnType(MethodTree tree, VisitorState state) {
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
    Type voidType = VOID_TYPE_SUPPLIER.get(state);
    return type instanceof ClassType classType
        ? mapTypeArguments(
            classType,
            arg ->
                ASTHelpers.isSameType(arg, voidType, state)
                    ? new WildcardType(arg, BoundKind.EXTENDS, state.getSymtab().boundClass)
                    : arg)
        : type;
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

  private static ClassType mapTypeArguments(ClassType classType, Function<Type, Type> typeMapper) {
    return new ClassType(
        classType.getEnclosingType(),
        classType.getTypeArguments().stream().map(typeMapper).collect(List.collector()),
        classType.tsym,
        classType.getMetadata());
  }
}
