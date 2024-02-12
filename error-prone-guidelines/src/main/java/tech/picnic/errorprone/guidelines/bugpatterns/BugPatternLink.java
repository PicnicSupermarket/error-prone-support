package tech.picnic.errorprone.guidelines.bugpatterns;

import static com.google.common.base.Verify.verify;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.FieldMatchers.staticField;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.isType;
import static com.google.errorprone.matchers.Matchers.packageStartsWith;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.AnnotationMatcherUtils;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.util.Constants;
import javax.lang.model.element.Name;

/**
 * A {@link BugChecker} that flags {@link BugChecker} declarations inside {@code
 * tech.picnic.errorprone.*} packages that do not reference the Error Prone Support website.
 */
// XXX: Introduce a similar check to enforce the Refaster `@OnlineDocumentation` annotation. (Or
// update the website generation to document Refaster collections by default, and provide an
// exclusion annotation instead. This may make more sense.)
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Error Prone Support checks must reference their online documentation",
    link = BUG_PATTERNS_BASE_URL + "BugPatternLink",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = LIKELY_ERROR)
public final class BugPatternLink extends BugChecker implements ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ClassTree> IS_ERROR_PRONE_SUPPORT_CLASS =
      packageStartsWith("tech.picnic.errorprone");
  private static final Matcher<ExpressionTree> IS_LINK_TYPE_NONE =
      staticField(BugPattern.LinkType.class.getCanonicalName(), "NONE");
  private static final Matcher<ExpressionTree> IS_BUG_PATTERNS_BASE_URL =
      staticField("tech.picnic.errorprone.utils.Documentation", "BUG_PATTERNS_BASE_URL");
  private static final MultiMatcher<ClassTree, AnnotationTree> HAS_BUG_PATTERN_ANNOTATION =
      annotations(AT_LEAST_ONE, isType(BugPattern.class.getCanonicalName()));

  /** Instantiates a new {@link BugPatternLink} instance. */
  public BugPatternLink() {}

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    if (ASTHelpers.findEnclosingNode(state.getPath(), ClassTree.class) != null) {
      /*
       * This is a nested class; even if it's bug checker, then it's likely declared within a test
       * class.
       */
      return Description.NO_MATCH;
    }

    if (!IS_ERROR_PRONE_SUPPORT_CLASS.matches(tree, state)) {
      /*
       * Bug checkers defined elsewhere are unlikely to be documented on the Error Prone Support
       * website.
       */
      return Description.NO_MATCH;
    }

    ImmutableList<AnnotationTree> bugPatternAnnotations =
        HAS_BUG_PATTERN_ANNOTATION.multiMatchResult(tree, state).matchingNodes();
    if (bugPatternAnnotations.isEmpty()) {
      /* This isn't a bug checker. */
      return Description.NO_MATCH;
    }

    AnnotationTree annotation = Iterables.getOnlyElement(bugPatternAnnotations);
    if (isCompliant(annotation, tree.getSimpleName(), state)) {
      /* The bug checker is correctly configured. */
      return Description.NO_MATCH;
    }

    return describeMatch(annotation, suggestFix(tree, state, annotation));
  }

  private static boolean isCompliant(
      AnnotationTree annotation, Name className, VisitorState state) {
    ExpressionTree linkType = AnnotationMatcherUtils.getArgument(annotation, "linkType");
    if (IS_LINK_TYPE_NONE.matches(linkType, state)) {
      /* This bug checker explicitly declares that there is no link. */
      return true;
    }

    ExpressionTree link = AnnotationMatcherUtils.getArgument(annotation, "link");
    if (!(link instanceof BinaryTree binary)) {
      return false;
    }

    verify(binary.getKind() == Kind.PLUS, "Unexpected binary operator");
    return IS_BUG_PATTERNS_BASE_URL.matches(binary.getLeftOperand(), state)
        && className.contentEquals(ASTHelpers.constValue(binary.getRightOperand(), String.class));
  }

  private static SuggestedFix suggestFix(
      ClassTree tree, VisitorState state, AnnotationTree annotation) {
    SuggestedFix.Builder fix = SuggestedFix.builder();

    String linkPrefix =
        SuggestedFixes.qualifyStaticImport(
            "tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL", fix, state);
    fix.merge(
        SuggestedFixes.updateAnnotationArgumentValues(
            annotation,
            state,
            "link",
            ImmutableList.of(
                linkPrefix + " + " + Constants.format(tree.getSimpleName().toString()))));

    String linkType =
        SuggestedFixes.qualifyStaticImport(
            BugPattern.LinkType.class.getCanonicalName() + ".CUSTOM", fix, state);
    fix.merge(
        SuggestedFixes.updateAnnotationArgumentValues(
            annotation, state, "linkType", ImmutableList.of(linkType)));

    return fix.build();
  }
}
