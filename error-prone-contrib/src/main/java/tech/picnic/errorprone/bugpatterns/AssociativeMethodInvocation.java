package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.not;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.matchers.Matchers.toType;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} that flags unnecessarily nested usage of methods that implement an
 * associative operation.
 *
 * <p>The arguments to such methods can be flattened without affecting semantics, while making the
 * code more readable.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "These methods implement an associative operation, so you can flatten the list of operands",
    link = BUG_PATTERNS_BASE_URL + "AssociativeMethodInvocation",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class AssociativeMethodInvocation extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Supplier<Type> ITERABLE = Suppliers.typeFromClass(Iterable.class);
  private static final ImmutableList<Matcher<ExpressionTree>> ASSOCIATIVE_OPERATIONS =
      ImmutableList.of(
          allOf(
              staticMethod().onClass(Suppliers.typeFromClass(Matchers.class)).named("allOf"),
              toType(MethodInvocationTree.class, not(hasArgumentOfType(ITERABLE)))),
          allOf(
              staticMethod().onClass(Suppliers.typeFromClass(Matchers.class)).named("anyOf"),
              toType(MethodInvocationTree.class, not(hasArgumentOfType(ITERABLE)))),
          staticMethod().onClass(Suppliers.typeFromClass(Refaster.class)).named("anyOf"));

  /** Instantiates a new {@link AssociativeMethodInvocation} instance. */
  public AssociativeMethodInvocation() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    for (Matcher<ExpressionTree> matcher : ASSOCIATIVE_OPERATIONS) {
      if (matcher.matches(tree, state)) {
        SuggestedFix.Builder fix = SuggestedFix.builder();
        for (ExpressionTree arg : tree.getArguments()) {
          if (matcher.matches(arg, state)) {
            MethodInvocationTree invocation = (MethodInvocationTree) arg;
            fix.merge(
                invocation.getArguments().isEmpty()
                    ? SuggestedFixes.removeElement(arg, tree.getArguments(), state)
                    : SourceCode.unwrapMethodInvocation(invocation, state));
          }
        }

        return fix.isEmpty() ? Description.NO_MATCH : describeMatch(tree, fix.build());
      }
    }

    return Description.NO_MATCH;
  }

  private static Matcher<MethodInvocationTree> hasArgumentOfType(Supplier<Type> type) {
    return (tree, state) ->
        tree.getArguments().stream()
            .anyMatch(arg -> ASTHelpers.isSubtype(ASTHelpers.getType(arg), type.get(state), state));
  }
}
