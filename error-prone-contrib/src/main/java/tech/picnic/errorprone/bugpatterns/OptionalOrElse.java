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
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import java.util.Optional;
import java.util.function.Supplier;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags arguments to {@link Optional#orElse(Object)} that should be
 * deferred using {@link Optional#orElseGet(Supplier)}.
 *
 * <p>The suggested fix assumes that the argument to {@code orElse} does not have side effects. If
 * it does, the suggested fix changes the program's semantics. Such fragile code must instead be
 * refactored such that the side-effectful code does not appear accidental.
 */
// XXX: Consider also implementing the inverse, in which `.orElseGet(() -> someConstant)` is
// flagged.
// XXX: Once the `MethodReferenceUsageCheck` becomes generally usable, consider leaving the method
// reference cleanup to that check, and express the remainder of the logic in this class using a
// Refaster template, i.c.w. a `@Matches` constraint that implements the `requiresComputation`
// logic.
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "Prefer `Optional#orElseGet` over `Optional#orElse` if the fallback requires additional computation",
    linkType = NONE,
    severity = WARNING,
    tags = PERFORMANCE)
public final class OptionalOrElse extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> OPTIONAL_OR_ELSE_METHOD =
      instanceMethod().onExactClass(Optional.class.getCanonicalName()).namedAnyOf("orElse");
  // XXX: Also exclude invocations of `@Placeholder`-annotated methods.
  private static final Matcher<ExpressionTree> REFASTER_METHOD =
      staticMethod().onClass(Refaster.class.getCanonicalName());

  /** Instantiates a new {@link OptionalOrElse} instance. */
  public OptionalOrElse() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!OPTIONAL_OR_ELSE_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    ExpressionTree argument = Iterables.getOnlyElement(tree.getArguments());
    if (!requiresComputation(argument) || REFASTER_METHOD.matches(argument, state)) {
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
        SuggestedFix.builder()
            .merge(SuggestedFixes.renameMethodInvocation(tree, "orElseGet", state))
            .replace(argument, newArgument)
            .build();

    return describeMatch(tree, fix);
  }

  /**
   * Tells whether the given expression contains anything other than a literal or a (possibly
   * dereferenced) variable or constant.
   */
  private static boolean requiresComputation(ExpressionTree tree) {
    return !(tree instanceof IdentifierTree
        || tree instanceof LiteralTree
        || (tree instanceof MemberSelectTree
            && !requiresComputation(((MemberSelectTree) tree).getExpression()))
        || ASTHelpers.constValue(tree) != null);
  }

  /** Returns the nullary method reference matching the given expression, if any. */
  private static Optional<String> tryMethodReferenceConversion(
      ExpressionTree tree, VisitorState state) {
    if (!(tree instanceof MethodInvocationTree)) {
      return Optional.empty();
    }

    MethodInvocationTree invocation = (MethodInvocationTree) tree;
    if (!invocation.getArguments().isEmpty()) {
      return Optional.empty();
    }

    if (!(invocation.getMethodSelect() instanceof MemberSelectTree)) {
      return Optional.empty();
    }

    MemberSelectTree method = (MemberSelectTree) invocation.getMethodSelect();
    if (requiresComputation(method.getExpression())) {
      return Optional.empty();
    }

    return Optional.of(
        SourceCode.treeToString(method.getExpression(), state)
            + "::"
            + (invocation.getTypeArguments().isEmpty()
                ? ""
                : invocation.getTypeArguments().stream()
                    .map(arg -> SourceCode.treeToString(arg, state))
                    .collect(joining(",", "<", ">")))
            + method.getIdentifier());
  }
}
