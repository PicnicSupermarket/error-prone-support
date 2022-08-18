package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.isType;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.util.Optional;

/** A {@link BugChecker} which flags empty methods that seemingly can simply be deleted. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Empty method can likely be deleted",
    linkType = NONE,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class EmptyMethod extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<Tree> PERMITTED_ANNOTATION =
      annotations(
          AT_LEAST_ONE,
          anyOf(isType("java.lang.Override"), isType("org.aspectj.lang.annotation.Pointcut")));

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (tree.getBody() == null
        || !tree.getBody().getStatements().isEmpty()
        || ASTHelpers.containsComments(tree, state)
        || PERMITTED_ANNOTATION.matches(tree, state)
        || isInPossibleTestHelperClass(state)) {
      return Description.NO_MATCH;
    }

    if (ASTHelpers.methodCanBeOverridden(ASTHelpers.getSymbol(tree))) {
      return Description.NO_MATCH;
    }

    return describeMatch(tree, SuggestedFix.delete(tree));
  }

  private static boolean isInPossibleTestHelperClass(VisitorState state) {
    return Optional.ofNullable(ASTHelpers.findEnclosingNode(state.getPath(), ClassTree.class))
        .map(ClassTree::getSimpleName)
        .filter(name -> name.toString().contains("Test"))
        .isPresent();
  }
}
