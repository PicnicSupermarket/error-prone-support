package tech.picnic.errorprone.refasterrules;

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
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link Map.Entry} instances. */
@OnlineDocumentation
final class MapEntryRules {
  private MapEntryRules() {}

  /**
   * Prefer {@link Map#entry(Object, Object)} over non-JDK alternatives or the associated
   * constructor.
   *
   * <p><strong>Warning:</strong> while both {@link Maps#immutableEntry(Object, Object)} and {@link
   * AbstractMap.SimpleImmutableEntry} allow {@code null} keys and values, the preferred {@link
   * Map#entry(Object, Object)} variant does not. Moreover, the {@link Map.Entry} instances produced
   * by the former approaches is {@link java.io.Serializable}, while this does not hold for the
   * object returned by the preferred approach.
   */
  static final class MapEntry<K, V> {
    @BeforeTemplate
    Map.Entry<K, V> before(K k, V v) {
      return Refaster.anyOf(
          Maps.immutableEntry(k, v), new AbstractMap.SimpleImmutableEntry<>(k, v));
    }

    @AfterTemplate
    Map.Entry<K, V> after(K k, V v) {
      return Map.entry(k, v);
    }
  }

  /** Prefer {@link Map.Entry#comparingByKey()} over more verbose alternatives. */
  // XXX: Also rewrite `Comparator.comparing{Double,Int,Long}(Map.Entry::getKey)`.
  static final class ComparingByKey<K extends Comparable<? super K>, V> {
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
  static final class ComparingByKeyWithComparator<K extends C, V, C> {
    @BeforeTemplate
    Comparator<Map.Entry<K, V>> before(Comparator<C> cmp) {
      return comparing(Map.Entry::getKey, cmp);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Comparator<Map.Entry<K, V>> after(Comparator<C> cmp) {
      return comparingByKey(cmp);
    }
  }

  /** Prefer {@link Map.Entry#comparingByValue()} over more verbose alternatives. */
  // XXX: Also rewrite `Comparator.comparing{Double,Int,Long}(Map.Entry::getValue)`.
  static final class ComparingByValue<K, V extends Comparable<? super V>> {
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
  static final class ComparingByValueWithComparator<K, V extends C, C> {
    @BeforeTemplate
    Comparator<Map.Entry<K, V>> before(Comparator<C> cmp) {
      return comparing(Map.Entry::getValue, cmp);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Comparator<Map.Entry<K, V>> after(Comparator<C> cmp) {
      return comparingByValue(cmp);
    }
  }
}
