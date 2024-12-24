package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link Multimap}s. */
@OnlineDocumentation
final class MultimapRules {
  private MultimapRules() {}

  /** Prefer {@link Multimap#keySet()} over more contrived alternatives. */
  static final class MultimapKeySet<K, V> {
    @BeforeTemplate
    Set<K> before(Multimap<K, V> multimap) {
      return multimap.asMap().keySet();
    }

    @AfterTemplate
    Set<K> after(Multimap<K, V> multimap) {
      return multimap.keySet();
    }
  }

  /** Prefer {@link Multimap#isEmpty()} over more contrived alternatives. */
  static final class MultimapIsEmpty<K, V> {
    @BeforeTemplate
    boolean before(Multimap<K, V> multimap) {
      return Refaster.anyOf(
              multimap.keySet(), multimap.keys(), multimap.values(), multimap.entries())
          .isEmpty();
    }

    @AfterTemplate
    boolean after(Multimap<K, V> multimap) {
      return multimap.isEmpty();
    }
  }

  /** Prefer {@link Multimap#size()} over more contrived alternatives. */
  static final class MultimapSize<K, V> {
    @BeforeTemplate
    int before(Multimap<K, V> multimap) {
      return multimap.values().size();
    }

    @AfterTemplate
    int after(Multimap<K, V> multimap) {
      return multimap.size();
    }
  }

  /** Prefer {@link Multimap#containsKey(Object)} over more contrived alternatives. */
  static final class MultimapContainsKey<K, V, T> {
    @BeforeTemplate
    boolean before(Multimap<K, V> multimap, T key) {
      return Refaster.anyOf(multimap.keySet(), multimap.keys()).contains(key);
    }

    @AfterTemplate
    boolean after(Multimap<K, V> multimap, T key) {
      return multimap.containsKey(key);
    }
  }

  /** Prefer {@link Multimap#containsValue(Object)} over more contrived alternatives. */
  static final class MultimapContainsValue<K, V, T> {
    @BeforeTemplate
    boolean before(Multimap<K, V> multimap, T value) {
      return multimap.values().contains(value);
    }

    @AfterTemplate
    boolean after(Multimap<K, V> multimap, T value) {
      return multimap.containsValue(value);
    }
  }

  /**
   * Prefer {@link Multimap#get(Object)} over more contrived alternatives.
   *
   * <p><strong>Warning:</strong> this rewrite rule is not completely behavior preserving: the
   * original code will yield {@code null} for unknown keys, while the replacement code will return
   * an empty collection for unknown keys.
   */
  static final class MultimapGet<K, V> {
    @BeforeTemplate
    @Nullable Collection<V> before(Multimap<K, V> multimap, K key) {
      return Refaster.anyOf(multimap.asMap(), Multimaps.asMap(multimap)).get(key);
    }

    @AfterTemplate
    Collection<V> after(Multimap<K, V> multimap, K key) {
      return multimap.get(key);
    }
  }

  /** Don't unnecessarily use {@link Multimap#entries()}. */
  static final class MultimapKeysStream<K, V> {
    @BeforeTemplate
    Stream<K> before(Multimap<K, V> multimap) {
      return multimap.entries().stream().map(Map.Entry::getKey);
    }

    @AfterTemplate
    Stream<K> after(Multimap<K, V> multimap) {
      return multimap.keys().stream();
    }
  }

  /** Don't unnecessarily use {@link Multimap#entries()}. */
  static final class MultimapValuesStream<K, V> {
    @BeforeTemplate
    Stream<V> before(Multimap<K, V> multimap) {
      return multimap.entries().stream().map(Map.Entry::getValue);
    }

    @AfterTemplate
    Stream<V> after(Multimap<K, V> multimap) {
      return multimap.values().stream();
    }
  }
}
