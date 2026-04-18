package tech.picnic.errorprone.guidelines.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
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
import com.sun.tools.javac.code.Type;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that suggests that Refaster rules have the {@link
 * tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility} annotation if and only
 * if it identifies at least one scenario in which application of the rule could yield uncompilable
 * code.
 *
 * <p>Currently, a Refaster rule is possibly source-incompatible if:
 *
 * <ul>
 *   <li>an {@link AfterTemplate} return type is not a subtype of every {@link BeforeTemplate}
 *       return type, meaning that the replacement may break compilation at call sites that depend
 *       on the narrower type, or
 *   <li>an {@link AfterTemplate} parameter type is not a supertype of every corresponding {@link
 *       BeforeTemplate} parameter type, meaning that the replacement may break compilation at
 *       argument positions that previously accepted a broader type.
 * </ul>
 */
// XXX: As-is, this rule relies on the return types declared by template methods. The
// `RefasterReturnType` check ensures that such return types are as specific as possible, but we
// could further reduce false-negatives by instead analyzing the return expressions of template
// methods to infer more specific non-denotable return types.
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
      // XXX: Removing this guard does not change observable behavior: if `beforeMethods` is empty,
      // both compatibility checks vacuously return `true`, yielding the same result. This guard is
      // retained as a performance optimization.
      return dropAnnotationIfPresent(tree, state);
    }

    ImmutableList<MethodTree> afterMethods = getMatchingMethods(tree, IS_AFTER_TEMPLATE, state);
    return hasCompatibleReturnTypes(beforeMethods, afterMethods, state)
            && hasCompatibleParameterTypes(beforeMethods, afterMethods, state)
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
   * Tells whether all given {@code @AfterTemplate} methods have a return type that is a subtype of
   * each of the given {@code @BeforeTemplate} methods.
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
      VisitorState state) {
    for (MethodTree afterMethod : afterMethods) {
      Type afterReturnType = ASTHelpers.getSymbol(afterMethod).getReturnType();
      for (MethodTree beforeMethod : beforeMethods) {
        Type beforeReturnType = ASTHelpers.getSymbol(beforeMethod).getReturnType();
        if (!state.getTypes().isSubtype(afterReturnType, beforeReturnType)) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Tells whether all given {@code @AfterTemplate} methods have parameter types that are supertypes
   * of each of the corresponding {@code @BeforeTemplate} parameter types.
   *
   * <p>Correspondence is determined by parameter name, consistent with how Refaster binds template
   * variables. For each {@code @AfterTemplate} parameter named {@code n}, every {@code
   * @BeforeTemplate} parameter also named {@code n} must have a type that is a subtype of the
   * after-template parameter's type. Parameters that have no same-named counterpart in a given
   * before-method are skipped for that method.
   */
  private static boolean hasCompatibleParameterTypes(
      ImmutableList<MethodTree> beforeMethods,
      ImmutableList<MethodTree> afterMethods,
      VisitorState state) {
    for (MethodTree afterMethod : afterMethods) {
      for (VariableTree afterParam : afterMethod.getParameters()) {
        Type afterParamType = ASTHelpers.getSymbol(afterParam).type;
        for (MethodTree beforeMethod : beforeMethods) {
          for (VariableTree beforeParam : beforeMethod.getParameters()) {
            if (beforeParam.getName().contentEquals(afterParam.getName())) {
              if (!state.getTypes().isSubtype(ASTHelpers.getSymbol(beforeParam).type, afterParamType)) {
                return false;
              }
              break;
            }
          }
        }
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
