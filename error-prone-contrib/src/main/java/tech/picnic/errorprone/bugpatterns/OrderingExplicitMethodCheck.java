package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.matchers.Matchers.not;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Name;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A {@link BugChecker} which flags that {@link com.google.common.collect.Ordering#explicit(Object,
 * Object[])}} for enums does not contain all it's values.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "OrderingExplicitMethod",
    summary =
        "Make sure Ordering.explicit() comparator with enum types contains all values from it",
    linkType = BugPattern.LinkType.NONE,
    severity = BugPattern.SeverityLevel.WARNING,
    tags = BugPattern.StandardTags.FRAGILE_CODE)
public final class OrderingExplicitMethodCheck extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (not(staticMethod().onClass(Ordering.class.getName()).named("explicit"))
        .matches(tree, state)) {
      return Description.NO_MATCH;
    }
    List<? extends ExpressionTree> arguments = tree.getArguments();
    if (isNotEnumType(arguments)) {
      return Description.NO_MATCH;
    }

    ImmutableSet<String> actualValues =
        arguments.stream()
            .map(arg -> (JCTree.JCFieldAccess) arg)
            .map(arg -> arg.name)
            .map(Name::toString)
            .collect(toImmutableSet());

    LinkedHashSet<String> enumValues =
        getTypeSymbolStream(arguments).map(ASTHelpers::enumValues).findFirst().orElseThrow();

    if (actualValues.containsAll(enumValues)) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .setMessage(
            String.format(
                "Method should include all values from %s enum", getClassSimpleName(arguments)))
        .build();
  }

  private static boolean isNotEnumType(List<? extends ExpressionTree> arguments) {
    return arguments.stream().anyMatch(Predicate.not(arg -> ASTHelpers.getSymbol(arg).isEnum()));
  }

  private static String getClassSimpleName(List<? extends ExpressionTree> arguments) {
    return getTypeSymbolStream(arguments)
        .map(Symbol::getSimpleName)
        .map(Name::toString)
        .findFirst()
        .orElseThrow();
  }

  private static Stream<Symbol.TypeSymbol> getTypeSymbolStream(
      List<? extends ExpressionTree> arguments) {
    return arguments.stream()
        .map(ASTHelpers::getType)
        .filter(Objects::nonNull)
        .map(type -> type.tsym);
  }
}
