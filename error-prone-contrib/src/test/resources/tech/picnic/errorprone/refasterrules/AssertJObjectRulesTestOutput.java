package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJObjectRulesTest implements RefasterRuleCollectionTestCase {
  AbstractAssert<?, ?> testAssertThatIsInstanceOf() {
    return assertThat("foo").isInstanceOf(String.class);
  }

  AbstractAssert<?, ?> testAssertThatIsInstanceOf2() {
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

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsSameAs() {
    return ImmutableSet.of(assertThat("foo").isSameAs("bar"), assertThat(0).isSameAs(1));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsNotSameAs() {
    return ImmutableSet.of(assertThat("foo").isNotSameAs("bar"), assertThat(0).isNotSameAs(1));
  }

  void testAssertThatIsNull() {
    assertThat("foo").isNull();
    assertThat("bar").isNull();
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsNotNull() {
    return ImmutableSet.of(assertThat("foo").isNotNull(), assertThat("bar").isNotNull());
  }

  AbstractAssert<?, ?> testAssertThatHasSameHashCodeAs() {
    return assertThat("foo").hasSameHashCodeAs("bar");
  }
}
