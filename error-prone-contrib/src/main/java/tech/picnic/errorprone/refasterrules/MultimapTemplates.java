package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nullable;

/** Refaster rules related to expressions dealing with {@link Multimap}s. */
final class MultimapTemplates {
  private MultimapTemplates() {}

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

  /**
   * Prefer {@link Multimap#get(Object)} over more contrived alternatives.
   *
   * <p><strong>Warning:</strong> this rewrite rule is not completely behavior preserving: the
   * original code will yield {@code null} for unknown keys, while the replacement code will return
   * an empty collection for unknown keys.
   */
  static final class MultimapGet<K, V> {
    @BeforeTemplate
    @Nullable
    Collection<V> before(Multimap<K, V> multimap, K key) {
      return Refaster.anyOf(multimap.asMap(), Multimaps.asMap(multimap)).get(key);
    }

    @AfterTemplate
    Collection<V> after(Multimap<K, V> multimap, K key) {
      return multimap.get(key);
    }
  }
}
