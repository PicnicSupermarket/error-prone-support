package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMultiset;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractDoubleAssert;
import org.assertj.core.api.AbstractFloatAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractLongAssert;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.OptionalAssert;
import org.assertj.core.api.OptionalDoubleAssert;
import org.assertj.core.api.OptionalIntAssert;
import org.assertj.core.api.OptionalLongAssert;

/** Refaster templates related to AssertJ expressions and statements. */
// XXX: Most `AbstractIntegerAssert` rules can also be applied for other primitive types. Generate
// these in separate files.
// XXX: `assertThat(cmp.compare(a, b)).isZero()` -> make something nicer.
// XXX: Consider splitting this class into multiple classes.
// XXX: Some of these rules may not apply given the updated TestNG rewrite rules. Review.
// XXX: Use `S, T extends S` bounds to avoid re-applying incompatible constructs.
final class AssertJTemplates {
  private AssertJTemplates() {}

  //
  // Boolean
  //

  static final class AssertThatBooleanIsEqualTo<E> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(AbstractBooleanAssert<?> boolAssert, boolean other) {
      return boolAssert.isNotEqualTo(!other);
    }

    @AfterTemplate
    AbstractBooleanAssert<?> after(AbstractBooleanAssert<?> boolAssert, boolean other) {
      return boolAssert.isEqualTo(other);
    }
  }

  static final class AssertThatBooleanIsNotEqualTo<E> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(AbstractBooleanAssert<?> boolAssert, boolean other) {
      return boolAssert.isEqualTo(!other);
    }

    @AfterTemplate
    AbstractBooleanAssert<?> after(AbstractBooleanAssert<?> boolAssert, boolean other) {
      return boolAssert.isNotEqualTo(other);
    }
  }

  static final class AssertThatBooleanIsTrue<E> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(AbstractBooleanAssert<?> boolAssert) {
      return Refaster.anyOf(
          boolAssert.isEqualTo(true),
          boolAssert.isEqualTo(Boolean.TRUE),
          boolAssert.isNotEqualTo(false),
          boolAssert.isNotEqualTo(Boolean.FALSE));
    }

    @AfterTemplate
    AbstractBooleanAssert<?> after(AbstractBooleanAssert<?> boolAssert) {
      return boolAssert.isTrue();
    }
  }

  static final class AssertThatBooleanIsFalse<E> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(AbstractBooleanAssert<?> boolAssert) {
      return Refaster.anyOf(
          boolAssert.isEqualTo(false),
          boolAssert.isEqualTo(Boolean.FALSE),
          boolAssert.isNotEqualTo(true),
          boolAssert.isNotEqualTo(Boolean.TRUE));
    }

    @AfterTemplate
    AbstractBooleanAssert<?> after(AbstractBooleanAssert<?> boolAssert) {
      return boolAssert.isFalse();
    }
  }

  //
  // Integer
  //

  static final class AssertThatIntegerIsEqualTo<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert, int n) {
      return intAssert.isEqualTo(Integer.valueOf(n));
    }

    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert, long n) {
      return intAssert.isEqualTo(Long.valueOf(n));
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert, int n) {
      return intAssert.isEqualTo(n);
    }
  }

  static final class AssertThatIntegerIsNotEqualTo<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert, int n) {
      return intAssert.isNotEqualTo(Integer.valueOf(n));
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert, int n) {
      return intAssert.isNotEqualTo(n);
    }
  }

  static final class AssertThatIntegerIsZero<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isEqualTo(0);
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isZero();
    }
  }

  static final class AssertThatIntegerIsNotZero<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isNotEqualTo(0);
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isNotZero();
    }
  }

  static final class AssertThatIntegerIsPositive<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert) {
      return Refaster.anyOf(intAssert.isGreaterThan(0), intAssert.isGreaterThanOrEqualTo(1));
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isPositive();
    }
  }

  static final class AssertThatIntegerIsNotPositive<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert) {
      return Refaster.anyOf(intAssert.isLessThanOrEqualTo(0), intAssert.isLessThan(1));
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isNotPositive();
    }
  }

  static final class AssertThatIntegerIsNegative<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert) {
      return Refaster.anyOf(intAssert.isLessThan(0), intAssert.isLessThanOrEqualTo(-1));
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isNegative();
    }
  }

  static final class AssertThatIntegerIsNotNegative<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert) {
      return Refaster.anyOf(intAssert.isGreaterThanOrEqualTo(0), intAssert.isGreaterThan(-1));
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isNotNegative();
    }
  }

  //
  // Float
  //

  static final class AssertThatFloatIsEqualTo<E> {
    @BeforeTemplate
    AbstractFloatAssert<?> before(AbstractFloatAssert<?> floatAssert, float n) {
      return Refaster.anyOf(
          floatAssert.isCloseTo(n, offset(0F)),
          floatAssert.isCloseTo(Float.valueOf(n), offset(0F)),
          floatAssert.isCloseTo(n, withPercentage(0F)),
          floatAssert.isCloseTo(Float.valueOf(n), withPercentage(0F)),
          floatAssert.isEqualTo(n, offset(0F)),
          floatAssert.isEqualTo(Float.valueOf(n)),
          floatAssert.isEqualTo(Float.valueOf(n), offset(0F)));
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(AbstractFloatAssert<?> floatAssert, float n) {
      return floatAssert.isEqualTo(n);
    }
  }

  //
  // Double
  //

  static final class AssertThatDoubleIsEqualTo<E> {
    @BeforeTemplate
    AbstractDoubleAssert<?> before(AbstractDoubleAssert<?> doubleAssert, double n) {
      return Refaster.anyOf(
          doubleAssert.isCloseTo(n, offset(0.0)),
          doubleAssert.isCloseTo(Double.valueOf(n), offset(0.0)),
          doubleAssert.isCloseTo(n, withPercentage(0.0)),
          doubleAssert.isCloseTo(Double.valueOf(n), withPercentage(0.0)),
          doubleAssert.isEqualTo(n, offset(0.0)),
          doubleAssert.isEqualTo(Double.valueOf(n)),
          doubleAssert.isEqualTo(Double.valueOf(n), offset(0.0)));
    }

    @AfterTemplate
    AbstractDoubleAssert<?> after(AbstractDoubleAssert<?> doubleAssert, double n) {
      return doubleAssert.isEqualTo(n);
    }
  }

  /// XXX: Above this line: context-independent rewrite rules. Should be applied first.
  // XXX: Below this line: context-dependent rewrite rules.

  //
  // Object
  //

  static final class AssertThatIsInstanceOf<S, T> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(S object) {
      return assertThat(Refaster.<T>isInstance(object)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ObjectAssert<S> after(S object) {
      return assertThat(object).isInstanceOf(Refaster.<T>clazz());
    }
  }

  static final class AssertThatIsNotInstanceOf<S, T> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(S object) {
      return assertThat(Refaster.<T>isInstance(object)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ObjectAssert<S> after(S object) {
      return assertThat(object).isNotInstanceOf(Refaster.<T>clazz());
    }
  }

  //
  // String
  //

  static final class AssertThatStringIsEmpty {
    @BeforeTemplate
    void before(String string) {
      Refaster.anyOf(
          assertThat(string).isEqualTo(""),
          assertThat(string).hasSize(0),
          assertThat(string).hasSizeLessThan(1),
          assertThat(string.isEmpty()).isTrue(),
          assertThat(string.length()).isZero(),
          assertThat(string.length()).isNotPositive());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(String string) {
      assertThat(string).isEmpty();
    }
  }

  static final class AssertThatStringIsNotEmpty {
    @BeforeTemplate
    AbstractAssert<?, ?> before(String string) {
      return Refaster.anyOf(
          assertThat(string).isNotEqualTo(""),
          assertThat(string).hasSizeGreaterThan(0),
          assertThat(string.isEmpty()).isFalse(),
          assertThat(string.length()).isNotZero(),
          assertThat(string.length()).isPositive());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String string) {
      return assertThat(string).isNotEmpty();
    }
  }

  static final class AssertThatStringHasLength {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(String string, int length) {
      return assertThat(string.length()).isEqualTo(length);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String string, int length) {
      return assertThat(string).hasSize(length);
    }
  }

  //
  // Optional
  //

  static final class AssertThatOptional<T> {
    @BeforeTemplate
    ObjectAssert<T> before(Optional<T> optional) {
      return assertThat(optional.get());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, T> after(Optional<T> optional) {
      return assertThat(optional).get();
    }
  }

  static final class AssertThatOptionalIsPresent<T> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Optional<T> optional) {
      return Refaster.anyOf(
          assertThat(optional.isPresent()).isTrue(),
          assertThat(optional.isEmpty()).isFalse(),
          assertThat(optional).isNotEqualTo(Optional.empty()));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    OptionalAssert<T> after(Optional<T> optional) {
      return assertThat(optional).isPresent();
    }
  }

  static final class AssertThatOptionalIsEmpty<T> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Optional<T> optional) {
      return Refaster.anyOf(
          assertThat(optional.isEmpty()).isTrue(),
          assertThat(optional.isPresent()).isFalse(),
          assertThat(optional).isEqualTo(Optional.empty()));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    OptionalAssert<T> after(Optional<T> optional) {
      return assertThat(optional).isEmpty();
    }
  }

  static final class AssertThatOptionalHasValue<T> {
    @BeforeTemplate
    OptionalAssert<T> before(Optional<T> optional, T value) {
      return assertThat(optional).isEqualTo(Optional.of(value));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    OptionalAssert<T> after(Optional<T> optional, T value) {
      return assertThat(optional).hasValue(value);
    }
  }

  static final class AssertThatOptionalHasValueMatching<T> {
    @BeforeTemplate
    OptionalAssert<T> before(Optional<T> optional, Predicate<? super T> predicate) {
      return assertThat(optional.filter(predicate)).isPresent();
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, T> after(Optional<T> optional, Predicate<? super T> predicate) {
      return assertThat(optional).get().matches(predicate);
    }
  }

  //
  // OptionalDouble
  //

  // XXX: There are several other variations that can also be optimized so as to avoid
  // unconditionally calling `getAsDouble`.
  static final class AssertThatOptionalDouble {
    @BeforeTemplate
    AbstractDoubleAssert<?> before(OptionalDouble optional, double expected) {
      return assertThat(optional.getAsDouble()).isEqualTo(expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    OptionalDoubleAssert after(OptionalDouble optional, double expected) {
      return assertThat(optional).hasValue(expected);
    }
  }

  //
  // OptionalInt
  //

  // XXX: There are several other variations that can also be optimized so as to avoid
  // unconditionally calling `getAsInt`.
  static final class AssertThatOptionalInt {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(OptionalInt optional, int expected) {
      return assertThat(optional.getAsInt()).isEqualTo(expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    OptionalIntAssert after(OptionalInt optional, int expected) {
      return assertThat(optional).hasValue(expected);
    }
  }

  //
  // OptionalLong
  //

  // XXX: There are several other variations that can also be optimized so as to avoid
  // unconditionally calling `getAsLong`.
  static final class AssertThatOptionalLong {
    @BeforeTemplate
    AbstractLongAssert<?> before(OptionalLong optional, long expected) {
      return assertThat(optional.getAsLong()).isEqualTo(expected);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    OptionalLongAssert after(OptionalLong optional, long expected) {
      return assertThat(optional).hasValue(expected);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////
  // To be organized.

  //
  // List
  //

  static final class AssertThatListsAreEqual<S, T extends S> {
    @BeforeTemplate
    ListAssert<S> before(List<S> list1, List<T> list2) {
      return assertThat(list1).isEqualTo(list2);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(List<S> list1, List<T> list2) {
      return assertThat(list1).containsExactlyElementsOf(list2);
    }
  }

  //
  // Set
  //

  static final class AssertThatSetsAreEqual<S, T extends S> {
    @BeforeTemplate
    IterableAssert<S> before(Set<S> set1, Set<T> set2) {
      return assertThat(set1).isEqualTo(set2);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    IterableAssert<S> after(Set<S> set1, Set<T> set2) {
      return assertThat(set1).containsExactlyInAnyOrderElementsOf(set2);
    }
  }

  //
  // Mutliset
  //

  static final class AssertThatMultisetsAreEqual<S, T extends S> {
    @BeforeTemplate
    IterableAssert<S> before(Multiset<S> multiset1, Multiset<T> multiset2) {
      return assertThat(multiset1).isEqualTo(multiset2);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    IterableAssert<S> after(Multiset<S> multiset1, Multiset<T> multiset2) {
      return assertThat(multiset1).containsExactlyInAnyOrderElementsOf(multiset2);
    }
  }

  //
  // Iterable
  //

  static final class AssertThatIterableHasSameElementsAsSet<S, T extends S> {
    @BeforeTemplate
    IterableAssert<S> before(Iterable<S> set1, Set<T> set2) {
      return assertThat(set1).containsExactlyInAnyOrderElementsOf(set2);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    IterableAssert<S> after(Iterable<S> set1, Set<T> set2) {
      return assertThat(set1).hasSameElementsAs(set2);
    }
  }

  // XXX: Most/all of these Iterable rules can also be applied to arrays.
  // XXX: Elsewhere add a rule to disallow `Collection.emptyList()` and variants as well as
  // `Arrays.asList()`.
  // XXX: We could also match against `#hasSameSizeAs` and `#isSubsetOf`, but that's pushing it.
  // Let's add those once Refaster supports a way of deduplicating repetition.
  static final class AssertThatIterableIsEmpty<E> {
    @BeforeTemplate
    void before(Iterable<E> iterable) {
      Refaster.anyOf(
          assertThat(iterable)
              .containsExactlyElementsOf(
                  Refaster.anyOf(
                      ImmutableList.of(),
                      new ArrayList<>(),
                      ImmutableSet.of(),
                      new HashSet<>(),
                      new LinkedHashSet<>(),
                      ImmutableSortedSet.of(),
                      new TreeSet<>(),
                      ImmutableMultiset.of(),
                      ImmutableSortedMultiset.of())),
          assertThat(iterable)
              .containsExactlyInAnyOrderElementsOf(
                  Refaster.anyOf(
                      ImmutableList.of(),
                      new ArrayList<>(),
                      ImmutableSet.of(),
                      new HashSet<>(),
                      new LinkedHashSet<>(),
                      ImmutableSortedSet.of(),
                      new TreeSet<>(),
                      ImmutableMultiset.of(),
                      ImmutableSortedMultiset.of())),
          assertThat(iterable)
              .containsOnlyElementsOf(
                  Refaster.anyOf(
                      ImmutableList.of(),
                      new ArrayList<>(),
                      ImmutableSet.of(),
                      new HashSet<>(),
                      new LinkedHashSet<>(),
                      ImmutableSortedSet.of(),
                      new TreeSet<>(),
                      ImmutableMultiset.of(),
                      ImmutableSortedMultiset.of())),
          assertThat(iterable)
              .hasSameElementsAs(
                  Refaster.anyOf(
                      ImmutableList.of(),
                      new ArrayList<>(),
                      ImmutableSet.of(),
                      new HashSet<>(),
                      new LinkedHashSet<>(),
                      ImmutableSortedSet.of(),
                      new TreeSet<>(),
                      ImmutableMultiset.of(),
                      ImmutableSortedMultiset.of())),
          assertThat(iterable).hasSize(0),
          assertThat(iterable).hasSizeLessThan(1),
          assertThat(iterable).containsExactly(),
          assertThat(iterable).containsExactlyInAnyOrder(),
          assertThat(iterable).containsOnly(),
          assertThat(iterable).isSubsetOf(),
          assertThat(Iterables.isEmpty(iterable)).isTrue(),
          assertThat(iterable.iterator().hasNext()).isFalse(),
          assertThat(Iterables.size(iterable)).isZero(),
          assertThat(Iterables.size(iterable)).isNotPositive());
    }

    @BeforeTemplate
    void before(Collection<E> iterable) {
      Refaster.anyOf(
          assertThat(iterable.isEmpty()).isTrue(),
          assertThat(iterable.size()).isZero(),
          assertThat(iterable.size()).isNotPositive());
    }

    @BeforeTemplate
    void before(List<E> iterable) {
      Refaster.anyOf(
          assertThat(iterable).isEqualTo(Refaster.anyOf(ImmutableList.of(), new ArrayList<>())),
          assertThat(iterable).hasSize(0),
          assertThat(iterable).hasSizeLessThan(1));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Collection<E> iterable) {
      assertThat(iterable).isEmpty();
    }
  }

  static final class AssertThatIterableIsNotEmpty<E> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Iterable<E> iterable) {
      return Refaster.anyOf(
          assertThat(iterable)
              .isNotEqualTo(
                  Refaster.anyOf(
                      ImmutableList.of(),
                      new ArrayList<>(),
                      ImmutableSet.of(),
                      new HashSet<>(),
                      new LinkedHashSet<>(),
                      ImmutableSortedSet.of(),
                      new TreeSet<>(),
                      ImmutableMultiset.of(),
                      ImmutableSortedMultiset.of())),
          assertThat(iterable).hasSizeGreaterThan(0),
          assertThat(iterable).hasSizeGreaterThanOrEqualTo(1),
          assertThat(Iterables.isEmpty(iterable)).isFalse(),
          assertThat(iterable.iterator().hasNext()).isTrue(),
          assertThat(Iterables.size(iterable)).isNotZero(),
          assertThat(Iterables.size(iterable)).isPositive());
    }

    @BeforeTemplate
    AbstractAssert<?, ?> before(Collection<E> iterable) {
      return Refaster.anyOf(
          assertThat(iterable.isEmpty()).isFalse(),
          assertThat(iterable.size()).isNotZero(),
          assertThat(iterable.size()).isPositive());
    }

    @BeforeTemplate
    ListAssert<E> before(List<E> iterable) {
      return Refaster.anyOf(
          assertThat(iterable).isNotEqualTo(Refaster.anyOf(ImmutableList.of(), new ArrayList<>())),
          assertThat(iterable).hasSizeGreaterThan(0),
          assertThat(iterable).hasSizeGreaterThanOrEqualTo(1));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> iterable) {
      return assertThat(iterable).isNotEmpty();
    }
  }

  static final class AssertThatIterableHasSize<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(Iterable<E> iterable, int length) {
      return assertThat(Iterables.size(iterable)).isEqualTo(length);
    }

    @BeforeTemplate
    AbstractIntegerAssert<?> before(Collection<E> iterable, int length) {
      return assertThat(iterable.size()).isEqualTo(length);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> iterable, int length) {
      return assertThat(iterable).hasSize(length);
    }
  }

  static final class AssertThatIterablesHaveSameSize<S, T> {
    @BeforeTemplate
    IterableAssert<S> before(Iterable<S> iterable1, Iterable<T> iterable2) {
      return assertThat(iterable1).hasSize(Iterables.size(iterable2));
    }

    @BeforeTemplate
    IterableAssert<S> before(Iterable<S> iterable1, Collection<T> iterable2) {
      return assertThat(iterable1).hasSize(iterable2.size());
    }

    @BeforeTemplate
    ListAssert<S> before(List<S> iterable1, Iterable<T> iterable2) {
      return assertThat(iterable1).hasSize(Iterables.size(iterable2));
    }

    @BeforeTemplate
    ListAssert<S> before(List<S> iterable1, Collection<T> iterable2) {
      return assertThat(iterable1).hasSize(iterable2.size());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    IterableAssert<S> after(Iterable<S> iterable1, Iterable<T> iterable2) {
      return assertThat(iterable1).hasSameSizeAs(iterable2);
    }
  }

  //    // XXX: Add a variant which checks the exact size.
  //    static final class AssertThatMapIsEmpty<K, V> {
  //        @BeforeTemplate
  //        void before(Map<K, V> map) {
  //            Refaster.anyOf(
  //                    assertThat(map)
  //                            .isEqualTo(
  //                                    Refaster.anyOf(
  //                                            ImmutableMap.of(),
  //                                            ImmutableBiMap.of(),
  //                                            ImmutableSortedMap.of(),
  //                                            new HashMap<>(),
  //                                            new LinkedHashMap<>(),
  //                                            new TreeMap<>())),
  //                    assertThat(map).hasSize(0),
  //                    assertThat(Refaster.anyOf(map.keySet(), map.values())).hasSize(0),
  //                    assertThat(map).hasSizeLessThan(1),
  //                    assertThat(Refaster.anyOf(map.keySet(), map.values())).hasSizeLessThan(1),
  //                    assertThat(map.isEmpty()).isTrue(),
  //                    assertThat(Refaster.anyOf(map.keySet(), map.values()).isEmpty()).isTrue(),
  //                    assertThat(Refaster.anyOf(map.size(), map.keySet().size(),
  // map.values().size()))
  //                            .isZero(),
  //                    assertThat(Refaster.anyOf(map.size(), map.keySet().size(),
  // map.values().size()))
  //                            .isNotPositive());
  //        }
  //
  //        @BeforeTemplate
  //        void before2(Map<K, V> map) {
  //            assertThat(Refaster.anyOf(map.keySet(), map.values())).isEmpty();
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        void after(Map<K, V> map) {
  //            assertThat(map).isEmpty();
  //        }
  //    }
  //
  //    static final class AssertThatMapIsNotEmpty<K, V> {
  //        @BeforeTemplate
  //        AbstractAssert<?, ?> before(Map<K, V> map) {
  //            return Refaster.anyOf(
  //                    assertThat(map)
  //                            .isNotEqualTo(
  //                                    Refaster.anyOf(
  //                                            ImmutableMap.of(),
  //                                            ImmutableBiMap.of(),
  //                                            ImmutableSortedMap.of(),
  //                                            new HashMap<>(),
  //                                            new LinkedHashMap<>(),
  //                                            new TreeMap<>())),
  //                    assertThat(map).hasSizeGreaterThan(0),
  //                    assertThat(Refaster.anyOf(map.keySet(),
  // map.values())).hasSizeGreaterThan(0),
  //                    assertThat(map.isEmpty()).isFalse(),
  //                    assertThat(Refaster.anyOf(map.keySet(), map.values()).isEmpty()).isFalse(),
  //                    assertThat(Refaster.anyOf(map.size(), map.keySet().size(),
  // map.values().size()))
  //                            .isNotZero(),
  //                    assertThat(Refaster.anyOf(map.size(), map.keySet().size(),
  // map.values().size()))
  //                            .isPositive());
  //        }
  //
  //        @BeforeTemplate
  //        IterableAssert<?> before2(Map<K, V> map) {
  //            return assertThat(Refaster.anyOf(map.keySet(), map.values())).isNotEmpty();
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        MapAssert<K, V> after(Map<K, V> map) {
  //            return assertThat(map).isNotEmpty();
  //        }
  //    }
  //
  //    static final class AssertThatMapHasSize<K, V> {
  //        @BeforeTemplate
  //        AbstractIntegerAssert<?> before(Map<K, V> map, int length) {
  //            return assertThat(Refaster.anyOf(map.size(), map.keySet().size(),
  // map.values().size()))
  //                    .isEqualTo(length);
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        MapAssert<K, V> after(Map<K, V> map, int length) {
  //            return assertThat(map).hasSize(length);
  //        }
  //    }
  //
  //    static final class AssertThatMapsHaveSameSize<K, V> {
  //        @BeforeTemplate
  //        AbstractAssert<?, ?> before(Map<K, V> map1, Map<K, V> map2) {
  //            return Refaster.anyOf(
  //                    assertThat(map1)
  //                            .hasSize(
  //                                    Refaster.anyOf(
  //                                            map2.size(),
  //                                            map2.keySet().size(),
  //                                            map2.values().size())),
  //                    assertThat(Refaster.anyOf(map1.keySet(), map1.values()))
  //                            .hasSize(
  //                                    Refaster.anyOf(
  //                                            map2.size(),
  //                                            map2.keySet().size(),
  //                                            map2.values().size())));
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        MapAssert<K, V> after(Map<K, V> map1, Map<K, V> map2) {
  //            return assertThat(map1).hasSameSizeAs(map2);
  //        }
  //    }
  //
  //    // XXX: Should also add a rule (elsewhere) to simplify `map.keySet().contains(key)`.
  //    static final class AssertThatMapContainsKey<K, V> {
  //        @BeforeTemplate
  //        AbstractBooleanAssert<?> before(Map<K, V> map, K key) {
  //            return assertThat(map.containsKey(key)).isTrue();
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        MapAssert<K, V> after(Map<K, V> map, K key) {
  //            return assertThat(map).containsKey(key);
  //        }
  //    }
  //
  //    static final class AssertThatMapDoesNotContainKey<K, V> {
  //        @BeforeTemplate
  //        AbstractBooleanAssert<?> before(Map<K, V> map, K key) {
  //            return assertThat(map.containsKey(key)).isFalse();
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        MapAssert<K, V> after(Map<K, V> map, K key) {
  //            return assertThat(map).doesNotContainKey(key);
  //        }
  //    }
  //
  //    // XXX: There's a bunch of variations on this theme.
  //    // XXX: The `Iterables.getOnlyElement` variant doesn't match in
  //    // `analytics/analytics-message-listener`. Why?
  //    // XXX: Here and elsewhere: make sure `Arrays.asList` is migrated away from, then drop it
  // here.
  //    static final class AssertThatOnlyElementIsEqualTo<E> {
  //        @BeforeTemplate
  //        AbstractAssert<?, ?> before(Iterable<E> iterable, E expected) {
  //            return Refaster.anyOf(
  //                    assertThat(Iterables.getOnlyElement(iterable)).isEqualTo(expected),
  //                    assertThat(iterable)
  //                            .isEqualTo(
  //                                    Refaster.anyOf(
  //                                            ImmutableList.of(expected),
  //                                            Arrays.asList(expected),
  //                                            ImmutableSet.of(expected),
  //                                            ImmutableMultiset.of(expected))));
  //        }
  //
  //        @BeforeTemplate
  //        AbstractAssert<?, ?> before(List<E> iterable, E expected) {
  //            return assertThat(iterable)
  //                    .isEqualTo(Refaster.anyOf(ImmutableList.of(expected),
  // Arrays.asList(expected)));
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        IterableAssert<E> after(Iterable<E> iterable, E expected) {
  //            return assertThat(iterable).containsExactly(expected);
  //        }
  //    }
  //
  //    static final class AssertThatOnlyComparableElementIsEqualTo<E extends Comparable<? super E>>
  // {
  //        @BeforeTemplate
  //        AbstractAssert<?, ?> before(Iterable<E> iterable, E expected) {
  //            return assertThat(iterable)
  //                    .isEqualTo(
  //                            Refaster.<Object>anyOf(
  //                                    ImmutableSortedSet.of(expected),
  //                                    ImmutableSortedMultiset.of(expected)));
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        IterableAssert<E> after(Iterable<E> iterable, E expected) {
  //            return assertThat(iterable).containsExactly(expected);
  //        }
  //    }
  //
  //    static final class AssertThatIterableContainsTwoSpecificElementsInOrder<E> {
  //        @BeforeTemplate
  //        IterableAssert<E> before(Iterable<E> iterable, E e1, E e2) {
  //            return assertThat(iterable)
  //                    .isEqualTo(Refaster.anyOf(ImmutableList.of(e1, e2), Arrays.asList(e1, e2)));
  //        }
  //
  //        @BeforeTemplate
  //        ListAssert<E> before(List<E> iterable, E e1, E e2) {
  //            return assertThat(iterable)
  //                    .isEqualTo(Refaster.anyOf(ImmutableList.of(e1, e2), Arrays.asList(e1, e2)));
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        IterableAssert<E> after(Iterable<E> iterable, E e1, E e2) {
  //            return assertThat(iterable).containsExactly(e1, e2);
  //        }
  //    }
  //
  //    static final class AssertThatIterableContainsThreeSpecificElementsInOrder<E> {
  //        @BeforeTemplate
  //        IterableAssert<E> before(Iterable<E> iterable, E e1, E e2, E e3) {
  //            return assertThat(iterable)
  //                    .isEqualTo(
  //                            Refaster.anyOf(
  //                                    ImmutableList.of(e1, e2, e3), Arrays.asList(e1, e2, e3)));
  //        }
  //
  //        @BeforeTemplate
  //        ListAssert<E> before(List<E> iterable, E e1, E e2, E e3) {
  //            return assertThat(iterable)
  //                    .isEqualTo(
  //                            Refaster.anyOf(
  //                                    ImmutableList.of(e1, e2, e3), Arrays.asList(e1, e2, e3)));
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        IterableAssert<E> after(Iterable<E> iterable, E e1, E e2, E e3) {
  //            return assertThat(iterable).containsExactly(e1, e2, e3);
  //        }
  //    }
  //
  //    static final class AssertThatIterableContainsFourSpecificElementsInOrder<E> {
  //        @BeforeTemplate
  //        IterableAssert<E> before(Iterable<E> iterable, E e1, E e2, E e3, E e4) {
  //            return assertThat(iterable)
  //                    .isEqualTo(
  //                            Refaster.anyOf(
  //                                    ImmutableList.of(e1, e2, e3, e4),
  //                                    Arrays.asList(e1, e2, e3, e4)));
  //        }
  //
  //        @BeforeTemplate
  //        ListAssert<E> before(List<E> iterable, E e1, E e2, E e3, E e4) {
  //            return assertThat(iterable)
  //                    .isEqualTo(
  //                            Refaster.anyOf(
  //                                    ImmutableList.of(e1, e2, e3, e4),
  //                                    Arrays.asList(e1, e2, e3, e4)));
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        IterableAssert<E> after(Iterable<E> iterable, E e1, E e2, E e3, E e4) {
  //            return assertThat(iterable).containsExactly(e1, e2, e3, e4);
  //        }
  //    }
  //
  //    // XXX: Up to 12...? :)
  //    static final class AssertThatIterableContainsFiveSpecificElementsInOrder<E> {
  //        @BeforeTemplate
  //        IterableAssert<E> before(Iterable<E> iterable, E e1, E e2, E e3, E e4, E e5) {
  //            return assertThat(iterable)
  //                    .isEqualTo(
  //                            Refaster.anyOf(
  //                                    ImmutableList.of(e1, e2, e3, e4, e5),
  //                                    Arrays.asList(e1, e2, e3, e4, e5)));
  //        }
  //
  //        @BeforeTemplate
  //        ListAssert<E> before(List<E> iterable, E e1, E e2, E e3, E e4, E e5) {
  //            return assertThat(iterable)
  //                    .isEqualTo(
  //                            Refaster.anyOf(
  //                                    ImmutableList.of(e1, e2, e3, e4, e5),
  //                                    Arrays.asList(e1, e2, e3, e4, e5)));
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        IterableAssert<E> after(Iterable<E> iterable, E e1, E e2, E e3, E e4, E e5) {
  //            return assertThat(iterable).containsExactly(e1, e2, e3, e4, e5);
  //        }
  //    }
  //
  //    // XXX: For this and other variants we could also match other behavior-preserving collection
  //    // operations.
  //    static final class AssertThatStreamContainsTwoSpecificElementsInOrder<E> {
  //        @BeforeTemplate
  //        ListAssert<E> before(Stream<E> stream, E e1, E e2) {
  //            return Refaster.anyOf(
  //                    assertThat(stream.collect(toImmutableList()))
  //                            .isEqualTo(
  //                                    Refaster.anyOf(
  //                                            ImmutableList.of(e1, e2), Arrays.asList(e1, e2))),
  //                    assertThat(stream.collect(toImmutableList())).containsExactly(e1, e2));
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        ListAssert<E> after(Stream<E> stream, E e1, E e2) {
  //            return assertThat(stream).containsExactly(e1, e2);
  //        }
  //    }
  //
  //    static final class AssertThatStreamContainsThreeSpecificElementsInOrder<E> {
  //        @BeforeTemplate
  //        ListAssert<E> before(Stream<E> stream, E e1, E e2, E e3) {
  //            return Refaster.anyOf(
  //                    assertThat(stream.collect(toImmutableList()))
  //                            .isEqualTo(
  //                                    Refaster.anyOf(
  //                                            ImmutableList.of(e1, e2, e3),
  //                                            Arrays.asList(e1, e2, e3))),
  //                    assertThat(stream.collect(toImmutableList())).containsExactly(e1, e2, e3));
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        ListAssert<E> after(Stream<E> stream, E e1, E e2, E e3) {
  //            return assertThat(stream).containsExactly(e1, e2, e3);
  //        }
  //    }
  //
  //    static final class AssertThatStreamContainsFourSpecificElementsInOrder<E> {
  //        @BeforeTemplate
  //        ListAssert<E> before(Stream<E> stream, E e1, E e2, E e3, E e4) {
  //            return Refaster.anyOf(
  //                    assertThat(stream.collect(toImmutableList()))
  //                            .isEqualTo(
  //                                    Refaster.anyOf(
  //                                            ImmutableList.of(e1, e2, e3, e4),
  //                                            Arrays.asList(e1, e2, e3, e4))),
  //                    assertThat(stream.collect(toImmutableList())).containsExactly(e1, e2, e3,
  // e4));
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        ListAssert<E> after(Stream<E> stream, E e1, E e2, E e3, E e4) {
  //            return assertThat(stream).containsExactly(e1, e2, e3, e4);
  //        }
  //    }
  //
  //    // XXX: Up to 12...? :)
  //    static final class AssertThatStreamContainsFiveSpecificElementsInOrder<E> {
  //        @BeforeTemplate
  //        ListAssert<E> before(Stream<E> stream, E e1, E e2, E e3, E e4, E e5) {
  //            return Refaster.anyOf(
  //                    assertThat(stream.collect(toImmutableList()))
  //                            .isEqualTo(
  //                                    Refaster.anyOf(
  //                                            ImmutableList.of(e1, e2, e3, e4, e5),
  //                                            Arrays.asList(e1, e2, e3, e4, e5))),
  //                    assertThat(stream.collect(toImmutableList()))
  //                            .containsExactly(e1, e2, e3, e4, e5));
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        ListAssert<E> after(Stream<E> stream, E e1, E e2, E e3, E e4, E e5) {
  //            return assertThat(stream).containsExactly(e1, e2, e3, e4, e5);
  //        }
  //    }
  //
  //    static final class AssertThatIterableContainsTwoSpecificElements<E> {
  //        @BeforeTemplate
  //        IterableAssert<E> before(Iterable<E> iterable, E e1, E e2) {
  //            return assertThat(iterable)
  //                    .isEqualTo(
  //                            Refaster.anyOf(ImmutableSet.of(e1, e2), ImmutableMultiset.of(e1,
  // e2)));
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        IterableAssert<E> after(Iterable<E> iterable, E e1, E e2) {
  //            return assertThat(iterable).containsExactlyInAnyOrder(e1, e2);
  //        }
  //    }
  //
  //    static final class AssertThatIterableContainsThreeSpecificElements<E> {
  //        @BeforeTemplate
  //        IterableAssert<E> before(Iterable<E> iterable, E e1, E e2, E e3) {
  //            return assertThat(iterable)
  //                    .isEqualTo(
  //                            Refaster.anyOf(
  //                                    ImmutableSet.of(e1, e2, e3), ImmutableMultiset.of(e1, e2,
  // e3)));
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        IterableAssert<E> after(Iterable<E> iterable, E e1, E e2, E e3) {
  //            return assertThat(iterable).containsExactlyInAnyOrder(e1, e2, e3);
  //        }
  //    }
  //
  //    static final class AssertThatIterableContainsFourSpecificElements<E> {
  //        @BeforeTemplate
  //        IterableAssert<E> before(Iterable<E> iterable, E e1, E e2, E e3, E e4) {
  //            return assertThat(iterable)
  //                    .isEqualTo(
  //                            Refaster.anyOf(
  //                                    ImmutableSet.of(e1, e2, e3, e4),
  //                                    ImmutableMultiset.of(e1, e2, e3, e4)));
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        IterableAssert<E> after(Iterable<E> iterable, E e1, E e2, E e3, E e4) {
  //            return assertThat(iterable).containsExactlyInAnyOrder(e1, e2, e3, e4);
  //        }
  //    }
  //
  //    // XXX: Up to 12...? :)
  //    static final class AssertThatIterableContainsFiveSpecificElements<E> {
  //        @BeforeTemplate
  //        IterableAssert<E> before(Iterable<E> iterable, E e1, E e2, E e3, E e4, E e5) {
  //            return assertThat(iterable)
  //                    .isEqualTo(
  //                            Refaster.anyOf(
  //                                    ImmutableSet.of(e1, e2, e3, e4, e5),
  //                                    ImmutableMultiset.of(e1, e2, e3, e4, e5)));
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        IterableAssert<E> after(Iterable<E> iterable, E e1, E e2, E e3, E e4, E e5) {
  //            return assertThat(iterable).containsExactlyInAnyOrder(e1, e2, e3, e4, e5);
  //        }
  //    }
  //
  //    static final class AssertThatStreamContainsTwoSpecificElements<E> {
  //        @BeforeTemplate
  //        IterableAssert<E> before(Stream<E> stream, E e1, E e2) {
  //            return assertThat(stream.collect(toImmutableSet())).isEqualTo(ImmutableSet.of(e1,
  // e2));
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        ListAssert<E> after(Stream<E> stream, E e1, E e2) {
  //            return assertThat(stream).containsOnly(e1, e2);
  //        }
  //    }
  //
  //    static final class AssertThatStreamContainsThreeSpecificElements<E> {
  //        @BeforeTemplate
  //        IterableAssert<E> before(Stream<E> stream, E e1, E e2, E e3) {
  //            return assertThat(stream.collect(toImmutableSet()))
  //                    .isEqualTo(ImmutableSet.of(e1, e2, e3));
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        ListAssert<E> after(Stream<E> stream, E e1, E e2, E e3) {
  //            return assertThat(stream).containsOnly(e1, e2, e3);
  //        }
  //    }
  //
  //    // XXX: Up to 12...? :)
  //    static final class AssertThatStreamContainsFiveSpecificElements<E> {
  //        @BeforeTemplate
  //        IterableAssert<E> before(Stream<E> stream, E e1, E e2, E e3, E e4, E e5) {
  //            return assertThat(stream.collect(toImmutableSet()))
  //                    .isEqualTo(ImmutableSet.of(e1, e2, e3, e4, e5));
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
  //        ListAssert<E> after(Stream<E> stream, E e1, E e2, E e3, E e4, E e5) {
  //            return assertThat(stream).containsOnly(e1, e2, e3, e4, e5);
  //        }
  //    }
}
