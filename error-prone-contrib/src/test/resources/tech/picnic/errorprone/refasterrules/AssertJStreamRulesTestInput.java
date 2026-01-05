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
    assertThat(Stream.of(1).findAny()).isEmpty();
    assertThat(Stream.of(2).findFirst()).isEmpty();
    assertThat(Stream.of(3).toArray()).isEmpty();
    assertThat(Stream.of(4).toArray(Integer[]::new)).isEmpty();
    assertThat(Stream.of(5).toList()).isEmpty();
    assertThat(Stream.of(6).collect((Collector<Integer, Object, Iterable<Object>>) null)).isEmpty();
    assertThat(Stream.of(7).collect(toImmutableSet())).isEmpty();
    assertThat(Stream.of(8).collect(toImmutableList())).isEmpty();
  }

  void testAssertThatIsNotEmpty() {
    assertThat(Stream.of(1).count()).isNotEqualTo(0);
    assertThat(Stream.of(2).count()).isNotEqualTo(0L);
    assertThat(Stream.of(3).findAny()).isPresent();
    assertThat(Stream.of(4).findFirst()).isPresent();
    assertThat(Stream.of(5).toArray()).isNotEmpty();
    assertThat(Stream.of(6).toArray(Integer[]::new)).isNotEmpty();
    assertThat(Stream.of(7).toList()).isNotEmpty();
    assertThat(Stream.of(8).collect((Collector<Integer, Object, Iterable<Object>>) null))
        .isNotEmpty();
    assertThat(Stream.of(9).collect(toImmutableSet())).isNotEmpty();
    assertThat(Stream.of(10).collect(toImmutableList())).isNotEmpty();
  }

  AbstractAssert<?, ?> testAssertThatHasSize() {
    return assertThat(Stream.of(1).count()).isEqualTo(2);
  }

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
