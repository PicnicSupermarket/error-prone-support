package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.stream.Stream;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJStreamRulesTest implements RefasterRuleCollectionTestCase {
  AbstractAssert<?, ?> testAssertThatFilteredOn() {
    return assertThat(Stream.of(1).filter(i -> i > 0));
  }

  void testAssertThatNoneMatch() {
    assertThat(Stream.of(1)).filteredOn(i -> i > 0).isEmpty();
    assertThat(Stream.of(2).anyMatch(i -> i > 0)).isFalse();
    assertThat(Stream.of(3).noneMatch(i -> i > 0)).isTrue();
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatAnyMatch() {
    return ImmutableSet.of(
        assertThat(Stream.of(1)).filteredOn(i -> i > 0).isNotEmpty(),
        assertThat(Stream.of(2).anyMatch(i -> i > 0)).isTrue(),
        assertThat(Stream.of(3).noneMatch(i -> i > 0)).isFalse());
  }

  AbstractAssert<?, ?> testAssertThatCollection() {
    return assertThat(ImmutableSet.of("a").stream());
  }
}
