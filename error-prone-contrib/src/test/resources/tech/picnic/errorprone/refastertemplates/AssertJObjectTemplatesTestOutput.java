package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractAssert;

final class AssertJObjectTemplatesTest implements RefasterTemplateTestCase {
  AbstractAssert<?, ?> testAssertThatIsInstanceOf() {
    return assertThat("foo").isInstanceOf(String.class);
  }

  AbstractAssert<?, ?> testAssertThatIsNotInstanceOf() {
    return assertThat("foo").isNotInstanceOf(String.class);
  }

  AbstractAssert<?, ?> testAssertThatIsIsEqualTo() {
    return assertThat("foo").isEqualTo("bar");
  }

  AbstractAssert<?, ?> testAssertThatIsIsNotEqualTo() {
    return assertThat("foo").isNotEqualTo("bar");
  }

  AbstractAssert<?, ?> testAssertThatHasToString() {
    return assertThat(new Object()).hasToString("foo");
  }
}
