package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@AutoService(BugChecker.class)
@BugPattern(
    name = "SimplifyTimeBasedAnnotation",
    summary = "Simplifies annotations which express an amount of time using TimeUnit",
    linkType = BugPattern.LinkType.NONE,
    severity = BugPattern.SeverityLevel.WARNING,
    tags = BugPattern.StandardTags.SIMPLIFICATION)
public final class SimplifyTimeBasedAnnotationCheck extends BugChecker
    implements AnnotationTreeMatcher {
  private static ImmutableListMultimap<String, String> ANNOTATION_ATTRIBUTES =
      ImmutableListMultimap.<String, String>builder()
          .putAll("org.junit.jupiter.api.Timeout", "value", "unit")
          .build();

  private static final AnnotationAttributeMatcher ARGUMENT_SELECTOR = getMatcher();

  @Override
  public Description matchAnnotation(AnnotationTree annotationTree, VisitorState visitorState) {
    ImmutableList<ExpressionTree> arguments =
        ARGUMENT_SELECTOR.extractMatchingArguments(annotationTree).collect(toImmutableList());

    if (arguments.isEmpty()) {
      return Description.NO_MATCH;
    }

    return trySimplification(annotationTree, arguments)
        .map(fix -> describeMatch(annotationTree, fix))
        .orElse(Description.NO_MATCH);
  }

  private static Optional<Fix> trySimplification(
      AnnotationTree annotation, ImmutableList<ExpressionTree> arguments) {
    checkArgument(!arguments.isEmpty());

    ImmutableMap<String, ExpressionTree> indexedArguments =
        Maps.uniqueIndex(
            arguments,
            expr ->
                ASTHelpers.getSymbol(((AssignmentTree) expr).getVariable())
                    .getSimpleName()
                    .toString());

    Number value = getValue(annotation, indexedArguments);
    TimeUnit timeUnit = getTimeUnit(annotation, indexedArguments);

    return simplifyUnit(value, timeUnit)
        .map(
            simplification ->
                // handle the @Annotation(value) case separately by synthesizing it completely.
                SuggestedFixes.updateAnnotationArgumentValues(
                        annotation,
                        getTimeUnitArgumentName(annotation),
                        ImmutableList.of(simplification.getUnit().name()))
                    .merge(
                        SuggestedFixes.updateAnnotationArgumentValues(
                            annotation,
                            getValueArgumentName(annotation),
                            ImmutableList.of(simplification.getValue().toString())))
                    .addStaticImport(
                        TimeUnit.class.getName() + '.' + simplification.getUnit().name())
                    .build());
  }

  private static Number getValue(
      AnnotationTree annotationTree, ImmutableMap<String, ExpressionTree> indexedArguments) {
    String valueName = getValueArgumentName(annotationTree);
    return (Number)
        ASTHelpers.constValue(((AssignmentTree) indexedArguments.get(valueName)).getExpression());
  }

  private static String getValueArgumentName(AnnotationTree annotationTree) {
    return getArgumentName(annotationTree, 0);
  }

  private static String getTimeUnitArgumentName(AnnotationTree annotationTree) {
    return getArgumentName(annotationTree, 1);
  }

  private static String getArgumentName(AnnotationTree annotationTree, int index) {
    return ANNOTATION_ATTRIBUTES
        .get(ASTHelpers.getSymbol(annotationTree).getQualifiedName().toString())
        .get(index);
  }

  private static TimeUnit getTimeUnit(
      AnnotationTree annotation, ImmutableMap<String, ExpressionTree> indexedArguments) {
    String argument = getTimeUnitArgumentName(annotation);
    VarSymbol symbol =
        Optional.ofNullable(indexedArguments.get(argument))
            .map(
                argumentTree ->
                    (VarSymbol)
                        ASTHelpers.getSymbol(((AssignmentTree) argumentTree).getExpression()))
            .orElseGet(() -> getDefaultTimeUnit(annotation, argument));
    return TimeUnit.valueOf(symbol.getQualifiedName().toString());
  }

  private static VarSymbol getDefaultTimeUnit(AnnotationTree annotation, String argument) {
    Scope scope = ASTHelpers.getSymbol(annotation).members();
    MethodSymbol argumentSymbol =
        (MethodSymbol)
            Iterables.getOnlyElement(
                scope.getSymbols(symbol -> symbol.getQualifiedName().contentEquals(argument)));
    return (VarSymbol) argumentSymbol.getDefaultValue().getValue();
  }

  private static AnnotationAttributeMatcher getMatcher() {
    ImmutableList<String> toMatch =
        ANNOTATION_ATTRIBUTES.entries().stream()
            .map(entry -> entry.getKey() + '#' + entry.getValue())
            .collect(toImmutableList());
    return AnnotationAttributeMatcher.create(Optional.of(toMatch), ImmutableList.of());
  }

  private static Optional<TimeSimplifier.Simplification> simplifyUnit(Number value, TimeUnit unit) {
    checkArgument(
        value instanceof Integer || value instanceof Long,
        "Only time expressed as a long or integer can be simplified");
    return TimeSimplifier.simplify(value.longValue(), unit)
        .map(simplification -> simplification.ensureNumberIsOfType(value.getClass()));
  }

  private static final class TimeSimplifier {
    /**
     * Returns a {@link Simplification} (iff possible) that describes how the {@code originalValue}
     * and {@code originalUnit} can be simplified using a larger {@link TimeUnit}.
     */
    static Optional<Simplification> simplify(long originalValue, TimeUnit originalUnit) {
      ImmutableList<TimeUnit> ceiling = descendingLargerUnits(originalUnit);
      return ceiling.stream()
          .flatMap(unit -> trySimplify(originalValue, originalUnit, unit))
          .findFirst();
    }

    private static Stream<Simplification> trySimplify(
        long originalValue, TimeUnit originalUnit, TimeUnit unit) {
      long converted = unit.convert(originalValue, originalUnit);
      // Check if we lose any precision by checking if we can convert back.
      return originalValue == originalUnit.convert(converted, unit)
          ? Stream.of(new Simplification(converted, unit))
          : Stream.empty();
    }

    /**
     * Returns all time units that represent a larger amount of time than {@code unit} in descending
     * order.
     */
    private static ImmutableList<TimeUnit> descendingLargerUnits(TimeUnit unit) {
      return Arrays.stream(TimeUnit.values())
          .filter(u -> u.compareTo(unit) > 0)
          .collect(toImmutableList())
          .reverse();
    }

    private static final class Simplification {
      private final Number value;
      private final TimeUnit unit;

      public Simplification(int value, TimeUnit unit) {
        this.value = value;
        this.unit = unit;
      }

      public Simplification(long value, TimeUnit unit) {
        this.value = value;
        this.unit = unit;
      }

      public Number getValue() {
        return value;
      }

      public TimeUnit getUnit() {
        return unit;
      }

      /**
       * Ensures that {@link #getValue()} returns a {@link Number} of the same type as {@code
       * original}. Since a {@link Simplification} can only have smaller or values equal to the
       * original value, this cannot result in an overflow if we need to go from a long to an
       * integer.
       */
      public Simplification ensureNumberIsOfType(Class<? extends Number> original) {
        return original.equals(value.getClass())
            ? this
            : new Simplification(value.intValue(), unit);
      }
    }
  }
}
