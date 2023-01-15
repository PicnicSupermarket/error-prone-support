package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.argument;
import static com.google.errorprone.matchers.Matchers.argumentCount;
import static com.google.errorprone.matchers.Matchers.isSameType;
import static com.google.errorprone.matchers.Matchers.isVariable;
import static com.google.errorprone.matchers.Matchers.not;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.sun.source.tree.Tree.Kind.RETURN;
import static com.sun.source.tree.Tree.Kind.VARIABLE;
import static java.util.Objects.requireNonNull;
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
  // XXX: Replace `var` usage with explicit type instead.
  private static final Matcher<VariableTree> INCOMPATIBLE_VARIABLE =
      anyOf(ASTHelpers::hasNoExplicitType, MockitoMockClassReference::hasTypeDifference);
  private static final Matcher<ReturnTree> INCOMPATIBLE_RETURN =
      MockitoMockClassReference::hasTypeDifference;

  /** Instantiates a new {@link MockitoMockClassReference} instance. */
  public MockitoMockClassReference() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!MOCKITO_MOCK_OR_SPY.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Tree parent = state.getPath().getParentPath().getLeaf();
    if (parent.getKind() == VARIABLE
        && INCOMPATIBLE_VARIABLE.matches((VariableTree) parent, state)) {
      return Description.NO_MATCH;
    } else if (parent.getKind() == RETURN
        && INCOMPATIBLE_RETURN.matches((ReturnTree) parent, state)) {
      return Description.NO_MATCH;
    }

    return describeMatch(tree, SuggestedFix.delete(Iterables.getOnlyElement(tree.getArguments())));
  }

  private static boolean hasTypeDifference(VariableTree tree, VisitorState state) {
    return hasTypeDifference(tree, tree.getInitializer(), state);
  }

  private static boolean hasTypeDifference(ReturnTree tree, VisitorState state) {
    Tree returnTypeTree = requireNonNull(state.findEnclosing(MethodTree.class)).getReturnType();
    return hasTypeDifference(returnTypeTree, tree.getExpression(), state);
  }

  private static boolean hasTypeDifference(Tree treeA, Tree treeB, VisitorState state) {
    return !ASTHelpers.isSameType(ASTHelpers.getType(treeA), ASTHelpers.getType(treeB), state);
  }
}
