package tech.picnic.errorprone.refaster.test.input;

import com.google.common.collect.ImmutableSet;

/** Code to test the Refaster rules from {@link MethodWithoutPrefixRules}. */
final class MethodWithoutPrefixRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of();
  }

  boolean testStringIsEmpty() {
    return "foo".equals("");
  }

  private void foo() {}

  public String bar() {
    return "";
  }
}
