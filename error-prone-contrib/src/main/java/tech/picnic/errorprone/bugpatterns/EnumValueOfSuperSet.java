package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
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
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;

/** hehe. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Avoid passing arguments to `valueOf` from a superset enum",
    link = BUG_PATTERNS_BASE_URL + "EnumValueOfSuperSet",
    linkType = CUSTOM,
    severity = WARNING,
    tags = FRAGILE_CODE)
public final class EnumValueOfSuperSet extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link EnumValueOfSuperSet} instance. */
  public EnumValueOfSuperSet() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    Matcher<ExpressionTree> valueOfMatcher =
        staticMethod().onDescendantOf(Enum.class.getCanonicalName()).named("valueOf");
    Matcher<ExpressionTree> valueOfArgumentMatcher =
        instanceMethod()
            .onDescendantOf(Enum.class.getCanonicalName())
            .namedAnyOf("name", "toString");
    if (valueOfMatcher.matches(tree, state)) {
      MethodInvocationTree enumNameInvocation =
          (MethodInvocationTree)
              tree.getArguments().stream()
                  .filter(argument -> valueOfArgumentMatcher.matches(argument, state))
                  .collect(toImmutableList())
                  .getFirst();

      ImmutableSet<String> valuesOfSource = getEnumValues(tree);
      ImmutableSet<String> valuesOfTarget = getEnumValues(enumNameInvocation);

      ImmutableSet<String> difference =
          Sets.difference(valuesOfTarget, valuesOfSource).immutableCopy();

      return difference.isEmpty() ? Description.NO_MATCH : describeMatch(tree);
    }
    return Description.NO_MATCH;
  }

  private static ImmutableSet<String> getEnumValues(MethodInvocationTree tree) {
    return ImmutableSet.copyOf(
        ASTHelpers.enumValues(
            requireNonNull(
                    ASTHelpers.getType(((MemberSelectTree) tree.getMethodSelect()).getExpression()))
                .tsym));
  }
}
