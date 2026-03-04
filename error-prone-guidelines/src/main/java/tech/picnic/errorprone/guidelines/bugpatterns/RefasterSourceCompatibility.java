package tech.picnic.errorprone.guidelines.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.isType;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
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
import com.sun.tools.javac.code.Type;
import javax.lang.model.type.TypeKind;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that validates the consistency between the presence of a {@link
 * tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility} annotation and the
 * actual source compatibility of a Refaster rule.
 *
 * <p>A Refaster rule is source-incompatible if its {@link AfterTemplate} return type is not a
 * subtype of every {@link BeforeTemplate} return type, meaning the replacement may break
 * compilation at call sites that depend on the narrower type. Such rules should be annotated with
 * {@link tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Refaster rules with source-incompatible type changes should be annotated with "
            + "`@PossibleSourceIncompatibility` and vice versa",
    link = BUG_PATTERNS_BASE_URL + "RefasterSourceCompatibility",
    linkType = CUSTOM,
    severity = WARNING,
    tags = LIKELY_ERROR)
public final class RefasterSourceCompatibility extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String POSSIBLE_SOURCE_INCOMPATIBILITY_ANNOTATION =
      "tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility";
  private static final Matcher<Tree> BEFORE_TEMPLATE_METHOD = hasAnnotation(BeforeTemplate.class);
  private static final Matcher<Tree> AFTER_TEMPLATE_METHOD = hasAnnotation(AfterTemplate.class);
  private static final MultiMatcher<Tree, AnnotationTree> HAS_POSSIBLE_SOURCE_INCOMPATIBILITY =
      annotations(AT_LEAST_ONE, isType(POSSIBLE_SOURCE_INCOMPATIBILITY_ANNOTATION));

  /** Instantiates a new {@link RefasterSourceCompatibility} instance. */
  public RefasterSourceCompatibility() {}

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    if (!hasMatchingMember(tree, BEFORE_TEMPLATE_METHOD, state)) {
      return Description.NO_MATCH;
    }

    boolean isIncompatible = isSourceIncompatible(tree, state);
    MultiMatchResult<AnnotationTree> annotationMatch =
        HAS_POSSIBLE_SOURCE_INCOMPATIBILITY.multiMatchResult(tree, state);
    boolean hasAnnotation = annotationMatch.matches();

    if (isIncompatible && !hasAnnotation) {
      return describeMatch(
          tree,
          SuggestedFix.builder()
              .prefixWith(tree, "@PossibleSourceIncompatibility ")
              .addImport(POSSIBLE_SOURCE_INCOMPATIBILITY_ANNOTATION)
              .build());
    }

    if (!isIncompatible && hasAnnotation) {
      return describeMatch(
          tree, SourceCode.deleteWithTrailingWhitespace(annotationMatch.onlyMatchingNode(), state));
    }

    return Description.NO_MATCH;
  }

  private static boolean isSourceIncompatible(ClassTree tree, VisitorState state) {
    Type afterReturnType = getAfterTemplateReturnType(tree, state);
    if (afterReturnType == null || afterReturnType.getKind() == TypeKind.VOID) {
      return false;
    }

    for (Tree member : tree.getMembers()) {
      if (BEFORE_TEMPLATE_METHOD.matches(member, state)
          && member instanceof MethodTree methodTree) {
        Type beforeReturnType = ASTHelpers.getSymbol(methodTree).getReturnType();
        if (beforeReturnType.getKind() != TypeKind.VOID
            && !state.getTypes().isSubtype(afterReturnType, beforeReturnType)) {
          return true;
        }
      }
    }

    return false;
  }

  private static @Nullable Type getAfterTemplateReturnType(ClassTree tree, VisitorState state) {
    for (Tree member : tree.getMembers()) {
      if (AFTER_TEMPLATE_METHOD.matches(member, state) && member instanceof MethodTree methodTree) {
        return ASTHelpers.getSymbol(methodTree).getReturnType();
      }
    }
    return null;
  }

  private static boolean hasMatchingMember(
      ClassTree tree, Matcher<Tree> matcher, VisitorState state) {
    return tree.getMembers().stream().anyMatch(member -> matcher.matches(member, state));
  }
}
