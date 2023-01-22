package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

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

  AbstractAssert<?, ?> testAbstractAssertAsInstanceOfMatches() {
    return assertThat((Object) new IllegalStateException())
        .asInstanceOf(type(RuntimeException.class))
        .matches(t -> !(t).toString().isEmpty());
  }
}
