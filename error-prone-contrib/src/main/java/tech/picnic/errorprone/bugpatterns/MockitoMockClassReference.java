package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.argument;
import static com.google.errorprone.matchers.Matchers.isSameType;
import static com.google.errorprone.matchers.Matchers.isVariable;
import static com.google.errorprone.matchers.Matchers.not;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.util.List;
import tech.picnic.errorprone.utils.MoreASTHelpers;

/**
 * A {@link BugChecker} that flags the use of {@link org.mockito.Mockito#mock(Class)} and {@link
 * org.mockito.Mockito#spy(Class)} where instead the type to be mocked or spied can be derived from
 * context.
 */
// XXX: This check currently does not flag method invocation arguments. When adding support for
// this, consider that in some cases the type to be mocked or spied must be specified explicitly so
// as to disambiguate between method overloads.
// XXX: This check currently does not flag (implicit or explicit) lambda return expressions.
// XXX: This check currently does not drop suppressions that become obsolete after the
// suggested fix is applied; consider adding support for this.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Don't unnecessarily pass a type to Mockito's `mock(Class)` and `spy(Class)` methods",
    link = BUG_PATTERNS_BASE_URL + "MockitoMockClassReference",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class MockitoMockClassReference extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<MethodInvocationTree> MOCKITO_MOCK_OR_SPY_WITH_HARDCODED_TYPE =
      allOf(
          argument(0, allOf(isSameType(Class.class.getCanonicalName()), not(isVariable()))),
          staticMethod().onClass("org.mockito.Mockito").namedAnyOf("mock", "spy"));

  /** Instantiates a new {@link MockitoMockClassReference} instance. */
  public MockitoMockClassReference() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!MOCKITO_MOCK_OR_SPY_WITH_HARDCODED_TYPE.matches(tree, state)
        || !isTypeDerivableFromContext(tree, state)) {
      return Description.NO_MATCH;
    }

    List<? extends ExpressionTree> arguments = tree.getArguments();
    return describeMatch(tree, SuggestedFixes.removeElement(arguments.get(0), arguments, state));
  }

  // XXX: Use switch pattern matching once the targeted JDK supports this.
  private static boolean isTypeDerivableFromContext(MethodInvocationTree tree, VisitorState state) {
    Tree parent = state.getPath().getParentPath().getLeaf();
    return switch (parent.getKind()) {
      case VARIABLE -> !ASTHelpers.hasImplicitType((VariableTree) parent, state)
          && MoreASTHelpers.areSameType(tree, parent, state);
      case ASSIGNMENT -> MoreASTHelpers.areSameType(tree, parent, state);
      case RETURN -> MoreASTHelpers.findMethodExitedOnReturn(state)
          .filter(m -> MoreASTHelpers.areSameType(tree, m.getReturnType(), state))
          .isPresent();
      default -> false;
    };
  }
}
