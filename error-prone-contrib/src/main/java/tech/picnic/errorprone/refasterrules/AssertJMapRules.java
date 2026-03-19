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
        @Matches(IsEmpty.class) Map<M, N> wellTypedMap,
        @Matches(IsEmpty.class) Map<?, ?> arbitrarilyTypedMap,
        @Matches(IsEmpty.class) Iterable<T> keys) {
      Refaster.anyOf(
          mapAssert.containsExactlyEntriesOf(wellTypedMap),
          mapAssert.containsExactlyInAnyOrderEntriesOf(wellTypedMap),
          mapAssert.hasSameSizeAs(arbitrarilyTypedMap),
          mapAssert.isEqualTo(arbitrarilyTypedMap),
          mapAssert.containsOnlyKeys(keys),
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
    void before(Map<K, V> map) {
      Refaster.anyOf(
          assertThat(map).hasSize(0),
          assertThat(map.isEmpty()).isTrue(),
          assertThat(map.size()).isEqualTo(0L),
          assertThat(map.size()).isNotPositive());
    }

    @BeforeTemplate
    void before2(Map<K, V> map) {
      assertThat(Refaster.anyOf(map.keySet(), map.values(), map.entrySet())).isEmpty();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Map<K, V> map) {
      assertThat(map).isEmpty();
    }
  }

  /** Prefer {@link AbstractMapAssert#isNotEmpty()} over more contrived alternatives. */
  static final class AbstractMapAssertIsNotEmpty<K, V> {
    @BeforeTemplate
    AbstractMapAssert<?, ?, K, V> before(
        AbstractMapAssert<?, ?, K, V> mapAssert, @Matches(IsEmpty.class) Map<?, ?> map) {
      return mapAssert.isNotEqualTo(map);
    }

    @AfterTemplate
    AbstractMapAssert<?, ?, K, V> after(AbstractMapAssert<?, ?, K, V> mapAssert) {
      return mapAssert.isNotEmpty();
    }
  }

  /** Prefer {@code assertThat(map).isNotEmpty()} over more contrived alternatives. */
  static final class AssertThatIsNotEmpty<K, V> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Map<K, V> map) {
      return Refaster.anyOf(
          assertThat(map.isEmpty()).isFalse(),
          assertThat(map.size()).isNotEqualTo(0),
          assertThat(map.size()).isPositive(),
          assertThat(Refaster.anyOf(map.keySet(), map.values(), map.entrySet())).isNotEmpty());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> map) {
      return assertThat(map).isNotEmpty();
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
    AbstractMapAssert<?, ?, K, V> before(AbstractMapAssert<?, ?, K, V> mapAssert, K key, V value) {
      return mapAssert.containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(key, value));
    }

    @AfterTemplate
    AbstractMapAssert<?, ?, K, V> after(AbstractMapAssert<?, ?, K, V> mapAssert, K key, V value) {
      return mapAssert.containsExactlyEntriesOf(ImmutableMap.of(key, value));
    }
  }

  /** Prefer {@code assertThat(map).hasSize(int)} over more contrived alternatives. */
  static final class AssertThatHasSize<K, V> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Map<K, V> map, int length) {
      return Refaster.anyOf(
          assertThat(map.size()).isEqualTo(length),
          assertThat(Refaster.anyOf(map.keySet(), map.values(), map.entrySet())).hasSize(length));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> map, int length) {
      return assertThat(map).hasSize(length);
    }
  }

  /** Prefer {@link AbstractMapAssert#hasSameSizeAs(Map)} over more contrived alternatives. */
  static final class AbstractMapAssertHasSameSizeAs<K, V> {
    @BeforeTemplate
    AbstractMapAssert<?, ?, K, V> before(AbstractMapAssert<?, ?, K, V> mapAssert, Map<?, ?> map) {
      return mapAssert.hasSize(map.size());
    }

    @AfterTemplate
    AbstractMapAssert<?, ?, K, V> after(AbstractMapAssert<?, ?, K, V> mapAssert, Map<?, ?> map) {
      return mapAssert.hasSameSizeAs(map);
    }
  }

  /** Prefer {@code assertThat(map).containsKey(Object)} over more contrived alternatives. */
  static final class AssertThatContainsKey<K, V> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Map<K, V> map, K key) {
      return Refaster.anyOf(
          assertThat(map.containsKey(key)).isTrue(), assertThat(map.keySet()).contains(key));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> map, K key) {
      return assertThat(map).containsKey(key);
    }
  }

  /** Prefer {@code assertThat(map).doesNotContainKey(Object)} over more contrived alternatives. */
  static final class AssertThatDoesNotContainKey<K, V> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Map<K, V> map, K key) {
      return Refaster.anyOf(
          assertThat(map.containsKey(key)).isFalse(), assertThat(map.keySet()).doesNotContain(key));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> map, K key) {
      return assertThat(map).doesNotContainKey(key);
    }
  }

  /** Prefer {@code assertThat(map).containsOnlyKeys(Object)} over more contrived alternatives. */
  // XXX: Strictly speaking this rule could be merged into `AssertThatMapContainsOnlyKeys` below,
  // but that rule targets another `containsOnlyKeys` overload. Review how cases like this should
  // impact the preferred naming scheme.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsOnlyKeysObject<K, V> {
    @BeforeTemplate
    AbstractCollectionAssert<?, Collection<? extends K>, K, ObjectAssert<K>> before(
        Map<K, V> map, K key) {
      return assertThat(map.keySet()).containsExactly(key);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> map, K key) {
      return assertThat(map).containsOnlyKeys(key);
    }
  }

  /** Prefer {@code assertThat(map).containsOnlyKeys(Iterable)} over more contrived alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatContainsOnlyKeysIterable<K, V, T extends K> {
    @BeforeTemplate
    AbstractCollectionAssert<?, Collection<? extends K>, K, ObjectAssert<K>> before(
        Map<K, V> map, Iterable<T> keys) {
      return assertThat(map.keySet()).hasSameElementsAs(keys);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> map, Iterable<T> keys) {
      return assertThat(map).containsOnlyKeys(keys);
    }
  }

  /** Prefer {@code assertThat(map).containsValue(Object)} over more contrived alternatives. */
  static final class AssertThatContainsValue<K, V> {
    @BeforeTemplate
    AbstractAssert<? extends AbstractAssert<?, ?>, ? extends Object> before(
        Map<K, V> map, V value) {
      return Refaster.anyOf(
          assertThat(map.containsValue(value)).isTrue(), assertThat(map.values()).contains(value));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> map, V value) {
      return assertThat(map).containsValue(value);
    }
  }

  /**
   * Prefer {@code assertThat(map).doesNotContainValue(Object)} over more contrived alternatives.
   */
  static final class AssertThatDoesNotContainValue<K, V> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Map<K, V> map, V value) {
      return Refaster.anyOf(
          assertThat(map.containsValue(value)).isFalse(),
          assertThat(map.values()).doesNotContain(value));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> map, V value) {
      return assertThat(map).doesNotContainValue(value);
    }
  }
}
