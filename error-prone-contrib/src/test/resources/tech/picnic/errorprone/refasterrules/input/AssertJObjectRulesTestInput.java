package tech.picnic.errorprone.refasterrules.input;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJObjectRulesTest implements RefasterRuleCollectionTestCase {
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
