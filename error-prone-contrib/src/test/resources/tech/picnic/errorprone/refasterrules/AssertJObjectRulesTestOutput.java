package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJObjectRulesTest implements RefasterRuleCollectionTestCase {
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

  AbstractAssert<?, ?> testAssertThatIsSameAs() {
    return assertThat("foo").isSameAs("bar");
  }

  AbstractAssert<?, ?> testAssertThatIsNotSameAs() {
    return assertThat("foo").isNotSameAs("bar");
  }

  void testAssertThatIsNull() {
    assertThat("foo").isNull();
  }

  void testAssertThatIsNotNull() {
    assertThat("foo").isNotNull();
  }

  AbstractAssert<?, ?> testAssertThatHasSameHashCodeAs() {
    return assertThat("foo").hasSameHashCodeAs("bar");
  }

  AbstractAssert<?, ?> testAssertThatObjectIsInstanceOf() {
    return assertThat("foo").isInstanceOf(String.class);
  }
}
