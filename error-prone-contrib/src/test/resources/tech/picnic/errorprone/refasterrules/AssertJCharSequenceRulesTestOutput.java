package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJCharSequenceRulesTest implements RefasterRuleCollectionTestCase {
  void testAssertThatCharSequenceIsEmpty() {
    assertThat("foo").isEmpty();
    assertThat("bar").isEmpty();
    assertThat("baz").isEmpty();
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatCharSequenceIsNotEmpty() {
    return ImmutableSet.of(
        assertThat("foo").isNotEmpty(),
        assertThat("bar").isNotEmpty(),
        assertThat("baz").isNotEmpty());
  }

  AbstractAssert<?, ?> testAssertThatCharSequenceHasSize() {
    return assertThat("foo").hasSize(3);
  }
}
