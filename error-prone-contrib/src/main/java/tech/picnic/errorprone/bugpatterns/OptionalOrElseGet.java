package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.PERFORMANCE;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static java.util.stream.Collectors.joining;

import com.google.auto.service.AutoService;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.refaster.Refaster;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import java.util.Optional;
import java.util.function.Supplier;
import tech.picnic.errorprone.refaster.matchers.RequiresComputation;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags arguments to {@link Optional#orElse(Object)} that should be
 * deferred using {@link Optional#orElseGet(Supplier)}.
 *
 * <p>The suggested fix assumes that the argument to {@code orElse} does not have side effects. If
 * it does, the suggested fix changes the program's semantics. Such fragile code must instead be
 * refactored such that the side-effectful code does not appear accidental.
 */
// XXX: This rule may introduce a compilation error: the `value` expression may reference a
// non-effectively final variable, which is not allowed in the replacement lambda expression.
// Review whether a `@Matcher` can be used to avoid this.
// XXX: Once the `MethodReferenceUsageCheck` bug checker becomes generally usable, consider leaving
// the method reference cleanup to that check, and express the remainder of the logic in this class
// using a Refaster template, i.c.w. a `@NotMatches(RequiresComputation.class)` constraint.
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        """
        Prefer `Optional#orElseGet` over `Optional#orElse` if the fallback requires additional \
        computation""",
    linkType = NONE,
    severity = WARNING,
    tags = PERFORMANCE)
public final class OptionalOrElseGet extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> REQUIRES_COMPUTATION = new RequiresComputation();
  private static final Matcher<ExpressionTree> OPTIONAL_OR_ELSE_METHOD =
      instanceMethod().onExactClass(Optional.class.getCanonicalName()).namedAnyOf("orElse");
  // XXX: Also exclude invocations of `@Placeholder`-annotated methods.
  private static final Matcher<ExpressionTree> REFASTER_METHOD =
      staticMethod().onClass(Refaster.class.getCanonicalName());

  /** Instantiates a new {@link OptionalOrElseGet} instance. */
  public OptionalOrElseGet() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!OPTIONAL_OR_ELSE_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    ExpressionTree argument = Iterables.getOnlyElement(tree.getArguments());
    if (!REQUIRES_COMPUTATION.matches(argument, state)
        || REFASTER_METHOD.matches(argument, state)) {
      return Description.NO_MATCH;
    }

    /*
     * We have a match. Construct the method reference or lambda expression to be passed to the
     * replacement `#orElseGet` invocation.
     */
    String newArgument =
        tryMethodReferenceConversion(argument, state)
            .orElseGet(() -> "() -> " + SourceCode.treeToString(argument, state));

    /* Construct the suggested fix, replacing the method invocation and its argument. */
    SuggestedFix fix =
        SuggestedFixes.renameMethodInvocation(tree, "orElseGet", state).toBuilder()
            .replace(argument, newArgument)
            .build();

    return describeMatch(tree, fix);
  }

  /** Returns the nullary method reference matching the given expression, if any. */
  private static Optional<String> tryMethodReferenceConversion(
      ExpressionTree tree, VisitorState state) {
    if (!(tree instanceof MethodInvocationTree methodInvocation)) {
      return Optional.empty();
    }

    if (!methodInvocation.getArguments().isEmpty()) {
      return Optional.empty();
    }

    if (!(methodInvocation.getMethodSelect() instanceof MemberSelectTree memberSelect)) {
      return Optional.empty();
    }

    if (REQUIRES_COMPUTATION.matches(memberSelect.getExpression(), state)) {
      return Optional.empty();
    }

    return Optional.of(
        SourceCode.treeToString(memberSelect.getExpression(), state)
            + "::"
            + (methodInvocation.getTypeArguments().isEmpty()
                ? ""
                : methodInvocation.getTypeArguments().stream()
                    .map(arg -> SourceCode.treeToString(arg, state))
                    .collect(joining(",", "<", ">")))
            + memberSelect.getIdentifier());
  }
}
