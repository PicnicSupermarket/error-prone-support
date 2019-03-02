package tech.picnic.errorprone.refastertemplates.tobeunified;

import com.google.common.collect.Maps;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.AbstractMap;
import java.util.Map;

/**
 * Prefer {@link Map#entry(Object, Object)} over alternative ways to create an immutable map entry.
 *
 * <p><strong>Warning:</strong> while both {@link Maps#immutableEntry(Object, Object)} and {@link
 * AbstractMap.SimpleImmutableEntry} allow {@code null} keys and values, the preferred @link
 * Map#entry(Object, Object)} variant does not. Moreover, the {@link Map.Entry} instances produced
 * by the former approaches is {@link java.io.Serializable}, while this does not hold for the object
 * returned by the preferred approach.
 */
final class MapEntry<K, V> {
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
