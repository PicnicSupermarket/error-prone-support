package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJCharSequenceRulesTest implements RefasterRuleCollectionTestCase {
  void testAssertThatCharSequenceIsEmpty() {
    assertThat("foo".isEmpty()).isTrue();
    assertThat("bar".length()).isEqualTo(0L);
    assertThat("baz".length()).isNotPositive();
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatCharSequenceIsNotEmpty() {
    return ImmutableSet.of(
        assertThat("foo".isEmpty()).isFalse(),
        assertThat("bar".length()).isNotEqualTo(0),
        assertThat("baz".length()).isPositive());
  }

  AbstractAssert<?, ?> testAssertThatCharSequenceHasSize() {
    return assertThat("foo".length()).isEqualTo(3);
  }
}
