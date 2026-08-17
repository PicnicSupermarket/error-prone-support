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
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.tree.SwitchTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import java.util.List;
import java.util.Objects;
import tech.picnic.errorprone.utils.MoreASTHelpers;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags {@link Enum#valueOf} invocations that contain unvalidated
 * arguments.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Avoid passing unvalidated arguments to `Enum#valueOf`",
    link = BUG_PATTERNS_BASE_URL + "UnvalidatedEnumValueOfInvocation",
    linkType = CUSTOM,
    severity = WARNING,
    tags = FRAGILE_CODE)
public final class UnvalidatedEnumValueOfInvocation extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> ENUM_INSTANCE_VALUE_OF_NAME_ONLY =
      staticMethod()
          .onDescendantOf(Enum.class.getCanonicalName())
          .named("valueOf")
          .withParameters(String.class.getCanonicalName());
  private static final Matcher<ExpressionTree> ENUM_INSTANCE_VALUE_OF_CLASS_AND_NAME =
      staticMethod()
          .onDescendantOf(Enum.class.getCanonicalName())
          .named("valueOf")
          .withParameters(Class.class.getCanonicalName(), String.class.getCanonicalName());
  private static final Matcher<ExpressionTree> ABSTRACT_ENUM_VALUE_OF =
      staticMethod()
          .onClass(Enum.class.getCanonicalName())
          .named("valueOf")
          .withParameters(Class.class.getCanonicalName(), String.class.getCanonicalName());
  private static final Matcher<ExpressionTree> ENUM_NAME_OR_TO_STRING_METHOD =
      instanceMethod()
          .onDescendantOf(Enum.class.getCanonicalName())
          .namedAnyOf("name", "toString")
          .withNoParameters();

  /** Instantiates a new {@link UnvalidatedEnumValueOfInvocation} instance. */
  public UnvalidatedEnumValueOfInvocation() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!(captureEnumType(tree, state) instanceof Captured(Type enumType))) {
      return Description.NO_MATCH;
    }

    ExpressionTree nameArgument = tree.getArguments().getLast();
    if (!MoreASTHelpers.isStringTyped(nameArgument, state)) {
      return buildDescription(tree)
          .setMessage(
              "`%s` is not a valid type for `%s`, expected: `String`"
                  .formatted(ASTHelpers.getType(nameArgument), enumType))
          .build();
    }

    String value = ASTHelpers.constValue(nameArgument, String.class);
    ImmutableSet<String> valuesSourceEnum = getEnumValues(enumType);

    if (value != null && !valuesSourceEnum.contains(value)) {
      return buildDescription(tree)
          .setMessage(
              "`%s` is not a valid value for `%s`, possible values: %s"
                  .formatted(value, enumType, valuesSourceEnum))
          .build();
    }

    if (ENUM_NAME_OR_TO_STRING_METHOD.matches(nameArgument, state)) {
      ExpressionTree invocationReceiverTree = ASTHelpers.getReceiver(nameArgument);
      Symbol enumSymbolPassedToValueOf = ASTHelpers.getSymbol(invocationReceiverTree);
      if (enumSymbolPassedToValueOf == null) {
        return Description.NO_MATCH;
      }

      ImmutableSet<String> enumValuesOfNameInvocationReceiver =
          invocationReceiverTree instanceof MemberSelectTree memberSelectTree
              ? ImmutableSet.of(memberSelectTree.getIdentifier().toString())
              : getEnumValues(ASTHelpers.getReceiverType(nameArgument));

      ImmutableSet<String> missingValues =
          Sets.difference(
                  findSwitchCoveredValues(
                      enumSymbolPassedToValueOf, enumValuesOfNameInvocationReceiver, state),
                  valuesSourceEnum)
              .immutableCopy();
      return missingValues.isEmpty()
          ? Description.NO_MATCH
          : buildDescription(tree)
              .setMessage(
                  "`%s` might generate values which are missing in `%s`: %s"
                      .formatted(
                          SourceCode.treeToString(nameArgument, state), enumType, missingValues))
              .build();
    }

    // Match identifiers.
    return nameArgument instanceof IdentifierTree ? describeMatch(tree) : Description.NO_MATCH;
  }

  private static CaptureResult captureEnumType(MethodInvocationTree tree, VisitorState state) {
    if (ENUM_INSTANCE_VALUE_OF_NAME_ONLY.matches(tree, state)) {
      return new Captured(ASTHelpers.getReceiverType(tree));
    }
    if (ENUM_INSTANCE_VALUE_OF_CLASS_AND_NAME.matches(tree, state)
        || ABSTRACT_ENUM_VALUE_OF.matches(tree, state)) {
      Type classArgType = ASTHelpers.getType(tree.getArguments().getFirst());
      if (classArgType == null || classArgType.getTypeArguments().isEmpty()) {
        return new NoMatch();
      }
      return new Captured(classArgType.getTypeArguments().getFirst());
    }
    return new NoMatch();
  }

  private static ImmutableSet<String> getEnumValues(Type type) {
    return ImmutableSet.copyOf(ASTHelpers.enumValues(type.asElement()));
  }

  /**
   * Finds the enum values covered by the switch cases containing {@code enumValueArgument}.
   *
   * <p>For a non-default case, returns the labels of that case. For a default case, returns the
   * enum values not covered by other cases. Returns all values if {@code enumValueArgument} is not
   * part of a switch statement or expression, or if the switch expression symbol doesn't match the
   * symbol of {@code enumValueArgument}.
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
  // XXX: Fall-through in colon-style switch statements is not analyzed; only the enclosing case's
  // labels are considered.
  private static ImmutableSet<String> findSwitchCoveredValues(
      Symbol enumSymbolPassedToValueOf, ImmutableSet<String> valuesOfReceiver, VisitorState state) {
    CaseTree enclosingCaseTree = ASTHelpers.findEnclosingNode(state.getPath(), CaseTree.class);
    if (enclosingCaseTree == null) {
      /* Fast path: avoids unnecessary switch-tree lookups when not inside a case. */
      return valuesOfReceiver;
    }

    Symbol switchExpressionSymbol;
    List<? extends CaseTree> switchCases;
    SwitchExpressionTree switchExprTree =
        ASTHelpers.findEnclosingNode(state.getPath(), SwitchExpressionTree.class);
    if (switchExprTree != null) {
      switchExpressionSymbol =
          ASTHelpers.getSymbol(
              ((ParenthesizedTree) switchExprTree.getExpression()).getExpression());
      switchCases = switchExprTree.getCases();
    } else {
      SwitchTree switchStmtTree = ASTHelpers.findEnclosingNode(state.getPath(), SwitchTree.class);
      if (switchStmtTree == null) {
        /* Defensive: a `CaseTree` is structurally always inside a switch. */
        return valuesOfReceiver;
      }
      switchExpressionSymbol =
          ASTHelpers.getSymbol(
              ((ParenthesizedTree) switchStmtTree.getExpression()).getExpression());
      switchCases = switchStmtTree.getCases();
    }

    if (!Objects.equals(switchExpressionSymbol, enumSymbolPassedToValueOf)) {
      return valuesOfReceiver;
    }

    if (ASTHelpers.isSwitchDefault(enclosingCaseTree)) {
      ImmutableSet<String> coveredCases =
          switchCases.stream()
              .flatMap(caseTree -> caseTree.getLabels().stream())
              .map(label -> SourceCode.treeToString(label, state))
              .collect(toImmutableSet());
      return Sets.difference(valuesOfReceiver, coveredCases).immutableCopy();
    }

    return enclosingCaseTree.getLabels().stream()
        .map(label -> SourceCode.treeToString(label, state))
        .collect(toImmutableSet());
  }

  private sealed interface CaptureResult {}

  private record Captured(Type capturedType) implements CaptureResult {}

  private record NoMatch() implements CaptureResult {}
}
