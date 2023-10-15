package tech.picnic.errorprone.refaster.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Refaster rule collection to validate reporting of unsorted arguments of {@link
 * RefasterRuleCollectionTestCase#elidedTypesAndStaticImports} in test classes.
 */
final class NonSortedElidedTypesAndStaticImportsTestRules
    implements RefasterRuleCollectionTestCase {
  private NonSortedElidedTypesAndStaticImportsTestRules() {}

  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Lists.class, assertDoesNotThrow(() -> null), Iterables.class);
  }
}
