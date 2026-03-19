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
    assertThat(ImmutableSet.of(1)).isEmpty();
    assertThat(ImmutableSet.of(2)).isEmpty();
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsNotEmpty() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).isNotEmpty(), assertThat(ImmutableSet.of(2)).isNotEmpty());
  }

  ImmutableSet<AbstractIntegerAssert<?>> testAssertThatSize() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of(1)).size(), assertThat(ImmutableSet.of(2)).size());
  }

  AbstractAssert<?, ?> testAssertThatContainsExactly() {
    return assertThat(ImmutableSet.of(new Object())).containsExactly("foo");
  }
}
