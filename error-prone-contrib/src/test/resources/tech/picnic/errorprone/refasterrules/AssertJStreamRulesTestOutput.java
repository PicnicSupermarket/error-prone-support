package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.stream.Collector;
import java.util.stream.Stream;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ListAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJStreamRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Collector.class, toImmutableList(), toImmutableSet());
  }

  void testAssertThatIsEmpty() {
    assertThat(Stream.of(1)).isEmpty();
    assertThat(Stream.of(2)).isEmpty();
    assertThat(Stream.of(3)).isEmpty();
    assertThat(Stream.of(4)).isEmpty();
    assertThat(Stream.of(5)).isEmpty();
    assertThat(Stream.of(6)).isEmpty();
    assertThat(Stream.of(7)).isEmpty();
    assertThat(Stream.of(8)).isEmpty();
  }

  void testAssertThatIsNotEmpty() {
    assertThat(Stream.of(1)).isNotEmpty();
    assertThat(Stream.of(2)).isNotEmpty();
    assertThat(Stream.of(3)).isNotEmpty();
    assertThat(Stream.of(4)).isNotEmpty();
    assertThat(Stream.of(5)).isNotEmpty();
    assertThat(Stream.of(6)).isNotEmpty();
    assertThat(Stream.of(7)).isNotEmpty();
    assertThat(Stream.of(8)).isNotEmpty();
    assertThat(Stream.of(9)).isNotEmpty();
    assertThat(Stream.of(10)).isNotEmpty();
  }

  AbstractAssert<?, ?> testAssertThatHasSize() {
    return assertThat(Stream.of(1)).hasSize(2);
  }

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
