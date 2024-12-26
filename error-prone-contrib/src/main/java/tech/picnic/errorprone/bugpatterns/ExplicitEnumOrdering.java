package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.ImmutableSetMultimap.toImmutableSetMultimap;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.FRAGILE_CODE;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static java.util.stream.Collectors.collectingAndThen;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A {@link BugChecker} that flags {@link Ordering#explicit(Object, Object[])}} invocations listing
 * a subset of an enum type's values.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Make sure `Ordering#explicit` lists all of an enum's values",
    link = BUG_PATTERNS_BASE_URL + "ExplicitEnumOrdering",
    linkType = CUSTOM,
    severity = WARNING,
    tags = FRAGILE_CODE)
public final class ExplicitEnumOrdering extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> EXPLICIT_ORDERING =
      staticMethod().onClass(Ordering.class.getCanonicalName()).named("explicit");

  /** Instantiates a new {@link ExplicitEnumOrdering} instance. */
  public ExplicitEnumOrdering() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!EXPLICIT_ORDERING.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    ImmutableSet<String> missingEnumValues = getMissingEnumValues(tree.getArguments());
    if (missingEnumValues.isEmpty()) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .setMessage(
            String.format(
                "Explicit ordering lacks some enum values: %s",
                String.join(", ", missingEnumValues)))
        .build();
  }

  private static ImmutableSet<String> getMissingEnumValues(
      List<? extends ExpressionTree> expressions) {
    return expressions.stream()
        .map(ASTHelpers::getSymbol)
        .filter(s -> s != null && s.isEnum())
        .collect(
            collectingAndThen(
                toImmutableSetMultimap(Symbol::asType, Symbol::toString),
                ExplicitEnumOrdering::getMissingEnumValues));
  }

  private static ImmutableSet<String> getMissingEnumValues(
      ImmutableSetMultimap<Type, String> valuesByType) {
    return Multimaps.asMap(valuesByType).entrySet().stream()
        .flatMap(e -> getMissingEnumValues(e.getKey(), e.getValue()))
        .collect(toImmutableSet());
  }

  private static Stream<String> getMissingEnumValues(Type enumType, Set<String> values) {
    Symbol.TypeSymbol typeSymbol = enumType.asElement();
    return Sets.difference(ASTHelpers.enumValues(typeSymbol), values).stream()
        .map(v -> String.join(".", typeSymbol.getSimpleName(), v));
  }
}
