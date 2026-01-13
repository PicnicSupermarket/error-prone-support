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
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol;
import java.util.Objects;
import java.util.Optional;

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

    ExpressionTree nameArgument = extractNameArgument(tree);

    // Match unchecked String values
    if (nameArgument instanceof IdentifierTree) {
      return describeMatch(tree);
    }

    ImmutableSet<String> valuesOfSource = findEnumValuesOfMethodInvocationTree(tree);

    // Match name argument where it is an invocation of another enum's .name() or .toString()
    if (nameArgument instanceof MethodInvocationTree anotherEnumsInvocation
        && STRING_VALUE_ENUM.matches(anotherEnumsInvocation, state)) {

      // Check if it is a part of switch-case statement, and values are filtered by labels.
      ImmutableSet<String> filteredEnumValues =
          findFilteredEnumValues(anotherEnumsInvocation, state);

      ImmutableSet<String> valuesOfTarget =
          filteredEnumValues.isEmpty()
              ? findEnumValuesOfMethodInvocationTree(anotherEnumsInvocation)
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
  private static ExpressionTree extractNameArgument(MethodInvocationTree tree) {
    return tree.getArguments().stream()
        .filter(
            argument ->
                Optional.ofNullable(ASTHelpers.getType(argument))
                    .filter(type -> type.toString().equals(String.class.getCanonicalName()))
                    .isPresent())
        .findAny()
        .orElseThrow();
  }

  private static ImmutableSet<String> findEnumValuesOfMethodInvocationTree(
      MethodInvocationTree tree) {
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
      MethodInvocationTree nameArgument, VisitorState state) {
    TreePath treePath = TreePath.getPath(state.getPath(), nameArgument);
    SwitchExpressionTree switchExpressionTree =
        ASTHelpers.findEnclosingNode(treePath, SwitchExpressionTree.class);
    if (switchExpressionTree == null) {
      return ImmutableSet.of();
    }

    if (switchExpressionTree.getExpression() instanceof ParenthesizedTree parenthesizedTree) {
      Symbol switchParenthesis = ASTHelpers.getSymbol(parenthesizedTree.getExpression());
      Symbol nameCallReceiver = ASTHelpers.getSymbol(ASTHelpers.getReceiver(nameArgument));

      if (switchParenthesis == null
          || nameCallReceiver == null
          || !Objects.equals(switchParenthesis, nameCallReceiver)) {
        return ImmutableSet.of();
      }

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
