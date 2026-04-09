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
    assertThat(ImmutableSet.of(1)).isEmpty();
    assertThat(ImmutableSet.of(2)).isEmpty();
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIterableIsNotEmpty() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).isNotEmpty(), assertThat(ImmutableSet.of(2)).isNotEmpty());
  }

  ImmutableSet<AbstractIntegerAssert<?>> testAssertThatIterableSize() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).size(), assertThat(ImmutableSet.of(2)).size());
  }

  AbstractAssert<?, ?> testAssertThatCollectionContains() {
    return assertThat(ImmutableSet.of(1)).contains(1);
  }

  AbstractAssert<?, ?> testAssertThatCollectionDoesNotContain() {
    return assertThat(ImmutableSet.of(1)).doesNotContain(1);
  }

  AbstractAssert<?, ?> testAssertThatCollectionContainsAll() {
    return assertThat(ImmutableSet.of(1)).containsAll(ImmutableSet.of(2));
  }

  AbstractAssert<?, ?> testAssertThatIterableHasOneElementEqualTo() {
    return assertThat(ImmutableSet.of(new Object())).containsExactly("foo");
  }
}
