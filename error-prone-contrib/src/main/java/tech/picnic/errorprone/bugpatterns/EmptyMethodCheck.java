package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.MethodTree;

/** A {@link BugChecker} which flags empty methods. */
// XXX: Disable until fixed.
// @AutoService(BugChecker.class)
@BugPattern(
  name = "EmptyMethod",
  summary = "Empty method can likely be deleted",
  linkType = LinkType.NONE,
  severity = SeverityLevel.WARNING,
  tags = StandardTags.LIKELY_ERROR
)
public final class EmptyMethodCheck extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;

  // XXX: Restrict: only warn about:
  // - static methods
  // - methods that are provably not in an inheritance hierarchy.
  // - public methods in a Mockito test class.
  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (tree.getBody() == null || !tree.getBody().getStatements().isEmpty()) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree).build();
  }
}
