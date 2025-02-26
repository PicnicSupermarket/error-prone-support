package tech.picnic.errorprone.refaster.test;

import java.util.Collections;
import java.util.Map;

/** Code to test the Refaster rules from {@link NestedRefasterAnyOfRules}. */
final class NestedRefasterAnyOfRulesTest implements RefasterRuleCollectionTestCase {
  Map<?, ?> testNestedRefasterAnyOf() {
    Map<String, String> stringStringMap = Map.of("1", "2");
    return ImmutableMap.of("k1", "v1");
  }
}
