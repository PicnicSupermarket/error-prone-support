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
import java.util.Set;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractCollectionAssert;
import org.assertj.core.api.AbstractMapAssert;
import org.assertj.core.api.MapAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.matchers.IsEmpty;

@OnlineDocumentation
final class AssertJMapRules {
  private AssertJMapRules() {}

  static final class AbstractMapAssertIsEmpty<K, V> {
    @BeforeTemplate
    void before(
        AbstractMapAssert<?, ?, K, V> mapAssert,
        @Matches(IsEmpty.class) Map<? extends K, ? extends V> wellTypedMap,
        @Matches(IsEmpty.class) Map<?, ?> arbitraryMap,
        @Matches(IsEmpty.class) Iterable<? extends K> keys) {
      Refaster.anyOf(
          mapAssert.containsExactlyEntriesOf(wellTypedMap),
          mapAssert.containsExactlyInAnyOrderEntriesOf(wellTypedMap),
          mapAssert.hasSameSizeAs(arbitraryMap),
          mapAssert.isEqualTo(arbitraryMap),
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

  static final class AssertThatMapIsEmpty<K, V> {
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

  static final class AssertThatMapIsNotEmpty<K, V> {
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

  static final class AbstractMapAssertContainsExactlyInAnyOrderEntriesOf<K, V> {
    @BeforeTemplate
    AbstractMapAssert<?, ?, K, V> before(
        AbstractMapAssert<?, ?, K, V> mapAssert, Map<? extends K, ? extends V> map) {
      return mapAssert.isEqualTo(map);
    }

    @AfterTemplate
    AbstractMapAssert<?, ?, K, V> after(
        AbstractMapAssert<?, ?, K, V> mapAssert, Map<? extends K, ? extends V> map) {
      return mapAssert.containsExactlyInAnyOrderEntriesOf(map);
    }
  }

  static final class AbstractMapAssertContainsExactlyEntriesOf<K, V> {
    @BeforeTemplate
    AbstractMapAssert<?, ?, K, V> before(AbstractMapAssert<?, ?, K, V> mapAssert, K key, V value) {
      return mapAssert.containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(key, value));
    }

    @AfterTemplate
    AbstractMapAssert<?, ?, K, V> after(AbstractMapAssert<?, ?, K, V> mapAssert, K key, V value) {
      return mapAssert.containsExactlyEntriesOf(ImmutableMap.of(key, value));
    }
  }

  static final class AssertThatMapHasSize<K, V> {
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

  static final class AssertThatMapContainsKey<K, V> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Map<K, V> map, K key) {
      return assertThat(map.containsKey(key)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> map, K key) {
      return assertThat(map).containsKey(key);
    }
  }

  static final class AssertThatMapDoesNotContainKey<K, V> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Map<K, V> map, K key) {
      return assertThat(map.containsKey(key)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> map, K key) {
      return assertThat(map).doesNotContainKey(key);
    }
  }

  static final class AssertThatMapContainsOnlyKeys<K, V> {
    @BeforeTemplate
    AbstractCollectionAssert<?, Collection<? extends K>, K, ?> before(
        Map<K, V> map, Set<? extends K> keys) {
      return assertThat(map.keySet()).hasSameElementsAs(keys);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> map, Set<? extends K> keys) {
      return assertThat(map).containsOnlyKeys(keys);
    }
  }

  static final class AssertThatMapContainsValue<K, V> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Map<K, V> map, V value) {
      return assertThat(map.containsValue(value)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> map, V value) {
      return assertThat(map).containsValue(value);
    }
  }

  static final class AssertThatMapDoesNotContainValue<K, V> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Map<K, V> map, V value) {
      return assertThat(map.containsValue(value)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> map, V value) {
      return assertThat(map).doesNotContainValue(value);
    }
  }
}
