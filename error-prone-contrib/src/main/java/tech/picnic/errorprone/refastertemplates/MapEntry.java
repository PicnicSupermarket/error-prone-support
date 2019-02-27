package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.Maps;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Map;

/**
 * Prefer {@link Map#entry(Object, Object)} over the Guava alternative.
 *
 * <p><strong>Warning:</strong> while the Guava variant allows {@code null} keys and values, the
 * preferred JDK variant does not. Moreover, the {@link Map.Entry} produced by Guava is {@link
 * java.io.Serializable}, while this does not hold for the object returned by the preferred
 * approach.
 */
final class MapEntry<K, V> {
  @BeforeTemplate
  Map.Entry<K, V> before(K key, V value) {
    return Maps.immutableEntry(key, value);
  }

  @AfterTemplate
  Map.Entry<K, V> after(K key, V value) {
    return Map.entry(key, value);
  }
}
