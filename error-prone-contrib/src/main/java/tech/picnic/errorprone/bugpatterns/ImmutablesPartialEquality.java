package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.util.ASTHelpers.getEnclosedElements;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import java.util.stream.Stream;

/**
 * A {@link BugChecker} that flags usages of {@code isEqualTo(...)} in tests where no recursive
 * comparison is used and the compared type has partial or custom equality semantics due to
 * Immutables configuration.
 *
 * <p>This includes types that are:
 *
 * <ul>
 *   <li>Annotated with {@code @Value.Immutable} and either override {@code equals()} or contain
 *       {@code @Value.Auxiliary} fields, or
 *   <li>Annotated with {@code @Immutable } and extend or implement such types.
 * </ul>
 *
 * <p>These equality comparisons are considered risky because {@code isEqualTo(...)} implies full
 * structural equality, but these types may exclude fields or customize equality behavior.
 *
 * <p>This checker encourages safer alternatives such as {@code usingRecursiveComparison()},
 * custom assertion helpers, or explicit field comparisons for these types.
 */

@AutoService(BugChecker.class)
@BugPattern(
    summary = "Immutables with partial equality should not be compared using isEqualTo in tests",
    explanation =
        "Immutables that override equals() or use @Value.Auxiliary fields should not be compared using isEqualTo, as these comparisons can lead to misleading test results. Prefer asserting with `usingRecursiveComparison()` for these classes.",
    severity = WARNING,
    tags = BugPattern.StandardTags.FRAGILE_CODE)
public final class ImmutablesPartialEquality extends BugChecker
    implements BugChecker.MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;

  private static final Matcher<ExpressionTree> IS_EQUAL_TO =
      instanceMethod().onDescendantOf("org.assertj.core.api.Assert").named("isEqualTo");

  /** Instantiates a new {@link ImmutablesPartialEquality} instance. */
  public ImmutablesPartialEquality() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!IS_EQUAL_TO.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    if (usesRecursiveComparison(tree)) {
      return Description.NO_MATCH;
    }

    ExpressionTree argument = tree.getArguments().get(0);
    Type argType = ASTHelpers.getType(argument);
    if (!(argType != null && argType.tsym instanceof ClassSymbol classSymbol)) {
      return Description.NO_MATCH;
    }

    if (isRiskyType(classSymbol, state)) {
      return describeMatch(tree);
    }

    return Description.NO_MATCH;
  }

  private static boolean usesRecursiveComparison(MethodInvocationTree tree) {
    ExpressionTree current = ASTHelpers.getReceiver(tree);

    while (current instanceof MethodInvocationTree methodInvocation) {
      Symbol.MethodSymbol symbol = ASTHelpers.getSymbol(methodInvocation);
      if (symbol != null && symbol.getSimpleName().contentEquals("usingRecursiveComparison")) {
        return true;
      }

      current = ASTHelpers.getReceiver(methodInvocation);
    }

    return false;
  }

  private static boolean isRiskyType(ClassSymbol classSymbol, VisitorState state) {
    return isSelfRiskyImmutable(classSymbol, state)
        || (ASTHelpers.hasAnnotation(classSymbol, "javax.annotation.concurrent.Immutable", state)
            && hasRiskyImmutableSubtype(classSymbol, state));
  }

  private static boolean isSelfRiskyImmutable(ClassSymbol classSymbol, VisitorState state) {
    return ASTHelpers.hasAnnotation(classSymbol, "org.immutables.value.Value.Immutable", state)
        && (hasAuxiliaryField(classSymbol, state) || overridesEquals(classSymbol));
  }

  private static boolean hasRiskyImmutableSubtype(ClassSymbol classSymbol, VisitorState state) {
    return Stream.concat(
            classSymbol.getInterfaces().stream(), Stream.of(classSymbol.getSuperclass()))
        .map(t -> t.tsym)
        .filter(ClassSymbol.class::isInstance)
        .map(ClassSymbol.class::cast)
        .filter(parent -> !parent.getQualifiedName().contentEquals("java.lang.Object"))
        .anyMatch(parent -> isSelfRiskyImmutable(parent, state));
  }

  private static boolean overridesEquals(ClassSymbol classSymbol) {
    if (classSymbol.members() == null || classSymbol.members().isEmpty()) {
      return false;
    }

    for (Symbol member : classSymbol.members().getSymbols()) {
      if (member instanceof Symbol.MethodSymbol method
          && method.name.contentEquals("equals")
          && method.getParameters().size() == 1
          && method
              .getParameters()
              .get(0)
              .type
              .tsym
              .getQualifiedName()
              .contentEquals("java.lang.Object")) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasAuxiliaryField(ClassSymbol classSymbol, VisitorState state) {
    return getEnclosedElements(classSymbol).stream()
        .filter(e -> e instanceof Symbol.MethodSymbol)
        .anyMatch(
            method ->
                ASTHelpers.hasAnnotation(method, "org.immutables.value.Value.Auxiliary", state));
  }
}
