package tech.picnic.errorprone.refaster.test;

import static java.util.Collections.singletonMap;

import com.google.common.collect.ImmutableMap;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;

/** Refaster rule collection to validate the reporting of missing test methods. */
final class NestedRefasterAnyOfRules {
  private NestedRefasterAnyOfRules() {}

  static final class NestedRefasterAnyOf<K, V> {
    @BeforeTemplate
    Map<K, V> before(K k1, V v1) {
      return Refaster.anyOf(
          ImmutableMap.ofEntries(
              Refaster.anyOf(Map.entry(k1, v1), new SimpleImmutableEntry<>(k1, v1))),
          singletonMap(k1, v1));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(K k1, V v1) {
      return ImmutableMap.of(k1, v1);
    }
  }
}
