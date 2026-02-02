package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.FRAGILE_CODE;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static java.util.Objects.requireNonNull;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.CaseLabelTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.tools.javac.code.Type;
import java.util.Optional;
import tech.picnic.errorprone.utils.MoreASTHelpers;

/**
 * A {@link BugChecker} that flags {@link Enum#valueOf} invocations that contain unchecked
 * arguments.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Avoid passing unchecked arguments to `Enum#valueOf`",
    link = BUG_PATTERNS_BASE_URL + "UncheckedEnumValueOfInvocation",
    linkType = CUSTOM,
    severity = WARNING,
    tags = FRAGILE_CODE)
public final class UncheckedEnumValueOfInvocation extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> ENUM_VALUE_OF =
      staticMethod().onDescendantOf(Enum.class.getCanonicalName()).named("valueOf");
  private static final Matcher<ExpressionTree> STRING_VALUE_ENUM =
      instanceMethod().onDescendantOf(Enum.class.getCanonicalName()).namedAnyOf("name", "toString");

  /** Instantiates a new {@link UncheckedEnumValueOfInvocation} instance. */
  public UncheckedEnumValueOfInvocation() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!ENUM_VALUE_OF.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    Type enumType = ASTHelpers.getReceiverType(tree);
    Optional<ExpressionTree> optionalNameArgument = extractNameArgument(tree, state);

    if (optionalNameArgument.isEmpty()) {
      return buildDescription(tree)
          .setMessage(
              "No `String` typed `name` argument was found on `%s`"
                  .formatted(state.getSourceForNode(tree)))
          .build();
    }

    ExpressionTree nameArgument = optionalNameArgument.orElseThrow();
    String value = ASTHelpers.constValue(nameArgument, String.class);
    ImmutableSet<String> valuesSourceEnum = findEnumValuesOfReceiver(enumType);
    if (value != null && !valuesSourceEnum.contains(value)) {
      return buildDescription(tree)
          .setMessage(
              "`%s` is not a valid value for `%s`, possible values: %s"
                  .formatted(value, enumType, valuesSourceEnum))
          .build();
    }

    // Match name argument where it is an invocation of another enum's `.name()` or `.toString()`.
    if (STRING_VALUE_ENUM.matches(nameArgument, state)) {
      // Check if it is a part of switch-case statement, and values are filtered by labels.
      ImmutableSet<String> valuesTargetEnum =
          findEnumValuesOfReceiver(ASTHelpers.getReceiverType(nameArgument));
      ImmutableSet<String> switchCoveredValues =
          findSwitchCoveredValues(nameArgument, valuesTargetEnum, state);
      ImmutableSet<String> missingValues =
          Sets.difference(switchCoveredValues, valuesSourceEnum).immutableCopy();
      return missingValues.isEmpty()
          ? Description.NO_MATCH
          : buildDescription(tree)
              .setMessage(
                  "`%s` might generate values which are missing in `%s`: %s"
                      .formatted(state.getSourceForNode(nameArgument), enumType, missingValues))
              .build();
    }

    /* Matches unchecked identifiers. */
    return nameArgument instanceof IdentifierTree ? describeMatch(tree) : Description.NO_MATCH;
  }

  /** Extracts {@code name} argument from {@link Enum#valueOf} invocations. */
  private static Optional<ExpressionTree> extractNameArgument(
      MethodInvocationTree tree, VisitorState state) {
    return tree.getArguments().stream()
        .filter(argument -> MoreASTHelpers.isStringTyped(argument, state))
        .map(ExpressionTree.class::cast)
        .findAny();
  }

  private static ImmutableSet<String> findEnumValuesOfReceiver(Type type) {
    return ImmutableSet.copyOf(ASTHelpers.enumValues(type.tsym));
  }

  /**
   * Finds the enum values covered by the switch cases containing {@code enumValueArgument}.
   *
   * <p>For a non-default case, returns the labels of that case. For a default case, returns the
   * enum values not covered by other cases. Returns all values if {@code enumValueArgument} is not
   * part of a switch statement, or if the switch expression type doesn't match the type of {@code
   * enumValueArgument}.
   *
   * <p>Example 1 - non-default case returns {@code ["B1", "B2"]}:
   *
   * <pre>{@code
   * enum B {
   *     B1, B2, B3
   * }
   *
   * switch(b) {
   *     case B1, B2 -> A.valueOf(b.name());
   * }
   * }</pre>
   *
   * <p>Example 2 - default case returns {@code ["B3"]}:
   *
   * <pre>{@code
   * enum B {
   *     B1, B2, B3
   * }
   *
   * switch(b) {
   *     case B1, B2 -> null;
   *     default -> A.valueOf(b.name());
   * }
   * }</pre>
   */
  private static ImmutableSet<String> findSwitchCoveredValues(
      ExpressionTree enumValueArgument, ImmutableSet<String> valuesOfReceiver, VisitorState state) {
    SwitchExpressionTree switchTree =
        ASTHelpers.findEnclosingNode(state.getPath(), SwitchExpressionTree.class);
    if (switchTree == null) {
      return valuesOfReceiver;
    }

    Type switchExpressionType = ASTHelpers.getType(switchTree.getExpression());
    Type nameInvocationReceiverType = ASTHelpers.getReceiverType(enumValueArgument);
    if (!ASTHelpers.isSameType(switchExpressionType, nameInvocationReceiverType, state)) {
      return valuesOfReceiver;
    }

    CaseTree enclosingCaseTree =
        requireNonNull(
            ASTHelpers.findEnclosingNode(state.getPath(), CaseTree.class), "No `case` found");
    if (ASTHelpers.isSwitchDefault(enclosingCaseTree)) {
      ImmutableSet<String> coveredCases =
          switchTree.getCases().stream()
              .flatMap(caseTree -> caseTree.getLabels().stream())
              .map(CaseLabelTree::toString)
              .collect(toImmutableSet());

      return Sets.difference(valuesOfReceiver, coveredCases).immutableCopy();
    }

    return enclosingCaseTree.getLabels().stream()
        .map(CaseLabelTree::toString)
        .collect(toImmutableSet());
  }
}
