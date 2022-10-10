package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.OptionalAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJOptionalTemplatesTest implements RefasterRuleCollectionTestCase {
  AbstractAssert<?, ?> testAssertThatOptional() {
    return assertThat(Optional.of(new Object()).orElseThrow());
  }

  ImmutableSet<OptionalAssert<Integer>> testAbstractOptionalAssertIsPresent() {
    return ImmutableSet.of(
        assertThat(Optional.of(1)).isNotEmpty(),
        assertThat(Optional.of(2)).isNotEqualTo(Optional.empty()));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatOptionalIsPresent() {
    return ImmutableSet.of(
        assertThat(Optional.of(1).isPresent()).isTrue(),
        assertThat(Optional.of(2).isEmpty()).isFalse());
  }

  ImmutableSet<OptionalAssert<Integer>> testAbstractOptionalAssertIsEmpty() {
    return ImmutableSet.of(
        assertThat(Optional.of(1)).isNotPresent(),
        assertThat(Optional.of(2)).isEqualTo(Optional.empty()));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatOptionalIsEmpty() {
    return ImmutableSet.of(
        assertThat(Optional.of(1).isEmpty()).isTrue(),
        assertThat(Optional.of(2).isPresent()).isFalse());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractOptionalAssertHasValue() {
    return ImmutableSet.of(
        assertThat(Optional.of(1)).get().isEqualTo(1),
        assertThat(Optional.of(2)).isEqualTo(Optional.of(2)),
        assertThat(Optional.of(3)).contains(3),
        assertThat(Optional.of(4)).isPresent().hasValue(4));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractOptionalAssertContainsSame() {
    return ImmutableSet.of(
        assertThat(Optional.of(1)).get().isSameAs(1),
        assertThat(Optional.of(2)).isPresent().isSameAs(2));
  }

  AbstractAssert<?, ?> testAssertThatOptionalHasValueMatching() {
    return assertThat(Optional.of("foo").filter(String::isEmpty)).isPresent();
  }
}
