package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJStreamRulesTest implements RefasterRuleCollectionTestCase {
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of();
  }

  AbstractAssert<?, ?> testAssertThatStreamFilteredOn() {
    Predicate<Integer> pred = i -> i > 0;
    return assertThat(Stream.of(1, 2, 3).filter(pred));
  }

  void testAssertThatStreamNoneMatch() {
    Predicate<Integer> pred = i -> i > 0;
    assertThat(Stream.of(1, 2, 3)).filteredOn(pred).isEmpty();
    assertThat(Stream.of(1, 2, 3).anyMatch(pred)).isFalse();
    assertThat(Stream.of(1, 2, 3).noneMatch(pred)).isTrue();
  }

  void testAssertThatStreamAnyMatch() {
    Predicate<Integer> pred = i -> i > 0;
    assertThat(Stream.of(1, 2, 3)).filteredOn(pred).isNotEmpty();
    assertThat(Stream.of(1, 2, 3).anyMatch(pred)).isTrue();
    assertThat(Stream.of(1, 2, 3).noneMatch(pred)).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatCollectionStream() {
    return assertThat(ImmutableSet.of("a", "b").stream());
  }

  void testAssertThatStreamCountZero() {
    assertThat(Stream.of().count()).isEqualTo(0);
    assertThat(Stream.of()).hasSize(0);
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatStreamSingleElement() {
    return ImmutableSet.of(
        assertThat(Stream.of(42).count()).isEqualTo(1), assertThat(Stream.of(43)).hasSize(1));
  }
}
