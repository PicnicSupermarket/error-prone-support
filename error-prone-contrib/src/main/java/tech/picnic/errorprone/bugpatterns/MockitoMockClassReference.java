package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.argument;
import static com.google.errorprone.matchers.Matchers.argumentCount;
import static com.google.errorprone.matchers.Matchers.isSameType;
import static com.google.errorprone.matchers.Matchers.isVariable;
import static com.google.errorprone.matchers.Matchers.not;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;

/**
 * A {@link BugChecker} that flags the use of {@link org.mockito.Mockito#mock(Class)} and {@link
 * org.mockito.Mockito#spy(Class)} where instead the type could be derived from the variable or
 * field with an explicit type.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Don't unnecessarily pass the type reference to Mockito's `mock(Class)` and spy(Class)`",
    link = BUG_PATTERNS_BASE_URL + "MockitoMockClassReference",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class MockitoMockClassReference extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<MethodInvocationTree> MOCKITO_MOCK_OR_SPY =
      allOf(
          argumentCount(1),
          argument(0, allOf(isSameType(Class.class.getName()), not(isVariable()))),
          staticMethod().onClass("org.mockito.Mockito").namedAnyOf("mock", "spy"));

  /** Instantiates a new {@link MockitoMockClassReference} instance. */
  public MockitoMockClassReference() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!MOCKITO_MOCK_OR_SPY.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    return isReturnTypeDerivableFromContext(state)
        ? describeMatch(tree, SuggestedFix.delete(Iterables.getOnlyElement(tree.getArguments())))
        : Description.NO_MATCH;
  }

  private static boolean isReturnTypeDerivableFromContext(VisitorState state) {
    Tree parent = state.getPath().getParentPath().getLeaf();
    switch (parent.getKind()) {
      case VARIABLE:
        return !ASTHelpers.hasNoExplicitType((VariableTree) parent, state)
            && areSameType(parent, ((VariableTree) parent).getInitializer(), state);
      case ASSIGNMENT:
        return areSameType(parent, ((AssignmentTree) parent).getExpression(), state);
      case RETURN:
        Tree context = state.findEnclosing(LambdaExpressionTree.class, MethodTree.class);
        return context instanceof MethodTree
            && areSameType(
                ((MethodTree) context).getReturnType(),
                ((ReturnTree) parent).getExpression(),
                state);
      default:
        return false;
    }
  }

  private static boolean areSameType(Tree treeA, Tree treeB, VisitorState state) {
    return ASTHelpers.isSameType(ASTHelpers.getType(treeA), ASTHelpers.getType(treeB), state);
  }
}
