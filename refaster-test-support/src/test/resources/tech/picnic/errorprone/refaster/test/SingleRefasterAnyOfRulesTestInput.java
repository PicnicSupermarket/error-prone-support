package tech.picnic.errorprone.refaster.test;

import java.util.Collections;
import java.util.Map;

/** Code to test the Refaster rules from {@link SingleRefasterAnyOfRules}. */
final class SingleRefasterAnyOfRulesTest implements RefasterRuleCollectionTestCase {
  Map<?, ?> testSingleRefasterAnyOf() {
    return Collections.singletonMap("k1", "v1");
  }
}
