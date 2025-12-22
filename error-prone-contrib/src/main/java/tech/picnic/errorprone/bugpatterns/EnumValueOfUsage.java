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
import com.sun.source.util.TreePath;
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

    Optional<? extends ExpressionTree> optionalNameArgument = findStringArgument(tree);
    if (optionalNameArgument.isEmpty()) {
      return Description.NO_MATCH;
    }

    ExpressionTree nameArgument = optionalNameArgument.orElseThrow();
    String constantValue = ASTHelpers.constValue(nameArgument, String.class);

    ImmutableSet<String> valuesOfSource = findEnumValuesOfMethodInvocationTree(tree);

    // Match constants
    if (constantValue != null) {
      return valuesOfSource.contains(constantValue) ? Description.NO_MATCH : describeMatch(tree);
    }

    // Match unchecked String values
    if (nameArgument instanceof IdentifierTree) {
      return describeMatch(tree);
    }

    // Match name argument where it is another enum's .name() or .toString() invocation.
    if (nameArgument instanceof MethodInvocationTree anotherEnumsInvocation
        && STRING_VALUE_ENUM.matches(anotherEnumsInvocation, state)) {

      // Check if it is a part of switch-case statement
      CaseTree filteredCaseTree =
          ASTHelpers.findEnclosingNode(
              TreePath.getPath(state.getPath(), nameArgument), CaseTree.class);
      ImmutableSet<String> valuesOfTarget =
          filteredCaseTree == null
              ? findEnumValuesOfMethodInvocationTree(anotherEnumsInvocation)
              : filteredCaseTree.getLabels().stream()
                  .map(Object::toString)
                  .collect(toImmutableSet());

      return Sets.difference(valuesOfTarget, valuesOfSource).isEmpty()
          ? Description.NO_MATCH
          : describeMatch(tree);
    }

    return Description.NO_MATCH;
  }

  /**
   * {@link Enum#valueOf} contains two implementations, we are interested in finding the {@code
   * name}:
   *
   * <pre>{@code
   * valueOf(String name)
   * valueOf(Class<T> enumClass, String name)
   * }</pre>
   *
   * @param tree {@link MethodInvocationTree} that belongs to {@code valueOf} call.
   * @return {@link ExpressionTree} implementation of {@code name} argument or empty if no {@link
   *     String} argument found.
   */
  private static Optional<? extends ExpressionTree> findStringArgument(MethodInvocationTree tree) {
    return tree.getArguments().stream()
        .filter(
            argument ->
                Optional.ofNullable(ASTHelpers.getType(argument))
                    .filter(type -> type.toString().equals(String.class.getCanonicalName()))
                    .isPresent())
        .findAny();
  }

  private static ImmutableSet<String> findEnumValuesOfMethodInvocationTree(
      MethodInvocationTree tree) {
    return ImmutableSet.copyOf(ASTHelpers.enumValues(ASTHelpers.getReceiverType(tree).tsym));
  }
}
