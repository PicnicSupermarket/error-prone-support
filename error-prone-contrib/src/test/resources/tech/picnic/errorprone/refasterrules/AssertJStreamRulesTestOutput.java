package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.stream.Stream;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ListAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJStreamRulesTest implements RefasterRuleCollectionTestCase {
  ListAssert<Integer> testAssertThatFilteredOn() {
    return assertThat(Stream.of(1)).filteredOn(i -> i > 2);
  }

  void testAssertThatNoneMatch() {
    assertThat(Stream.of(1)).noneMatch(i -> i > 2);
    assertThat(Stream.of(3)).noneMatch(i -> i > 4);
    assertThat(Stream.of(5)).noneMatch(i -> i > 6);
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatAnyMatch() {
    return ImmutableSet.of(
        assertThat(Stream.of(1)).anyMatch(i -> i > 2),
        assertThat(Stream.of(3)).anyMatch(i -> i > 4),
        assertThat(Stream.of(5)).anyMatch(i -> i > 6));
  }

  AbstractAssert<?, ?> testAssertThatCollection() {
    return assertThat(ImmutableSet.of("foo"));
  }
}
