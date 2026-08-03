package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJObjectRulesTest implements RefasterRuleCollectionTestCase {
  AbstractAssert<?, ?> testAssertThatIsInstanceOfClass() {
    return assertThat("foo" instanceof String).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsInstanceOf() {
    return assertThat(String.class.isInstance("foo")).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsNotInstanceOfClass() {
    return assertThat("foo" instanceof String).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatIsEqualTo() {
    return assertThat("foo".equals("bar")).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsNotEqualTo() {
    return assertThat("foo".equals("bar")).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatHasToString() {
    return assertThat(new Object().toString()).isEqualTo("foo");
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsSameAs() {
    return ImmutableSet.of(
        assertThat(null == "foo").isTrue(),
        assertThat("bar" == "baz").isTrue(),
        assertThat(null != "qux").isFalse(),
        assertThat("quux" != "corge").isFalse());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsNotSameAs() {
    return ImmutableSet.of(
        assertThat(null == "foo").isFalse(),
        assertThat("bar" == "baz").isFalse(),
        assertThat(null != "qux").isTrue(),
        assertThat("quux" != "corge").isTrue());
  }

  AbstractAssert<?, ?> testAssertThatHasSameHashCodeAs() {
    return assertThat("foo".hashCode()).isEqualTo("bar".hashCode());
  }
}
