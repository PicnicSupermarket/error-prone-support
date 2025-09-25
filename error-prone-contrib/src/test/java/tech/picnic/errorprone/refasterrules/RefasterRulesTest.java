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
          //          AssertJArrayRules.class,
          //          AssertJBigDecimalRules.class,
          //          AssertJBigIntegerRules.class,
          //          AssertJBooleanRules.class,
          //          AssertJByteRules.class,
          //          AssertJCharSequenceRules.class,
          //          AssertJComparableRules.class,
          //          AssertJDoubleRules.class,
          //          AssertJDurationRules.class,
          //          AssertJEnumerableRules.class,
          //          AssertJInstantRules.class,
          //          AssertJFileRules.class,
          //          AssertJFloatRules.class,
          //          AssertJIntegerRules.class,
          //          AssertJIterableRules.class,
          //          AssertJIteratorRules.class,
          //          AssertJLongRules.class,
          //          AssertJMapRules.class,
          //          AssertJNumberRules.class,
          //          AssertJObjectRules.class,
          //          AssertJOptionalRules.class,
          //          AssertJPathRules.class,
          //          AssertJPrimitiveRules.class,
          //          AssertJRules.class,
          //          AssertJShortRules.class,
          //          AssertJStreamRules.class,
          //          AssertJStringRules.class,
          //          AssertJThrowingCallableRules.class,
          //          AssortedRules.class,
          //          BigDecimalRules.class,
          //          BugCheckerRules.class,
          //          CharSequenceRules.class,
          //          ClassRules.class,
          //          CollectionRules.class,
          //          ComparatorRules.class,
          //          DoubleStreamRules.class,
          //          EqualityRules.class,
          //          FileRules.class,
          //          ImmutableEnumSetRules.class,
          //          ImmutableListRules.class,
          //          ImmutableListMultimapRules.class,
          //          ImmutableMapRules.class,
          //          ImmutableMultisetRules.class,
          //          ImmutableSetRules.class,
          //          ImmutableSetMultimapRules.class,
          //          ImmutableSortedMapRules.class,
          //          ImmutableSortedMultisetRules.class,
          //          ImmutableSortedSetRules.class,
          //          ImmutableTableRules.class,
          //          InputStreamRules.class,
          //          IntStreamRules.class,
          //          JacksonRules.class,
          //          JUnitRules.class,
          //          JUnitToAssertJRules.class,
          //          LongStreamRules.class,
          //          MapEntryRules.class,
          //          MapRules.class,
          //          MicrometerRules.class,
          //          MockitoRules.class,
          //          MultimapRules.class,
          //          NullRules.class,
          //          OptionalRules.class,
          //          PatternRules.class,
          //          PreconditionsRules.class,
          //          PrimitiveRules.class,
          //          RandomGeneratorRules.class,
          //          ReactorRules.class,
          //          RxJava2AdapterRules.class,
          //          StreamRules.class,
          StringRules.class,
          //          SuggestedFixRules.class,
          //          TestNGToAssertJRules.class,
          //          TimeRules.class,
          WebClientRules.class);

  // XXX: Create a JUnit extension to automatically discover the rule collections in a given context
  // to make sure the list is exhaustive.
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
