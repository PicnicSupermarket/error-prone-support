package tech.picnic.errorprone.guidelines.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSetMultimap.toImmutableSetMultimap;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.isType;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.matchers.MultiMatcher.MultiMatchResult;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that suggests that Refaster rules have the {@link
 * PossibleSourceIncompatibility} annotation if and only if it identifies at least one scenario in
 * which application of the rule could yield uncompilable code.
 *
 * <p>Currently, a Refaster rule is possibly source-incompatible if:
 *
 * <ul>
 *   <li>an {@link AfterTemplate} parameter type is not a supertype of every corresponding {@link
 *       BeforeTemplate} parameter type, meaning that the replacement may break compilation at
 *       parameter locations that previously accepted a broader type, or
 *   <li>an {@link AfterTemplate} return type is not a subtype of every {@link BeforeTemplate}
 *       return type, meaning that the replacement may break compilation at call sites that depend
 *       on the narrower type.
 * </ul>
 */
// XXX: As-is, this check relies on the return types declared by template methods. The
// `RefasterReturnType` check ensures that such return types are as specific as possible, but we
// could further reduce false-negatives by instead analyzing the return expressions of template
// methods to infer more specific non-denotable return types.
// XXX: As-is, this check does not account for type variable substitution in wildcard bounds (e.g.
// `List<? extends T>` vs. `List<? extends Void>`). Extend `inferTypeVarMappings` to recurse into
// wildcard bounds if cases arise in practice.
// XXX: As-is, this check unconditionally flags incompatible functional parameters. We may be more
// refined if we consider `@Matches(IsLambdaExpressionOrMethodReference.class)`. See
// `JUnitToAssertJRules` and `TestNGToAssertJRules` for examples involving
// `Executable`/`ThrowingSupplier`/`ThrowingCallable` and `ThrowingRunnable`/`ThrowingCallable`,
// respectively. (Though there's nuance here involving
// `@(Not)Matches(ThrowsCheckedException.class)`; see the `MonoFromSupplier` rule.)
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Refaster rules should be annotated with `@PossibleSourceIncompatibility` if and only if "
            + " they are possibly source-incompatible",
    link = BUG_PATTERNS_BASE_URL + "RefasterSourceCompatibility",
    linkType = CUSTOM,
    severity = WARNING,
    tags = LIKELY_ERROR)
public final class RefasterSourceCompatibility extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String POSSIBLE_SOURCE_INCOMPATIBILITY_ANNOTATION =
      "tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility";
  private static final Matcher<Tree> IS_BEFORE_TEMPLATE = hasAnnotation(BeforeTemplate.class);
  private static final Matcher<Tree> IS_AFTER_TEMPLATE = hasAnnotation(AfterTemplate.class);
  private static final MultiMatcher<Tree, AnnotationTree> HAS_POSSIBLE_SOURCE_INCOMPATIBILITY =
      annotations(AT_LEAST_ONE, isType(POSSIBLE_SOURCE_INCOMPATIBILITY_ANNOTATION));

  /** Instantiates a new {@link RefasterSourceCompatibility} instance. */
  public RefasterSourceCompatibility() {}

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    ImmutableList<MethodTree> beforeMethods = getMatchingMethods(tree, IS_BEFORE_TEMPLATE, state);
    if (beforeMethods.isEmpty()) {
      /* Fast path: this is not a Refaster rule. */
      return dropAnnotationIfPresent(tree, state);
    }

    ImmutableList<MethodTree> afterMethods = getMatchingMethods(tree, IS_AFTER_TEMPLATE, state);
    Symbol classSymbol = ASTHelpers.getSymbol(tree);
    return hasCompatibleParameterTypes(beforeMethods, afterMethods, classSymbol, state)
            && hasCompatibleReturnTypes(beforeMethods, afterMethods, classSymbol, state)
        ? dropAnnotationIfPresent(tree, state)
        : addAnnotationIfAbsent(tree, state);
  }

  private static ImmutableList<MethodTree> getMatchingMethods(
      ClassTree tree, Matcher<Tree> matcher, VisitorState state) {
    return tree.getMembers().stream()
        .filter(member -> matcher.matches(member, state))
        .map(MethodTree.class::cast)
        .collect(toImmutableList());
  }

  /**
   * Tells whether all given {@code @AfterTemplate} methods have parameter types that are supertypes
   * of each of the corresponding {@code @BeforeTemplate} parameter types.
   *
   * <p>A before-template parameter type is considered compatible with an after-template parameter
   * type if the former is a subtype of the latter, accounting for the fact that unconstrained
   * class-level type variables in the after-template parameter type can be substituted with the
   * concrete types from the before-template parameter type.
   */
  private static boolean hasCompatibleParameterTypes(
      ImmutableList<MethodTree> beforeMethods,
      ImmutableList<MethodTree> afterMethods,
      Symbol classSymbol,
      VisitorState state) {
    ImmutableSetMultimap<String, Type> beforeTypes =
        beforeMethods.stream()
            .flatMap(m -> m.getParameters().stream())
            .collect(
                toImmutableSetMultimap(
                    p -> p.getName().toString(), p -> ASTHelpers.getSymbol(p).type));

    Types types = state.getTypes();
    for (MethodTree afterMethod : afterMethods) {
      for (VariableTree afterParam : afterMethod.getParameters()) {
        Type afterParamType = ASTHelpers.getSymbol(afterParam).type;
        if (!beforeTypes.get(afterParam.getName().toString()).stream()
            .allMatch(
                beforeType ->
                    types.isSubtype(beforeType, afterParamType)
                        || substituteClassTypeVars(afterParamType, beforeType, classSymbol, types)
                            .filter(t -> types.isSubtype(beforeType, t))
                            .isPresent())) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Tells whether all given {@code @AfterTemplate} methods have a return type that is a subtype of
   * each of the given {@code @BeforeTemplate} methods.
   *
   * <p>A return type is considered compatible if the after-template return type is a subtype of the
   * before-template return type, accounting for the fact that unconstrained class-level type
   * variables in the after-template return type can be substituted with the concrete types from the
   * before-template return type.
   *
   * @implNote Note that this method does not need to implement custom logic to handle {@code void}
   *     return types (associated with "block templates"): Refaster rules cannot combine {@code
   *     void} after-templates with non-{@code void} before-templates, and while the reverse is
   *     supported, it is not in general safe to replace a sequence of statements with a {@code
   *     return} statement.
   */
  private static boolean hasCompatibleReturnTypes(
      ImmutableList<MethodTree> beforeMethods,
      ImmutableList<MethodTree> afterMethods,
      Symbol classSymbol,
      VisitorState state) {
    Types types = state.getTypes();
    for (MethodTree afterMethod : afterMethods) {
      Type afterReturnType = ASTHelpers.getSymbol(afterMethod).getReturnType();
      for (MethodTree beforeMethod : beforeMethods) {
        Type beforeReturnType = ASTHelpers.getSymbol(beforeMethod).getReturnType();
        if (!types.isSubtype(afterReturnType, beforeReturnType)
            && substituteClassTypeVars(afterReturnType, beforeReturnType, classSymbol, types)
                .filter(t -> types.isSubtype(t, beforeReturnType))
                .isEmpty()) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Attempts to substitute class-level type variables in {@code afterType} with corresponding types
   * inferred from {@code referenceType}, returning an {@link Optional} containing the substituted
   * type, or an empty {@link Optional} if no valid substitution can be found.
   */
  private static Optional<Type> substituteClassTypeVars(
      Type afterType, Type referenceType, Symbol classSymbol, Types types) {
    Map<TypeVar, Type> substitution = new LinkedHashMap<>();
    // XXX: Mutations of this guard are unkillable: with `EQUAL_IF` (always `Optional.empty()`),
    // no test detects the absence of substitution when the initial `isSubtype` check already
    // passes; with `EQUAL_ELSE` (always computing `subst`), the empty `from`/`to` lists leave
    // `afterType` unchanged, which already failed the `isSubtype` check.
    if (!inferTypeVarMappings(afterType, referenceType, classSymbol, substitution, types)
        || substitution.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(
        types.subst(
            afterType, List.<Type>from(substitution.keySet()), List.from(substitution.values())));
  }

  /**
   * Recursively walks {@code afterType} and {@code referenceType} in tandem to collect mappings
   * from class-level type variables in {@code afterType} to corresponding concrete types in {@code
   * referenceType}. Returns {@code false} if a contradiction is encountered (e.g., a type variable
   * would need to map to two different types, or its bounds are violated).
   */
  private static boolean inferTypeVarMappings(
      Type afterType,
      Type referenceType,
      Symbol classSymbol,
      Map<TypeVar, Type> substitution,
      Types types) {
    if (afterType instanceof TypeVar tv && tv.tsym.owner.equals(classSymbol)) {
      /*
       * `afterType` is directly a class-level type variable. Reject mappings to other type
       * variables: substituting one type variable for another does not yield a concrete type, and
       * can mask incompatibilities (e.g. `Flux<T>` vs. `Flux<S>` in a class with both `T` and
       * `S` as type parameters).
       */
      if (referenceType instanceof TypeVar) {
        return false;
      }
      Type existing = substitution.get(tv);
      if (existing != null) {
        // XXX: Swapping these arguments is unkillable because `isSameType` is symmetric.
        return types.isSameType(existing, referenceType);
      }
      if (!types.isSubtype(referenceType, tv.getUpperBound())) {
        /* `referenceType` does not satisfy the type variable's upper bound. */
        return false;
      }
      substitution.put(tv, referenceType);
      return true;
    }

    /* Recurse into the type arguments of both types. */
    List<Type> afterArgs = afterType.getTypeArguments();
    List<Type> refArgs = referenceType.getTypeArguments();
    // XXX: Skipping this guard is unkillable: if `afterArgs` is empty (a non-parameterized type
    // with no TypeVar), there is nothing to substitute, so `afterType` is returned unchanged, and
    // the outer `isSubtype` check already failed it. A size mismatch likewise prevents meaningful
    // substitution, and the outer check catches any remaining incompatibility.
    if (afterArgs.isEmpty() || afterArgs.size() != refArgs.size()) {
      return false;
    }
    for (int i = 0; i < afterArgs.size(); i++) {
      // XXX: Returning `true` on failure is unkillable: a partial/inconsistent substitution
      // produces an incorrect type from `types.subst()`, which the outer `isSubtype` check
      // then rejects, yielding the same final outcome.
      if (!inferTypeVarMappings(
          afterArgs.get(i), refArgs.get(i), classSymbol, substitution, types)) {
        return false;
      }
    }
    return true;
  }

  private Description dropAnnotationIfPresent(ClassTree tree, VisitorState state) {
    MultiMatchResult<AnnotationTree> annotationMatch = getIncompatibilityAnnotation(tree, state);
    if (!annotationMatch.matches()) {
      return Description.NO_MATCH;
    }

    AnnotationTree annotation = annotationMatch.onlyMatchingNode();
    return describeMatch(annotation, SourceCode.deleteWithTrailingWhitespace(annotation, state));
  }

  private Description addAnnotationIfAbsent(ClassTree tree, VisitorState state) {
    MultiMatchResult<AnnotationTree> annotationMatch = getIncompatibilityAnnotation(tree, state);
    if (annotationMatch.matches()) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder fix = SuggestedFix.builder();
    String annotation =
        SuggestedFixes.qualifyType(state, fix, POSSIBLE_SOURCE_INCOMPATIBILITY_ANNOTATION);
    return describeMatch(tree, fix.prefixWith(tree, '@' + annotation + ' ').build());
  }

  private static MultiMatchResult<AnnotationTree> getIncompatibilityAnnotation(
      ClassTree tree, VisitorState state) {
    return HAS_POSSIBLE_SOURCE_INCOMPATIBILITY.multiMatchResult(tree, state);
  }
}
