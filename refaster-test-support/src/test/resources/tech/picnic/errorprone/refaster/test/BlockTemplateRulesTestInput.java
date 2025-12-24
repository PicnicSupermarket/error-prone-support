package tech.picnic.errorprone.refaster.test;

import com.google.common.collect.ImmutableSet;
import java.util.function.Predicate;

/** Code to test the Refaster rules from {@link BlockTemplateRules}. */
final class BlockTemplateRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of();
  }

  void testThrowIllegalArgumentException() {
    boolean invalid = true;
    if (invalid) {
      throw new IllegalArgumentException();
    }
  }

  boolean testPredicateTest() {
    Predicate<String> predicate = String::isEmpty;
    return predicate.test("foo");
  }
}
