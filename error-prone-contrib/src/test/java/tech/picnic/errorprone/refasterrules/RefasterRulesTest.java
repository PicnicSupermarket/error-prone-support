package tech.picnic.errorprone.refasterrules;

import static java.util.function.Predicate.not;

import com.google.common.collect.ImmutableSet;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollection;

final class RefasterRulesTest {
  /** The names of all Refaster rule groups defined in this module. */
  private static final ImmutableSet<Class<?>> RULE_COLLECTIONS =
      ImmutableSet.of(
          AssertJRules.class,
          AssertJBigDecimalRules.class,
          AssertJBigIntegerRules.class,
          AssertJBooleanRules.class,
          AssertJByteRules.class,
          AssertJCharSequenceRules.class,
          AssertJComparableRules.class,
          AssertJDoubleRules.class,
          AssertJEnumerableRules.class,
          AssertJFloatRules.class,
          AssertJIntegerRules.class,
          AssertJLongRules.class,
          AssertJNumberRules.class,
          AssertJMapRules.class,
          AssertJObjectRules.class,
          AssertJOptionalRules.class,
          AssertJPrimitiveRules.class,
          AssertJShortRules.class,
          AssertJStringRules.class,
          AssertJThrowingCallableRules.class,
          AssortedRules.class,
          BigDecimalRules.class,
          CollectionRules.class,
          ComparatorRules.class,
          DoubleStreamRules.class,
          EqualityRules.class,
          ImmutableListRules.class,
          ImmutableListMultimapRules.class,
          ImmutableMapRules.class,
          ImmutableMultisetRules.class,
          ImmutableSetRules.class,
          ImmutableSetMultimapRules.class,
          ImmutableSortedMapRules.class,
          ImmutableSortedMultisetRules.class,
          ImmutableSortedSetRules.class,
          IntStreamRules.class,
          JUnitRules.class,
          LongStreamRules.class,
          MapEntryRules.class,
          MockitoRules.class,
          MultimapRules.class,
          NullRules.class,
          OptionalRules.class,
          PrimitiveRules.class,
          ReactorRules.class,
          RxJava2AdapterRules.class,
          StreamRules.class,
          StringRules.class,
          TestNGToAssertJRules.class,
          TimeRules.class,
          WebClientRules.class);

  // XXX: Create a JUnit extension to automatically discover the rule collections in a given
  // context to make sure the list is exhaustive.
  private static Stream<Arguments> validateRuleCollectionTestCases() {
    // XXX: Drop the filter once we have added tests for AssertJ! We can then also replace this
    // method with `@ValueSource(classes = {...})`.
    return RULE_COLLECTIONS.stream()
        .filter(not(AssertJRules.class::equals))
        .map(Arguments::arguments);
  }

  @MethodSource("validateRuleCollectionTestCases")
  @ParameterizedTest
  void validateRuleCollection(Class<?> clazz) {
    RefasterRuleCollection.validate(clazz);
  }
}
