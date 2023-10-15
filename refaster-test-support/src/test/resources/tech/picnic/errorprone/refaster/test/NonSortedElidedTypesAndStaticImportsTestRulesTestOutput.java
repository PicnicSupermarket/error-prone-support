package tech.picnic.errorprone.refaster.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Refaster rule collection to validate reporting of unsorted arguments of {@link
 * RefasterRuleCollectionTestCase#elidedTypesAndStaticImports} in test classes.
 */
final class NonSortedElidedTypesAndStaticImportsTestRulesTest
    implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    /* ERROR: Arguments of elidedTypesAndStaticImports should be sorted lexicographically.
    Did you mean: `(assertDoesNotThrow(() -> null), Iterables.class, Lists.class)`. */
    return ImmutableSet.of(Lists.class, assertDoesNotThrow(() -> null), Iterables.class);
  }
}

// This is a comment to appease Checkstyle.
/* ERROR: Unexpected token. */
;
