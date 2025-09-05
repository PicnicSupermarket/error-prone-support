package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.stream.Stream;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJStreamRulesTest implements RefasterRuleCollectionTestCase {
  AbstractAssert<?, ?> testAssertThatFilteredOn() {
    return assertThat(Stream.of(1)).filteredOn(i -> i > 0);
  }

  void testAssertThatNoneMatch() {
    assertThat(Stream.of(1)).noneMatch(i -> i > 0);
    assertThat(Stream.of(2)).noneMatch(i -> i > 0);
    assertThat(Stream.of(3)).noneMatch(i -> i > 0);
  }

  void testAssertThatAnyMatch() {
    assertThat(Stream.of(1)).anyMatch(i -> i > 0);
    assertThat(Stream.of(2)).anyMatch(i -> i > 0);
    assertThat(Stream.of(3)).anyMatch(i -> i > 0);
  }

  AbstractAssert<?, ?> testAssertThatCollection() {
    return assertThat(ImmutableSet.of("a"));
  }
}
