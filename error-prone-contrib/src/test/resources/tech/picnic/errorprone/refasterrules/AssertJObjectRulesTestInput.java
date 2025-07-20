package tech.picnic.errorprone.refasterrules;

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

  AbstractAssert<?, ?> testAssertThatIsSameAs() {
    return assertThat("foo" == "bar").isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsNotSameAs() {
    return assertThat("foo" == "bar").isFalse();
  }

  void testAssertThatIsNull() {
    assertThat("foo" == null).isTrue();
  }

  void testAssertThatIsNotNull() {
    assertThat("foo" == null).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatHasSameHashCodeAs() {
    return assertThat("foo".hashCode()).isEqualTo("bar".hashCode());
  }

  AbstractAssert<?, ?> testAssertThatObjectIsInstanceOf() {
    return assertThat(String.class.isInstance("foo")).isTrue();
  }
}
