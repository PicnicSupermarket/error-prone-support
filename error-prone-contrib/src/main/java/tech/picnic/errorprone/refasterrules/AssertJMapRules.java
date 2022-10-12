package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableMap;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Map;
import org.assertj.core.api.AbstractMapAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

@OnlineDocumentation
final class AssertJMapRules {
  private AssertJMapRules() {}

  static final class AbstractMapAssertContainsExactlyInAnyOrderEntriesOf<K, V> {
    @BeforeTemplate
    AbstractMapAssert<?, ?, K, V> before(AbstractMapAssert<?, ?, K, V> mapAssert, Map<K, V> map) {
      return mapAssert.isEqualTo(map);
    }

    @AfterTemplate
    AbstractMapAssert<?, ?, K, V> after(AbstractMapAssert<?, ?, K, V> mapAssert, Map<K, V> map) {
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
}
