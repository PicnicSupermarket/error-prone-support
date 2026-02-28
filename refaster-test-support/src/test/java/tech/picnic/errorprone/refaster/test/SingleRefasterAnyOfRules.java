package tech.picnic.errorprone.refaster.test;

import static java.util.Collections.singletonMap;

import com.google.common.collect.ImmutableMap;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Map;

/** Refaster rule collection to validate the reporting of missing test methods. */
final class SingleRefasterAnyOfRules {
  private SingleRefasterAnyOfRules() {}

  static final class SingleRefasterAnyOf<K, V> {
    @BeforeTemplate
    @SuppressWarnings("ImmutableMapOf1" /* Similar rule for testing purposes. */)
    Map<K, V> before(K k1, V v1) {
      return Refaster.anyOf(ImmutableMap.ofEntries(Map.entry(k1, v1)), singletonMap(k1, v1));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(K k1, V v1) {
      return ImmutableMap.of(k1, v1);
    }
  }
}
