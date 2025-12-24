package tech.picnic.errorprone.refaster.test;

import com.google.common.collect.ImmutableSet;

/** Code to test the Refaster rules from {@link OperatorRefasterRules}. */
final class OperatorRefasterRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of();
  }

  boolean testLessThanOperator() {
    return 2 > 1;
  }

  boolean testConditionalAndOperator() {
    return !(!true || !false);
  }

  int testPlusOperator() {
    return 2 + 1;
  }

  boolean testLogicalComplementOperator() {
    return true == false;
  }
}
