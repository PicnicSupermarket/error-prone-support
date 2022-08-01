package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableSet;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.picnic.errorprone.refaster.test.RefasterTemplateCollectionValidator;

final class RefasterTemplatesTest {
  /** The names of all Refaster template groups defined in this module. */
  private static final ImmutableSet<Class<?>> TEMPLATE_COLLECTIONS =
      ImmutableSet.of(
          AssertJTemplates.class,
          AssertJBigDecimalTemplates.class,
          AssertJBigIntegerTemplates.class,
          AssertJBooleanTemplates.class,
          AssertJByteTemplates.class,
          AssertJCharSequenceTemplates.class,
          AssertJDoubleTemplates.class,
          AssertJEnumerableTemplates.class,
          AssertJFloatTemplates.class,
          AssertJIntegerTemplates.class,
          AssertJLongTemplates.class,
          AssertJNumberTemplates.class,
          AssertJMapTemplates.class,
          AssertJObjectTemplates.class,
          AssertJOptionalTemplates.class,
          AssertJShortTemplates.class,
          AssertJStringTemplates.class,
          AssertJThrowingCallableTemplates.class,
          AssortedTemplates.class,
          BigDecimalTemplates.class,
          CollectionTemplates.class,
          ComparatorTemplates.class,
          DoubleStreamTemplates.class,
          EqualityTemplates.class,
          ImmutableListTemplates.class,
          ImmutableListMultimapTemplates.class,
          ImmutableMapTemplates.class,
          ImmutableMultisetTemplates.class,
          ImmutableSetTemplates.class,
          ImmutableSetMultimapTemplates.class,
          ImmutableSortedMapTemplates.class,
          ImmutableSortedMultisetTemplates.class,
          ImmutableSortedSetTemplates.class,
          IntStreamTemplates.class,
          JUnitTemplates.class,
          LongStreamTemplates.class,
          MapEntryTemplates.class,
          MockitoTemplates.class,
          MultimapTemplates.class,
          NullTemplates.class,
          OptionalTemplates.class,
          PrimitiveTemplates.class,
          ReactorTemplates.class,
          RxJava2AdapterTemplates.class,
          StreamTemplates.class,
          StringTemplates.class,
          TestNGToAssertJTemplates.class,
          TimeTemplates.class,
          WebClientTemplates.class);

  // XXX: Create a JUnit extension to automatically discover the template collections in a given
  // context to make sure the list is exhaustive.
  private static Stream<Arguments> validateTemplateCollectionTestCases() {
    // XXX: Drop the filter once we have added tests for AssertJ!
    return TEMPLATE_COLLECTIONS.stream()
        .filter(tc -> tc != AssertJTemplates.class)
        .map(Arguments::arguments);
  }

  @MethodSource("validateTemplateCollectionTestCases")
  @ParameterizedTest
  void validateTemplateCollection(Class<?> clazz) {
    RefasterTemplateCollectionValidator.validate(clazz);
  }
}
