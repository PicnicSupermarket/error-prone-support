package tech.picnic.errorprone.refaster.test;

import com.google.common.collect.ImmutableSet;

/** Code to test the Refaster rules from {@link BlockTemplateRules}. */
final class BlockTemplateRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of();
  }

  void testThrowIllegalArgumentException() {
    boolean invalid = true;
    if (!invalid) {
      throw new IllegalArgumentException();
    }
  }
}
