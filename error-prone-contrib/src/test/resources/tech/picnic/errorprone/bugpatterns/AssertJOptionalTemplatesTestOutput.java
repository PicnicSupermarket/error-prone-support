package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.OptionalAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJOptionalTemplatesTest implements RefasterTemplateTestCase {
  AbstractAssert<?, ?> testAssertThatOptional() {
    return assertThat(Optional.of(new Object())).get();
  }

  ImmutableSet<OptionalAssert<Integer>> testAbstractOptionalAssertIsPresent() {
    return ImmutableSet.of(
        assertThat(Optional.of(1)).isPresent(), assertThat(Optional.of(2)).isPresent());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatOptionalIsPresent() {
    return ImmutableSet.of(
        assertThat(Optional.of(1)).isPresent(), assertThat(Optional.of(2)).isPresent());
  }

  ImmutableSet<OptionalAssert<Integer>> testAbstractOptionalAssertIsEmpty() {
    return ImmutableSet.of(
        assertThat(Optional.of(1)).isEmpty(), assertThat(Optional.of(2)).isEmpty());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatOptionalIsEmpty() {
    return ImmutableSet.of(
        assertThat(Optional.of(1)).isEmpty(), assertThat(Optional.of(2)).isEmpty());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractOptionalAssertHasValue() {
    return ImmutableSet.of(
        assertThat(Optional.of(1)).hasValue(1),
        assertThat(Optional.of(2)).hasValue(2),
        assertThat(Optional.of(3)).hasValue(3),
        assertThat(Optional.of(4)).hasValue(4));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractOptionalAssertContainsSame() {
    return ImmutableSet.of(
        assertThat(Optional.of(1)).containsSame(1), assertThat(Optional.of(2)).containsSame(2));
  }

  AbstractAssert<?, ?> testAssertThatOptionalHasValueMatching() {
    return assertThat(Optional.of("foo")).get().matches(String::isEmpty);
  }
}
