package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;

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
