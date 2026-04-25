package tech.picnic.errorprone.guidelines.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
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
import com.google.common.collect.ImmutableMap;
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
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.code.Types.AdaptFailure;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import java.util.Iterator;
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
    return isCompatible(beforeMethods, afterMethods, state)
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
   * Tells whether every pair of {@link BeforeTemplate} and {@link AfterTemplate} methods admits a
   * single substitution of the class-level type variables that simultaneously makes each
   * before-parameter a subtype of the corresponding (by name) after-parameter and the
   * after-return-type a subtype of the before-return-type.
   */
  private static boolean isCompatible(
      ImmutableList<MethodTree> beforeMethods,
      ImmutableList<MethodTree> afterMethods,
      VisitorState state) {
    Types types = state.getTypes();
    TypeSymbol carrier = state.getSymtab().objectType.tsym;
    for (MethodTree beforeMethod : beforeMethods) {
      for (MethodTree afterMethod : afterMethods) {
        if (!isCompatible(beforeMethod, afterMethod, carrier, types)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Tells whether one before/after pair admits such a substitution.
   *
   * <p>The matched parameter pairs and the return-type pair are packaged into a synthetic n-ary
   * {@link ClassType} carrier and fed through a single {@link Types#adapt} invocation, which throws
   * {@link AdaptFailure} if a class-level type variable would have to map to two different types
   * within this pair.
   */
  private static boolean isCompatible(
      MethodTree beforeMethod, MethodTree afterMethod, TypeSymbol carrier, Types types) {
    ImmutableMap<String, Type> beforeParamsByName =
        beforeMethod.getParameters().stream()
            .collect(
                toImmutableMap(p -> p.getName().toString(), p -> ASTHelpers.getSymbol(p).type));

    ImmutableList.Builder<Type> afterTupleBuilder = ImmutableList.builder();
    ImmutableList.Builder<Type> beforeTupleBuilder = ImmutableList.builder();
    for (VariableTree afterParam : afterMethod.getParameters()) {
      Type matchedBefore = beforeParamsByName.get(afterParam.getName().toString());
      if (matchedBefore != null) {
        afterTupleBuilder.add(ASTHelpers.getSymbol(afterParam).type);
        beforeTupleBuilder.add(matchedBefore);
      }
    }
    afterTupleBuilder.add(ASTHelpers.getSymbol(afterMethod).getReturnType());
    beforeTupleBuilder.add(ASTHelpers.getSymbol(beforeMethod).getReturnType());

    ImmutableList<Type> afterTuple = afterTupleBuilder.build();
    ImmutableList<Type> beforeTuple = beforeTupleBuilder.build();

    ListBuffer<Type> from = new ListBuffer<>();
    ListBuffer<Type> to = new ListBuffer<>();
    try {
      types.adapt(
          new ClassType(Type.noType, List.from(afterTuple), carrier),
          new ClassType(Type.noType, List.from(beforeTuple), carrier),
          from,
          to);
    } catch (AdaptFailure e) {
      /* The before-method forces conflicting bindings of a class-level type variable. */
      return false;
    }
    List<Type> fromList = from.toList();
    List<Type> toList = to.toList();

    if (!areBindingsValid(fromList, toList, types)) {
      return false;
    }

    /*
     * After substitution, each before-parameter must be a subtype of the corresponding
     * after-parameter (contravariant), and the after-return-type must be a subtype of the
     * before-return-type (covariant).
     */
    int returnIndex = afterTuple.size() - 1;
    for (int i = 0; i < returnIndex; i++) {
      if (!types.isSubtype(beforeTuple.get(i), types.subst(afterTuple.get(i), fromList, toList))) {
        return false;
      }
    }
    return types.isSubtype(
        types.subst(afterTuple.get(returnIndex), fromList, toList), beforeTuple.get(returnIndex));
  }

  /**
   * Rejects substitutions that map a class-level type variable to a <em>different</em> type
   * variable, or that violate a type variable's declared upper bound. {@link Types#adapt} does not
   * verify either constraint itself; identity bindings, where the same type variable appears on
   * both sides, are accepted as no-ops.
   */
  private static boolean areBindingsValid(List<Type> fromList, List<Type> toList, Types types) {
    Iterator<Type> fromIt = fromList.iterator();
    Iterator<Type> toIt = toList.iterator();
    while (fromIt.hasNext()) {
      TypeVar tv = (TypeVar) fromIt.next();
      Type val = toIt.next();
      if (val instanceof TypeVar valVar) {
        if (!valVar.tsym.equals(tv.tsym)) {
          /* Substituting one type variable for another does not yield a concrete type. */
          return false;
        }
        /* Identity binding; trivially satisfies the upper bound. */
      } else if (!types.isSubtype(val, tv.getUpperBound())) {
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
