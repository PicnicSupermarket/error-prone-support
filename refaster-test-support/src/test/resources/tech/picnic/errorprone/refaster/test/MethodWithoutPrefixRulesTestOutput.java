package tech.picnic.errorprone.refaster.test;

import com.google.common.collect.ImmutableSet;

/** Code to test the Refaster rules from {@link MethodWithoutPrefixRules}. */
final class MethodWithoutPrefixRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of();
  }

  boolean testStringIsEmpty() {
    return "foo".isEmpty();
  }

  /* ERROR: Method names should start with `test`. */
  private void foo() {}

  /* ERROR: Method names should start with `test`. */
  public String bar() {
    return "";
  }
}
