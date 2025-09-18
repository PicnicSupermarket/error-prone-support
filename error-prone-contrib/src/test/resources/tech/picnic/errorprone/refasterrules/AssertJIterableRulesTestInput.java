package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJIterableRulesTest implements RefasterRuleCollectionTestCase {
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Iterables.class);
  }

  void testAssertThatIterableIsEmpty() {
    assertThat(ImmutableSet.of(1).iterator()).isExhausted();
    assertThat(ImmutableSet.of(2).isEmpty()).isTrue();
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIterableIsNotEmpty() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1).iterator()).hasNext(),
        assertThat(ImmutableSet.of(2).isEmpty()).isFalse());
  }

  ImmutableSet<AbstractIntegerAssert<?>> testAssertThatIterableSize() {
    return ImmutableSet.of(
        assertThat(Iterables.size(ImmutableSet.of(1))), assertThat(ImmutableSet.of(2).size()));
  }

  AbstractAssert<?, ?> testAssertThatIterableHasOneElementEqualTo() {
    return assertThat(Iterables.getOnlyElement(ImmutableSet.of(new Object()))).isEqualTo("foo");
  }

  AbstractIntegerAssert<?> testAssertThatCollectionHasSize() {
    return assertThat(ImmutableSet.of(1, 2, 3).size()).isEqualTo(3);
  }
}
