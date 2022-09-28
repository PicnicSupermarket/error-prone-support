package tech.picnic.errorprone.refasterrules.output;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJCharSequenceRulesTest implements RefasterRuleCollectionTestCase {
  void testAssertThatCharSequenceIsEmpty() {
    assertThat("foo").isEmpty();
    assertThat("foo").isEmpty();
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatCharSequenceIsNotEmpty() {
    return ImmutableSet.of(assertThat("foo").isNotEmpty(), assertThat("bar").isNotEmpty());
  }

  AbstractAssert<?, ?> testAssertThatCharSequenceHasSize() {
    return assertThat("foo").hasSize(3);
  }
}
