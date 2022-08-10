package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJCharSequenceTemplatesTest implements RefasterTemplateTestCase {
  void testAssertThatCharSequenceIsEmpty() {
    assertThat("foo".length()).isEqualTo(0L);
    assertThat("foo".length()).isNotPositive();
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatCharSequenceIsNotEmpty() {
    return ImmutableSet.of(
        assertThat("foo".length()).isNotEqualTo(0), assertThat("bar".length()).isPositive());
  }

  AbstractAssert<?, ?> testAssertThatCharSequenceHasSize() {
    return assertThat("foo".length()).isEqualTo(3);
  }
}
