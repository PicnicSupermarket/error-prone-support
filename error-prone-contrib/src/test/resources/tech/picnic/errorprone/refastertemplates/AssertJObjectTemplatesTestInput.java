package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractAssert;

final class AssertJObjectTemplatesTest implements RefasterTemplateTestCase {
  AbstractAssert<?, ?> testAssertThatIsInstanceOf() {
    return assertThat("foo" instanceof String).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsNotInstanceOf() {
    return assertThat("foo" instanceof String).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatIsIsEqualTo() {
    return assertThat("foo".equals("bar")).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsIsNotEqualTo() {
    return assertThat("foo".equals("bar")).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatHasToString() {
    return assertThat(new Object().toString()).isEqualTo("foo");
  }
}
