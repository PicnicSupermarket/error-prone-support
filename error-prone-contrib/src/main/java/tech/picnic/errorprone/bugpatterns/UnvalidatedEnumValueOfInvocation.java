package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.FRAGILE_CODE;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
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
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import java.util.List;
import org.jspecify.annotations.Nullable;
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
  private static final Matcher<ExpressionTree> ENUM_NAME_OR_TO_STRING_METHOD =
      instanceMethod()
          .onDescendantOf(Enum.class.getCanonicalName())
          .namedAnyOf("name", "toString")
          .withNoParameters();

  /** Instantiates a new {@link UnvalidatedEnumValueOfInvocation} instance. */
  public UnvalidatedEnumValueOfInvocation() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    return switch (captureEnumType(tree, state)) {
      case Captured(Type capturedType) -> matchMethodInvocation(tree, state, capturedType);
      case NoMatch() -> Description.NO_MATCH;
    };
  }

  private Description matchMethodInvocation(
      MethodInvocationTree tree, VisitorState state, Type enumType) {
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
      Type receiverType = toEnumType(ASTHelpers.getReceiverType(nameArgument), state);
      if (enumSymbolPassedToValueOf == null || receiverType == null) {
        return Description.NO_MATCH;
      }

      ImmutableSet<String> enumValuesOfNameInvocationReceiver =
          invocationReceiverTree instanceof MemberSelectTree memberSelectTree
              ? ImmutableSet.of(memberSelectTree.getIdentifier().toString())
              : getEnumValues(receiverType);

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
      return capture(ASTHelpers.getReceiverType(tree), state);
    }
    if (ENUM_INSTANCE_VALUE_OF_CLASS_AND_NAME.matches(tree, state)) {
      Type classArgType = ASTHelpers.getType(tree.getArguments().getFirst());
      if (classArgType == null || classArgType.getTypeArguments().isEmpty()) {
        return new NoMatch();
      }
      return capture(classArgType.getTypeArguments().getFirst(), state);
    }
    return new NoMatch();
  }

  private static CaptureResult capture(@Nullable Type type, VisitorState state) {
    Type enumType = toEnumType(type, state);
    return enumType == null ? new NoMatch() : new Captured(enumType);
  }

  /**
   * Returns the enum type denoted by the given type, resolving type variables to their upper bound,
   * or {@code null} if it does not denote a specific enum type (e.g. {@code T extends Enum<T>}).
   */
  private static @Nullable Type toEnumType(@Nullable Type type, VisitorState state) {
    if (type == null) {
      return null;
    }

    Type upperBound = ASTHelpers.getUpperBound(type, state.getTypes());
    return upperBound.asElement().isEnum() ? upperBound : null;
  }

  private static ImmutableSet<String> getEnumValues(Type type) {
    return ImmutableSet.copyOf(ASTHelpers.enumValues(type.asElement()));
  }

  /**
   * Finds the enum values covered by the innermost enclosing switch case whose switch selects on
   * {@code enumSymbolPassedToValueOf}.
   *
   * <p>For a non-default case, returns the labels of that case. For a default case, returns the
   * enum values not covered by other cases. Returns all values if there is no such switch case.
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
  // XXX: Reassignment of the selector variable within the switch case is not taken into account.
  private static ImmutableSet<String> findSwitchCoveredValues(
      Symbol enumSymbolPassedToValueOf, ImmutableSet<String> valuesOfReceiver, VisitorState state) {
    for (TreePath path = state.getPath(); path != null; path = path.getParentPath()) {
      if (path.getLeaf() instanceof CaseTree caseTree) {
        List<? extends CaseTree> switchCases =
            getCasesIfSelecting(path.getParentPath().getLeaf(), enumSymbolPassedToValueOf);
        if (switchCases != null) {
          return ASTHelpers.isSwitchDefault(caseTree)
              ? Sets.difference(valuesOfReceiver, getLabels(switchCases, state)).immutableCopy()
              : getLabels(ImmutableList.of(caseTree), state);
        }
      }
    }

    return valuesOfReceiver;
  }

  /**
   * Returns the cases of the given switch statement or expression, or {@code null} if it does not
   * select on the given symbol.
   */
  private static @Nullable List<? extends CaseTree> getCasesIfSelecting(Tree tree, Symbol symbol) {
    return switch (tree) {
      case SwitchTree switchTree when isSymbol(switchTree.getExpression(), symbol) ->
          switchTree.getCases();
      case SwitchExpressionTree switchExpression
          when isSymbol(switchExpression.getExpression(), symbol) ->
          switchExpression.getCases();
      default -> null;
    };
  }

  private static boolean isSymbol(ExpressionTree tree, Symbol symbol) {
    return symbol.equals(ASTHelpers.getSymbol(ASTHelpers.stripParentheses(tree)));
  }

  private static ImmutableSet<String> getLabels(
      List<? extends CaseTree> cases, VisitorState state) {
    return cases.stream()
        .flatMap(caseTree -> caseTree.getLabels().stream())
        .map(label -> SourceCode.treeToString(label, state))
        .collect(toImmutableSet());
  }

  private sealed interface CaptureResult {}

  private record Captured(Type capturedType) implements CaptureResult {}

  private record NoMatch() implements CaptureResult {}
}
