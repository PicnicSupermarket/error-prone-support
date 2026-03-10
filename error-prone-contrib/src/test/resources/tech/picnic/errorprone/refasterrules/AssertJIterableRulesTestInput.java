package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJIterableRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Iterables.class);
  }

  void testAssertThatIsEmpty() {
    assertThat(ImmutableSet.of(1).iterator()).isExhausted();
    assertThat(ImmutableSet.of(2).isEmpty()).isTrue();
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsNotEmpty() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1).iterator()).hasNext(),
        assertThat(ImmutableSet.of(2).isEmpty()).isFalse());
  }

  ImmutableSet<AbstractIntegerAssert<?>> testAssertThatSize() {
    return ImmutableSet.of(
        assertThat(Iterables.size(ImmutableSet.of(1))), assertThat(ImmutableSet.of(2).size()));
  }

  AbstractAssert<?, ?> testAssertThatContains() {
    return assertThat(ImmutableSet.of(1).contains(1)).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatDoesNotContain() {
    return assertThat(ImmutableSet.of(1).contains(1)).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatContainsAll() {
    return assertThat(ImmutableSet.of(1).containsAll(ImmutableSet.of(2))).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatContainsExactly() {
    return assertThat(Iterables.getOnlyElement(ImmutableSet.of(new Object()))).isEqualTo("foo");
  }
}
