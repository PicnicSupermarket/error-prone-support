package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;

final class AssertJCharSequenceTemplatesTest implements RefasterTemplateTestCase {
  void testAbstractCharSequenceAssertContains() {
    assertThat("foo").contains("f");
  }

  void testAssertThatCharSequenceIsEmpty1() {
    assertThat("foo").isEmpty();
  }

  void testAssertThatCharSequenceIsEmpty2() {
    assertThat("foo").isEmpty();
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatCharSequenceIsNotEmpty() {
    return ImmutableSet.of(assertThat("foo").isNotEmpty(), assertThat("bar").isNotEmpty());
  }

  AbstractAssert<?, ?> testAssertThatCharSequenceHasSize() {
    return assertThat("foo").hasSize(3);
  }
}
