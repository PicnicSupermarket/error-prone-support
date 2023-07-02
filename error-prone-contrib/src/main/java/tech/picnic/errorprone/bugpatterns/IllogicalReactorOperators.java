package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.Position;

/**
 * Each Reactor operator has some set of properties, and some of those properties are incompatible
 * when combined. This {@link BugChecker} is used to flag such cases.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "The reactor operators used are incompatible with each other and can indicate a hidden bug.",
    link = BUG_PATTERNS_BASE_URL + "IllogicalReactorOperators",
    linkType = CUSTOM,
    severity = WARNING,
    tags = LIKELY_ERROR)
public final class IllogicalReactorOperators extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Supplier<Type> PUBLISHER =
      Suppliers.typeFromString("org.reactivestreams.Publisher");

  private static final ImmutableSet<String> INCOMPATIBLE_CHILD_OPERATORS =
      ImmutableSet.of("filter", "map", "flatMap", "concatMap");

  private static final Matcher<ExpressionTree> PUBLISHER_INCOMPATIBLE_OPS =
      instanceMethod().onDescendantOf(PUBLISHER).namedAnyOf("then", "thenEmpty", "thenMany");

  /** Instantiates a new {@link IllogicalReactorOperators} instance. */
  public IllogicalReactorOperators() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!(PUBLISHER_INCOMPATIBLE_OPS.matches(tree, state)
        && INCOMPATIBLE_CHILD_OPERATORS.stream()
            .anyMatch(
                ASTHelpers.getSymbol(ASTHelpers.getReceiver(tree)).getSimpleName().toString()
                    ::equals))) {
      return Description.NO_MATCH;
    }

    Description.Builder description = buildDescription(tree);

    ImmutableList<ExpressionTree> collect =
        ASTHelpers.streamReceivers(tree).collect(toImmutableList());

    SuggestedFix fix = dropNoOpOperator(collect.get(0), state).build();

    description.addFix(fix);

    return description.build();
  }

  private static SuggestedFix.Builder dropNoOpOperator(
      ExpressionTree expressionTree, VisitorState state) {
    int startPosition = state.getEndPosition(ASTHelpers.getReceiver(expressionTree));
    int endPosition = state.getEndPosition(expressionTree);

    checkState(
        startPosition != Position.NOPOS && endPosition != Position.NOPOS,
        "Cannot locate method to be replaced in source code");

    return SuggestedFix.builder().replace(startPosition, endPosition, "");
  }
}
