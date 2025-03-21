package tech.picnic.errorprone.refaster.test;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.Map;

/** Code to test the Refaster rules from {@link SingleRefasterAnyOfRules}. */
final class SingleRefasterAnyOfRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Collections.class);
  }

  Map<?, ?> testSingleRefasterAnyOf() {
    return Collections.singletonMap("k1", "v1");
  }
}
