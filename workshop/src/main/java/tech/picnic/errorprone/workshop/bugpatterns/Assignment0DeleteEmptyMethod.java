package tech.picnic.errorprone.workshop.bugpatterns;

import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodTree;

/** A {@link BugChecker} that flags empty methods that seemingly can simply be deleted. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Empty method can likely be deleted",
    severity = WARNING,
    tags = SIMPLIFICATION)
public final class Assignment0DeleteEmptyMethod extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link Assignment0DeleteEmptyMethod} instance. */
  public Assignment0DeleteEmptyMethod() {}

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (tree.getBody() == null
        || !tree.getBody().getStatements().isEmpty()
        || ASTHelpers.hasAnnotation(tree, Override.class.getName(), state)) {
      return Description.NO_MATCH;
    }

    return describeMatch(tree, SuggestedFix.delete(tree));
  }
}
