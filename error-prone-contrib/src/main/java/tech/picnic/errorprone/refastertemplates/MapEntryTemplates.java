package tech.picnic.errorprone.refastertemplates;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Map.Entry.comparingByKey;
import static java.util.Map.Entry.comparingByValue;

import com.google.common.collect.Maps;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;

/** Refaster rules related to expressions dealing with {@link Map.Entry} instances. */
final class MapEntryTemplates {
  private MapEntryTemplates() {}

  /**
   * Prefer {@link Map#entry(Object, Object)} over alternative ways to create an immutable map
   * entry.
   *
   * <p><strong>Warning:</strong> while both {@link Maps#immutableEntry(Object, Object)} and {@link
   * AbstractMap.SimpleImmutableEntry} allow {@code null} keys and values, the preferred @link
   * Map#entry(Object, Object)} variant does not. Moreover, the {@link Map.Entry} instances produced
   * by the former approaches is {@link java.io.Serializable}, while this does not hold for the
   * object returned by the preferred approach.
   */
  static final class MapEntry<K, V> {
    @BeforeTemplate
    Map.Entry<K, V> before(K key, V value) {
      return Refaster.anyOf(
          Maps.immutableEntry(key, value), new AbstractMap.SimpleImmutableEntry<>(key, value));
    }

    @AfterTemplate
    Map.Entry<K, V> after(K key, V value) {
      return Map.entry(key, value);
    }
  }

  /** Prefer {@link Map.Entry#comparingByKey()} over more verbose alternatives. */
  // XXX: Also rewrite `Comparator.comparing{Double,Int,Long}(Map.Entry::getKey)`.
  static final class MapEntryComparingByKey<K extends Comparable<? super K>, V> {
    @BeforeTemplate
    Comparator<Map.Entry<K, V>> before() {
      return Refaster.anyOf(comparing(Map.Entry::getKey), comparingByKey(naturalOrder()));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Comparator<Map.Entry<K, V>> after() {
      return comparingByKey();
    }
  }

  /** Prefer {@link Map.Entry#comparingByKey(Comparator)} over more verbose alternatives. */
  static final class MapEntryComparingByKeyWithCustomComparator<K, V> {
    @BeforeTemplate
    Comparator<Map.Entry<K, V>> before(Comparator<? super K> cmp) {
      return comparing(Map.Entry::getKey, cmp);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Comparator<Map.Entry<K, V>> after(Comparator<? super K> cmp) {
      return comparingByKey(cmp);
    }
  }

  /** Prefer {@link Map.Entry#comparingByValue()} over more verbose alternatives. */
  // XXX: Also rewrite `Comparator.comparing{Double,Int,Long}(Map.Entry::getValue)`.
  static final class MapEntryComparingByValue<K, V extends Comparable<? super V>> {
    @BeforeTemplate
    Comparator<Map.Entry<K, V>> before() {
      return Refaster.anyOf(comparing(Map.Entry::getValue), comparingByValue(naturalOrder()));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Comparator<Map.Entry<K, V>> after() {
      return comparingByValue();
    }
  }

  /** Prefer {@link Map.Entry#comparingByValue(Comparator)} over more verbose alternatives. */
  static final class MapEntryComparingByValueWithCustomComparator<K, V> {
    @BeforeTemplate
    Comparator<Map.Entry<K, V>> before(Comparator<? super V> cmp) {
      return comparing(Map.Entry::getValue, cmp);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Comparator<Map.Entry<K, V>> after(Comparator<? super V> cmp) {
      return comparingByValue(cmp);
    }
  }
}
