package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Matches;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Collection;
import java.util.Map;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractCollectionAssert;
import org.assertj.core.api.AbstractMapAssert;
import org.assertj.core.api.MapAssert;
import org.assertj.core.api.ObjectAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;
import tech.picnic.errorprone.refaster.matchers.IsEmpty;

/** Refaster rules related to AssertJ assertions over {@link Map}s. */
@OnlineDocumentation
final class AssertJMapRules {
  private AssertJMapRules() {}

  /** Prefer {@link AbstractMapAssert#isEmpty()} over more contrived alternatives. */
  static final class AbstractMapAssertIsEmpty<K, V, M extends K, N extends V, T extends K> {
    @BeforeTemplate
    void before(
        AbstractMapAssert<?, ?, K, V> mapAssert,
        @Matches(IsEmpty.class) Map<M, N> emptyMap,
        @Matches(IsEmpty.class) Map<?, ?> emptyOther,
        @Matches(IsEmpty.class) Iterable<T> emptyKeys) {
      Refaster.anyOf(
          mapAssert.containsExactlyEntriesOf(emptyMap),
          mapAssert.containsExactlyInAnyOrderEntriesOf(emptyMap),
          mapAssert.hasSameSizeAs(emptyOther),
          mapAssert.isEqualTo(emptyOther),
          mapAssert.containsOnlyKeys(emptyKeys),
          mapAssert.containsExactly(),
          mapAssert.containsOnly(),
          mapAssert.containsOnlyKeys());
    }

    @AfterTemplate
    void after(AbstractMapAssert<?, ?, K, V> mapAssert) {
      mapAssert.isEmpty();
    }
  }

  /** Prefer {@code assertThat(map).isEmpty()} over more contrived alternatives. */
  static final class AssertThatIsEmpty<K, V> {
    @BeforeTemplate
    void before(Map<K, V> actual) {
      Refaster.anyOf(
          assertThat(actual).hasSize(0),
          assertThat(actual.isEmpty()).isTrue(),
          assertThat(actual.size()).isEqualTo(0L),
          assertThat(actual.size()).isNotPositive());
    }

    @BeforeTemplate
    void before2(Map<K, V> actual) {
      assertThat(Refaster.anyOf(actual.keySet(), actual.values(), actual.entrySet())).isEmpty();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Map<K, V> actual) {
      assertThat(actual).isEmpty();
    }
  }

  /** Prefer {@link AbstractMapAssert#isNotEmpty()} over more contrived alternatives. */
  static final class AbstractMapAssertIsNotEmpty<K, V> {
    @BeforeTemplate
    AbstractMapAssert<?, ?, K, V> before(
        AbstractMapAssert<?, ?, K, V> mapAssert, @Matches(IsEmpty.class) Map<?, ?> emptyOther) {
      return mapAssert.isNotEqualTo(emptyOther);
    }

    @AfterTemplate
    AbstractMapAssert<?, ?, K, V> after(AbstractMapAssert<?, ?, K, V> mapAssert) {
      return mapAssert.isNotEmpty();
    }
  }

  /** Prefer {@code assertThat(map).isNotEmpty()} over more contrived alternatives. */
  static final class AssertThatIsNotEmpty<K, V> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Map<K, V> actual) {
      return Refaster.anyOf(
          assertThat(actual.isEmpty()).isFalse(),
          assertThat(actual.size()).isNotEqualTo(0),
          assertThat(actual.size()).isPositive(),
          assertThat(Refaster.anyOf(actual.keySet(), actual.values(), actual.entrySet()))
              .isNotEmpty());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> actual) {
      return assertThat(actual).isNotEmpty();
    }
  }

  /**
   * Prefer {@link AbstractMapAssert#containsExactlyInAnyOrderEntriesOf(Map)} over less explicit
   * alternatives.
   */
  static final class AbstractMapAssertContainsExactlyInAnyOrderEntriesOf<
      K, V, M extends K, N extends V> {
    @BeforeTemplate
    AbstractMapAssert<?, ?, K, V> before(AbstractMapAssert<?, ?, K, V> mapAssert, Map<M, N> map) {
      return mapAssert.isEqualTo(map);
    }

    @AfterTemplate
    AbstractMapAssert<?, ?, K, V> after(AbstractMapAssert<?, ?, K, V> mapAssert, Map<M, N> map) {
      return mapAssert.containsExactlyInAnyOrderEntriesOf(map);
    }
  }

  /**
   * Prefer {@link AbstractMapAssert#containsExactlyEntriesOf(Map)} over less explicit alternatives.
   */
  static final class AbstractMapAssertContainsExactlyEntriesOfImmutableMapOf<K, V> {
    @BeforeTemplate
    AbstractMapAssert<?, ?, K, V> before(AbstractMapAssert<?, ?, K, V> mapAssert, K k1, V v1) {
      return mapAssert.containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(k1, v1));
    }

    @AfterTemplate
    AbstractMapAssert<?, ?, K, V> after(AbstractMapAssert<?, ?, K, V> mapAssert, K k1, V v1) {
      return mapAssert.containsExactlyEntriesOf(ImmutableMap.of(k1, v1));
    }
  }

  /** Prefer {@code assertThat(map).hasSize(int)} over more contrived alternatives. */
  static final class AssertThatHasSize<K, V> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Map<K, V> actual, int expected) {
      return Refaster.anyOf(
          assertThat(actual.size()).isEqualTo(expected),
          assertThat(Refaster.anyOf(actual.keySet(), actual.values(), actual.entrySet()))
              .hasSize(expected));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> actual, int expected) {
      return assertThat(actual).hasSize(expected);
    }
  }

  /** Prefer {@link AbstractMapAssert#hasSameSizeAs(Map)} over more contrived alternatives. */
  static final class AbstractMapAssertHasSameSizeAs<K, V> {
    @BeforeTemplate
    AbstractMapAssert<?, ?, K, V> before(AbstractMapAssert<?, ?, K, V> mapAssert, Map<?, ?> other) {
      return mapAssert.hasSize(other.size());
    }

    @AfterTemplate
    AbstractMapAssert<?, ?, K, V> after(AbstractMapAssert<?, ?, K, V> mapAssert, Map<?, ?> other) {
      return mapAssert.hasSameSizeAs(other);
    }
  }

  /** Prefer {@code assertThat(map).containsKey(Object)} over more contrived alternatives. */
  static final class AssertThatContainsKey<K, V> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Map<K, V> actual, K key) {
      return Refaster.anyOf(
          assertThat(actual.containsKey(key)).isTrue(), assertThat(actual.keySet()).contains(key));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> actual, K key) {
      return assertThat(actual).containsKey(key);
    }
  }

  /** Prefer {@code assertThat(map).doesNotContainKey(Object)} over more contrived alternatives. */
  static final class AssertThatDoesNotContainKey<K, V> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Map<K, V> actual, K key) {
      return Refaster.anyOf(
          assertThat(actual.containsKey(key)).isFalse(),
          assertThat(actual.keySet()).doesNotContain(key));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> actual, K key) {
      return assertThat(actual).doesNotContainKey(key);
    }
  }

  /** Prefer {@code assertThat(map).containsOnlyKeys(Object)} over more contrived alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatContainsOnlyKeysObject<K, V> {
    @BeforeTemplate
    AbstractCollectionAssert<?, Collection<? extends K>, K, ObjectAssert<K>> before(
        Map<K, V> actual, K key) {
      return assertThat(actual.keySet()).containsExactly(key);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> actual, K key) {
      return assertThat(actual).containsOnlyKeys(key);
    }
  }

  /** Prefer {@code assertThat(map).containsOnlyKeys(Iterable)} over more contrived alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatContainsOnlyKeysIterable<K, V, T extends K> {
    @BeforeTemplate
    AbstractCollectionAssert<?, Collection<? extends K>, K, ObjectAssert<K>> before(
        Map<K, V> actual, Iterable<T> keys) {
      return assertThat(actual.keySet()).hasSameElementsAs(keys);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> actual, Iterable<T> keys) {
      return assertThat(actual).containsOnlyKeys(keys);
    }
  }

  /** Prefer {@code assertThat(map).containsValue(Object)} over more contrived alternatives. */
  static final class AssertThatContainsValue<K, V> {
    @BeforeTemplate
    AbstractAssert<? extends AbstractAssert<?, ?>, ? extends Object> before(
        Map<K, V> actual, V value) {
      return Refaster.anyOf(
          assertThat(actual.containsValue(value)).isTrue(),
          assertThat(actual.values()).contains(value));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> actual, V value) {
      return assertThat(actual).containsValue(value);
    }
  }

  /**
   * Prefer {@code assertThat(map).doesNotContainValue(Object)} over more contrived alternatives.
   */
  static final class AssertThatDoesNotContainValue<K, V> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Map<K, V> actual, V value) {
      return Refaster.anyOf(
          assertThat(actual.containsValue(value)).isFalse(),
          assertThat(actual.values()).doesNotContain(value));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> actual, V value) {
      return assertThat(actual).doesNotContainValue(value);
    }
  }
}
