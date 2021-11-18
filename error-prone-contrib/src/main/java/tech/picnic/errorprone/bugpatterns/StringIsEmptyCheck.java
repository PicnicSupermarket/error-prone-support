package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType;
import static com.google.errorprone.BugPattern.SeverityLevel;
import static com.google.errorprone.BugPattern.StandardTags;
import static com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static com.sun.source.tree.Tree.Kind;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;

@AutoService(BugChecker.class)
@BugPattern(
    name = "StringIsEmpty",
    summary = "Prefer `String#isEmpty` over `String#equals`",
    severity = SeverityLevel.SUGGESTION,
    tags = StandardTags.STYLE)
public final class StringIsEmptyCheck extends BugChecker implements MethodInvocationTreeMatcher {
  private static final Matcher<ExpressionTree> STRING_EQUALS_INVOCATION =
      instanceMethod().onDescendantOf("java.lang.String").named("equals");

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (STRING_EQUALS_INVOCATION.matches(tree, state)
        && tree.getArguments().get(0).getKind() == Kind.STRING_LITERAL
        && ((LiteralTree) tree.getArguments().get(0)).getValue().equals("")) {
      return describeMatch(
          tree, SuggestedFix.replace(tree, ASTHelpers.getReceiver(tree) + ".isEmpty()"));
    }
    return Description.NO_MATCH;
  }
}
