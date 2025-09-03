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
    return assertThat(Stream.of(1, 2, 3)).filteredOn(pred);
  }

  void testAssertThatStreamNoneMatch() {
    Predicate<Integer> pred = i -> i > 0;
    assertThat(Stream.of(1, 2, 3)).noneMatch(pred);
    assertThat(Stream.of(1, 2, 3)).noneMatch(pred);
    assertThat(Stream.of(1, 2, 3)).noneMatch(pred);
  }

  void testAssertThatStreamAnyMatch() {
    Predicate<Integer> pred = i -> i > 0;
    assertThat(Stream.of(1, 2, 3)).anyMatch(pred);
    assertThat(Stream.of(1, 2, 3)).anyMatch(pred);
    assertThat(Stream.of(1, 2, 3)).anyMatch(pred);
  }

  AbstractAssert<?, ?> testAssertThatCollectionStream() {
    return assertThat(ImmutableSet.of("a", "b"));
  }
}
