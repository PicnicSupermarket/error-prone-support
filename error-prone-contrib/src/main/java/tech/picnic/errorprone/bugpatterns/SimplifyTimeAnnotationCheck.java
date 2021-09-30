package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@AutoService(BugChecker.class)
@BugPattern(
    name = "SimplifyTimeAnnotation",
    summary = "Simplifies annotations which express an amount of time using TimeUnit",
    linkType = BugPattern.LinkType.NONE,
    severity = BugPattern.SeverityLevel.WARNING,
    tags = BugPattern.StandardTags.SIMPLIFICATION)
public final class SimplifyTimeAnnotationCheck extends BugChecker implements AnnotationTreeMatcher {
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

    AnnotationDescriptor annotationDescriptor =
        AnnotationDescriptor.from(getAnnotationFqcn(annotation));
    if (containsAnyAttributeOf(annotation, annotationDescriptor.bannedFields)) {
      return Optional.empty();
    }

    ImmutableMap<String, ExpressionTree> indexedAttributes =
        Maps.uniqueIndex(
            arguments,
            expr ->
                ASTHelpers.getSymbol(((AssignmentTree) expr).getVariable())
                    .getSimpleName()
                    .toString());

    TimeUnit currentTimeUnit =
        getTimeUnit(annotation, annotationDescriptor.timeUnitField, indexedAttributes);

    ImmutableMap<String, Number> timeValues =
        annotationDescriptor.timeFields.stream()
            .map(field -> Maps.immutableEntry(field, getValue(field, indexedAttributes)))
            .filter(entry -> entry.getValue().isPresent())
            .collect(toImmutableMap(Map.Entry::getKey, entry -> entry.getValue().orElseThrow()));

    Map<String, TimeSimplifier.Simplification> simplifications =
        Maps.transformValues(
            Maps.filterValues(
                Maps.transformEntries(
                    timeValues, (field, value) -> trySimplify(value, currentTimeUnit)),
                Optional::isPresent),
            Optional::orElseThrow);

    // Some could not be simplified, and since the unit is shared, the others can't either.
    if (simplifications.size() != timeValues.size()) {
      return Optional.empty();
    }

    // The annotation is of the form `@Annotation(v)` or `@Annotation(value = v)`. For the former we
    // must synthesize the entire annotation, but this is OK for the latter, too.
    if (indexedAttributes.size() == 1 && indexedAttributes.containsKey("value")) {
      TimeSimplifier.Simplification simplification = simplifications.get("value");
      return Optional.of(
          getImplicitValueAttributeFix(
              annotation,
              simplification.value,
              annotationDescriptor.timeUnitField,
              simplification.unit));
    }

    // Since each might have a different simplification possible, check the common unit.
    // Since we only get simplifications iff it's possible, and we check that all can be simplified,
    // we don't need to check if this equals `currentTimeUnit`.
    TimeUnit commonUnit =
        findCommonUnit(
            ImmutableSet.copyOf(
                Maps.transformValues(simplifications, simplification -> simplification.unit)
                    .values()));

    return getExplicitAttributesFix(
        annotation, simplifications, annotationDescriptor.timeUnitField, commonUnit);
  }

  private static boolean containsAnyAttributeOf(
      AnnotationTree annotation, ImmutableSet<String> attributes) {
    return annotation.getArguments().stream()
        .map(
            expr ->
                expr.getKind() == Tree.Kind.ASSIGNMENT
                    ? ASTHelpers.getSymbol(((AssignmentTree) expr).getVariable())
                        .getSimpleName()
                        .toString()
                    : "value")
        .anyMatch(attributes::contains);
  }

  private static Fix getImplicitValueAttributeFix(
      AnnotationTree annotation, Number newValue, String timeUnitField, TimeUnit newTimeUnit) {
    return SuggestedFix.builder()
        .replace(
            annotation,
            annotation
                .toString()
                .replaceFirst(
                    "\\(.+\\)",
                    String.format(
                        "(value=%s, %s=%s)", newValue, timeUnitField, newTimeUnit.name())))
        .addStaticImport(TimeUnit.class.getName() + '.' + newTimeUnit.name())
        .build();
  }

  private static Optional<Fix> getExplicitAttributesFix(
      AnnotationTree annotation,
      Map<String, TimeSimplifier.Simplification> simplifications,
      String timeUnitField,
      TimeUnit newUnit) {
    return simplifications.entrySet().stream()
        .map(
            simplificationEntry ->
                SuggestedFixes.updateAnnotationArgumentValues(
                        annotation, timeUnitField, ImmutableList.of(newUnit.name()))
                    .merge(
                        SuggestedFixes.updateAnnotationArgumentValues(
                            annotation,
                            simplificationEntry.getKey(),
                            ImmutableList.of(
                                String.valueOf(simplificationEntry.getValue().toUnit(newUnit))))))
        .reduce(SuggestedFix.Builder::merge)
        .map(builder -> builder.addStaticImport(TimeUnit.class.getName() + '.' + newUnit.name()))
        .map(SuggestedFix.Builder::build);
  }

  private static String getAnnotationFqcn(AnnotationTree annotation) {
    return ASTHelpers.getSymbol(annotation).getQualifiedName().toString();
  }

  private static Optional<Number> getValue(
      String field, ImmutableMap<String, ExpressionTree> indexedArguments) {
    return Optional.ofNullable(indexedArguments.get(field))
        .filter(AssignmentTree.class::isInstance)
        .map(AssignmentTree.class::cast)
        .map(AssignmentTree::getExpression)
        .map(expr -> ASTHelpers.constValue(expr, Number.class));
  }

  private static TimeUnit getTimeUnit(
      AnnotationTree annotation,
      String field,
      ImmutableMap<String, ExpressionTree> indexedArguments) {
    VarSymbol symbol =
        Optional.ofNullable(indexedArguments.get(field))
            .map(
                argumentTree ->
                    (VarSymbol)
                        ASTHelpers.getSymbol(((AssignmentTree) argumentTree).getExpression()))
            .orElseGet(() -> getDefaultTimeUnit(annotation, field));
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
        Arrays.stream(AnnotationDescriptor.values())
            .flatMap(
                annotation ->
                    annotation.timeFields.stream().map(field -> annotation.fqcn + '#' + field))
            .collect(toImmutableList());
    return AnnotationAttributeMatcher.create(Optional.of(toMatch), ImmutableList.of());
  }

  private static Optional<TimeSimplifier.Simplification> trySimplify(Number value, TimeUnit unit) {
    checkArgument(
        value instanceof Integer || value instanceof Long,
        "Only time expressed as a long or integer can be simplified");
    return TimeSimplifier.simplify(value.longValue(), unit)
        .map(simplification -> simplification.ensureNumberIsOfType(value.getClass()));
  }

  private static TimeUnit findCommonUnit(ImmutableSet<TimeUnit> units) {
    return ImmutableSortedSet.copyOf(units).first();
  }

  private enum AnnotationDescriptor {
    JUNIT_TIMEOUT("org.junit.jupiter.api.Timeout", ImmutableSet.of("value"), "unit"),
    SPRING_SCHEDULED(
        "org.springframework.scheduling.annotation.Scheduled",
        ImmutableSet.of("fixedDelay", "fixedRate", "initialDelay"),
        "timeUnit",
        ImmutableSet.of("fixedDelayString", "fixedRateString", "initialDelayString"));

    /** The fully-qualified class name of the annotation to simplify. */
    private final String fqcn;
    /** The attributes containing a value of time. */
    private final ImmutableSet<String> timeFields;
    /** The attribute containing the time unit. */
    private final String timeUnitField;
    /** The set of attributes that cause the check to backoff. */
    private final ImmutableSet<String> bannedFields;

    AnnotationDescriptor(String fqcn, ImmutableSet<String> timeFields, String timeUnitField) {
      this(fqcn, timeFields, timeUnitField, ImmutableSet.of());
    }

    AnnotationDescriptor(
        String fqcn,
        ImmutableSet<String> timeFields,
        String timeUnitField,
        ImmutableSet<String> bannedFields) {
      this.fqcn = fqcn;
      this.timeFields = timeFields;
      this.timeUnitField = timeUnitField;
      this.bannedFields = bannedFields;
    }

    public static AnnotationDescriptor from(String fqcn) {
      return Arrays.stream(values())
          .filter(annotation -> annotation.fqcn.equals(fqcn))
          .findFirst()
          .orElseThrow(
              () ->
                  new IllegalArgumentException(
                      String.format(
                          "Unknown enum constant: %s.%s",
                          AnnotationDescriptor.class.getName(), fqcn)));
    }
  }

  /** Utility class to help simplify time expressions. */
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

    /** Represents a simplification in terms of the new value and new unit. */
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

      /**
       * Ensures that {@link #value} returns a {@link Number} of the same type as {@code original}.
       * Since a {@link Simplification} can only have smaller or values equal to the original value,
       * this cannot result in an overflow if we need to go from a long to an integer.
       */
      public Simplification ensureNumberIsOfType(Class<? extends Number> original) {
        return original.equals(value.getClass())
            ? this
            : new Simplification(value.intValue(), unit);
      }

      /**
       * Converts the value with the unit represented by this simplification to an equivalent value
       * in the given {@code unit}.
       */
      public long toUnit(TimeUnit unit) {
        return unit.convert(value.longValue(), this.unit);
      }
    }
  }
}
