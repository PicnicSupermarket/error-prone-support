package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import org.assertj.core.api.AbstractAssert;

/**
 * A {@link BugChecker} which flags {@code asserThat(someValue).isEqualTo(null)} and suggests {@link
 * AbstractAssert#isNull()}.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "AssertThatIsNull",
    summary = "asserThat(...).isEqualTo(null) should be assertThat(...).isNull()",
    linkType = NONE,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class AssertThatIsNullCheck extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    MethodSymbol symbol = ASTHelpers.getSymbol(tree);
    if (symbol == null
        || tree.getArguments().size() != 1
        || tree.getArguments().get(0).getKind() != Tree.Kind.NULL_LITERAL) {
      return Description.NO_MATCH;
    }

    return describeMatch(tree, SuggestedFixes.renameMethodInvocation(tree, "isNull()", state));
  }
}
