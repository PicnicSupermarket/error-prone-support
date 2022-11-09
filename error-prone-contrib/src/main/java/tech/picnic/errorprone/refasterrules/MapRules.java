package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.jspecify.nullness.Nullable;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link Map} instances. */
@OnlineDocumentation
final class MapRules {
  private MapRules() {}

  // XXX: We could add a rule for `new EnumMap(Map<K, ? extends V> m)`, but that constructor does
  // not allow an empty non-EnumMap to be provided.
  static final class CreateEnumMap<K extends Enum<K>, V> {
    @BeforeTemplate
    Map<K, V> before() {
      return new HashMap<>();
    }

    @AfterTemplate
    Map<K, V> after() {
      return new EnumMap<>(Refaster.<K>clazz());
    }
  }

  static final class MapGetOrNull<K, V, T> {
    @BeforeTemplate
    @Nullable V before(Map<K, V> map, T key) {
      return map.getOrDefault(key, null);
    }

    @AfterTemplate
    @Nullable V after(Map<K, V> map, T key) {
      return map.get(key);
    }
  }

  /** Prefer {@link Map#isEmpty()} over more contrived alternatives. */
  static final class MapIsEmpty<K, V> {
    @BeforeTemplate
    boolean before(Map<K, V> map) {
      return Refaster.anyOf(map.keySet(), map.values(), map.entrySet()).isEmpty();
    }

    @AfterTemplate
    boolean after(Map<K, V> map) {
      return map.isEmpty();
    }
  }

  /** Prefer {@link Map#size()} over more contrived alternatives. */
  static final class MapSize<K, V> {
    @BeforeTemplate
    int before(Map<K, V> map) {
      return Refaster.anyOf(map.keySet(), map.values(), map.entrySet()).size();
    }

    @AfterTemplate
    int after(Map<K, V> map) {
      return map.size();
    }
  }

  /** Prefer {@link Map#containsKey(Object)} over more contrived alternatives. */
  static final class MapContainsKey<K, V, T> {
    @BeforeTemplate
    boolean before(Map<K, V> map, T key) {
      return map.keySet().contains(key);
    }

    @AfterTemplate
    boolean after(Map<K, V> map, T key) {
      return map.containsKey(key);
    }
  }

  /** Prefer {@link Map#containsValue(Object)} over more contrived alternatives. */
  static final class MapContainsValue<K, V, T> {
    @BeforeTemplate
    boolean before(Map<K, V> map, T value) {
      return map.values().contains(value);
    }

    @AfterTemplate
    boolean after(Map<K, V> map, T value) {
      return map.containsValue(value);
    }
  }

  /** Don't unnecessarily use {@link Map#entrySet()}. */
  static final class MapKeyStream<K, V> {
    @BeforeTemplate
    Stream<K> before(Map<K, V> map) {
      return map.entrySet().stream().map(Map.Entry::getKey);
    }

    @AfterTemplate
    Stream<K> after(Map<K, V> map) {
      return map.keySet().stream();
    }
  }

  /** Don't unnecessarily use {@link Map#entrySet()}. */
  static final class MapValueStream<K, V> {
    @BeforeTemplate
    Stream<V> before(Map<K, V> map) {
      return map.entrySet().stream().map(Map.Entry::getValue);
    }

    @AfterTemplate
    Stream<V> after(Map<K, V> map) {
      return map.values().stream();
    }
  }
}
