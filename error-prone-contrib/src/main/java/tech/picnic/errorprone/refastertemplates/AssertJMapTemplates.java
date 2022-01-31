package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableMap;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Map;
import org.assertj.core.api.AbstractMapAssert;

final class AssertJMapTemplates {
  private AssertJMapTemplates() {}

  static final class AbstractMapAssertContainsExactlyEntriesOf<K, V> {
    @BeforeTemplate
    AbstractMapAssert<?, ?, K, V> before(AbstractMapAssert<?, ?, K, V> mapAssert, Map<K, V> map) {
      return mapAssert.isEqualTo(map);
    }

    @AfterTemplate
    AbstractMapAssert<?, ?, K, V> after(AbstractMapAssert<?, ?, K, V> mapAssert, Map<K, V> map) {
      return mapAssert.containsExactlyInAnyOrderEntriesOf(map);
    }
  }

  static final class AbstractMapAssertContainsEntry<K, V> {
    @BeforeTemplate
    AbstractMapAssert<?, ?, K, V> before(AbstractMapAssert<?, ?, K, V> mapAssert, K key, V value) {
      return mapAssert.containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(key, value));
    }

    @AfterTemplate
    AbstractMapAssert<?, ?, K, V> after(AbstractMapAssert<?, ?, K, V> mapAssert, K key, V value) {
      return mapAssert.containsExactly(Map.entry(key, value));
    }
  }
}
