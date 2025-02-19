package tech.picnic.errorprone.refaster.test;

import java.util.Collections;
import java.util.Map;

/** Code to test the Refaster rules from {@link StaticImportEdgeCaseRules}. */
final class StaticImportEdgeCaseRulesTest implements RefasterRuleCollectionTestCase {
  Map<?, ?> testImmutableMapOf1() {
    Map<String, String> stringStringMap = Map.of("1", "2");
    return Collections.singletonMap("k1", "v1");
  }
}
