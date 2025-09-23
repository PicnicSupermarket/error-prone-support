package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.stream.Stream;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ListAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJStreamRulesTest implements RefasterRuleCollectionTestCase {
  ListAssert<Integer> testAssertThatFilteredOn() {
    return assertThat(Stream.of(1).filter(i -> i > 2));
  }

  void testAssertThatNoneMatch() {
    assertThat(Stream.of(1)).filteredOn(i -> i > 2).isEmpty();
    assertThat(Stream.of(3).anyMatch(i -> i > 4)).isFalse();
    assertThat(Stream.of(5).noneMatch(i -> i > 6)).isTrue();
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatAnyMatch() {
    return ImmutableSet.of(
        assertThat(Stream.of(1)).filteredOn(i -> i > 2).isNotEmpty(),
        assertThat(Stream.of(3).anyMatch(i -> i > 4)).isTrue(),
        assertThat(Stream.of(5).noneMatch(i -> i > 6)).isFalse());
  }

  AbstractAssert<?, ?> testAssertThatCollection() {
    return assertThat(ImmutableSet.of("foo").stream());
  }
}
