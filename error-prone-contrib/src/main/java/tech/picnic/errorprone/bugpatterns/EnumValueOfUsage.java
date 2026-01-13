package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.FRAGILE_CODE;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.staticMethod;
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
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Type;
import tech.picnic.errorprone.utils.MoreASTHelpers;

/**
 * A {@link BugChecker} that flags {@link Enum#valueOf} invocations that contain unchecked
 * arguments.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Avoid passing unchecked arguments to `valueOf`",
    link = BUG_PATTERNS_BASE_URL + "EnumValueOfUsage",
    linkType = CUSTOM,
    severity = WARNING,
    tags = FRAGILE_CODE)
public final class EnumValueOfUsage extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> ENUM_VALUE_OF =
      staticMethod().onDescendantOf(Enum.class.getCanonicalName()).named("valueOf");
  private static final Matcher<ExpressionTree> STRING_VALUE_ENUM =
      instanceMethod().onDescendantOf(Enum.class.getCanonicalName()).namedAnyOf("name", "toString");

  /** Instantiates a new {@link EnumValueOfUsage} instance. */
  public EnumValueOfUsage() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!ENUM_VALUE_OF.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    ExpressionTree nameArgument = extractNameArgument(tree, state);

    // Match unchecked String values
    if (nameArgument instanceof IdentifierTree) {
      return describeMatch(tree);
    }

    ImmutableSet<String> valuesOfSource = findEnumValuesOfReceiver(tree);

    // Match name argument where it is an invocation of another enum's .name() or .toString()
    if (STRING_VALUE_ENUM.matches(nameArgument, state)) {
      // Check if it is a part of switch-case statement, and values are filtered by labels.
      ImmutableSet<String> filteredEnumValues = findFilteredEnumValues(nameArgument, state);

      ImmutableSet<String> valuesOfTarget =
          filteredEnumValues.isEmpty()
              ? findEnumValuesOfReceiver(nameArgument)
              : filteredEnumValues;

      return Sets.difference(valuesOfTarget, valuesOfSource).isEmpty()
          ? Description.NO_MATCH
          : describeMatch(tree);
    }

    // Match constants
    String constantValue = ASTHelpers.constValue(nameArgument, String.class);
    return constantValue != null && valuesOfSource.contains(constantValue)
        ? Description.NO_MATCH
        : describeMatch(tree);
  }

  /** Extracts {@code name} argument from {@link Enum#valueOf} invocations. */
  private static ExpressionTree extractNameArgument(MethodInvocationTree tree, VisitorState state) {
    return tree.getArguments().stream()
        .filter(argument -> MoreASTHelpers.isStringTyped(argument, state))
        .findAny()
        .orElseThrow();
  }

  private static ImmutableSet<String> findEnumValuesOfReceiver(ExpressionTree tree) {
    return ImmutableSet.copyOf(ASTHelpers.enumValues(ASTHelpers.getReceiverType(tree).tsym));
  }

  /**
   * Finds and returns all filtered enum values of {@code b.name()} (or {@code b.toString()}) where
   * {@code b} is an enum type, and is enclosed by a switch statement where {@code b} is inside
   * switch parenthesis, e.g. {@code switch (b)}. Otherwise, returns empty set. {@code b.name()} is
   * provided as {@code nameArgument}.
   *
   * <p>Returns {@code ["B1", "B2"]} for:
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
   */
  private static ImmutableSet<String> findFilteredEnumValues(
      ExpressionTree nameArgument, VisitorState state) {
    TreePath treePath = TreePath.getPath(state.getPath(), nameArgument);
    SwitchExpressionTree switchExpressionTree =
        ASTHelpers.findEnclosingNode(treePath, SwitchExpressionTree.class);
    if (switchExpressionTree == null) {
      return ImmutableSet.of();
    }

    Type paranthesisExpressionType = ASTHelpers.getType(switchExpressionTree.getExpression());
    Type nameInvocationReceiverType = ASTHelpers.getType(ASTHelpers.getReceiver(nameArgument));
    if (ASTHelpers.isSameType(paranthesisExpressionType, nameInvocationReceiverType, state)) {
      CaseTree filteredCaseTree = ASTHelpers.findEnclosingNode(treePath, CaseTree.class);
      if (filteredCaseTree != null) {
        return filteredCaseTree.getLabels().stream()
            .map(Object::toString)
            .collect(toImmutableSet());
      }
    }
    return ImmutableSet.of();
  }
}
