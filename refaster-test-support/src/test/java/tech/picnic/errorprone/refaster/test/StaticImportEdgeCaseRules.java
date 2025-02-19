package tech.picnic.errorprone.refaster.test;

import static java.util.Collections.singletonMap;

import com.google.common.collect.ImmutableMap;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Map;

/** Refaster rule collection to validate the reporting of missing test methods. */
final class StaticImportEdgeCaseRules {
  private StaticImportEdgeCaseRules() {}

  static final class ImmutableMapOf1<K, V> {
    @BeforeTemplate
    Map<K, V> before(K k1, V v1) {
      return Refaster.anyOf(
          //          ImmutableMap.<K, V>builder().put(k1, v1).buildOrThrow(),
          // XXX: Simply remove the first of the `Refaster#anyOf` and it will match.
          ImmutableMap.ofEntries(Map.entry(k1, v1)), singletonMap(k1, v1));
      //          Map.of(k1, v1));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(K k1, V v1) {
      return ImmutableMap.of(k1, v1);
    }
  }
}
