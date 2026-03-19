package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMultiset.toImmutableMultiset;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectEnumerableAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Collector.class, toImmutableList(), toImmutableMultiset(), toImmutableSet());
  }

  AbstractAssert<?, ?> testAssertThatHasValueOptionalDouble() {
    return assertThat(OptionalDouble.of(1.0).getAsDouble()).isEqualTo(2.0);
  }

  AbstractAssert<?, ?> testAssertThatHasValueOptionalInt() {
    return assertThat(OptionalInt.of(1).getAsInt()).isEqualTo(2);
  }

  AbstractAssert<?, ?> testAssertThatHasValueOptionalLong() {
    return assertThat(OptionalLong.of(1L).getAsLong()).isEqualTo(2L);
  }

  ImmutableSet<ObjectEnumerableAssert<?, String>> testObjectEnumerableAssertContains() {
    return ImmutableSet.of(
        assertThat(ImmutableList.of("foo")).containsAnyOf("bar"),
        assertThat(ImmutableList.of("foo")).containsSequence("bar"),
        assertThat(ImmutableList.of("foo")).containsSubsequence("bar"));
  }

  ObjectEnumerableAssert<?, String> testObjectEnumerableAssertDoesNotContain() {
    return assertThat(ImmutableList.of("foo")).doesNotContainSequence("bar");
  }

  ImmutableSet<ObjectEnumerableAssert<?, String>> testObjectEnumerableAssertContainsExactly() {
    return ImmutableSet.of(
        assertThat(ImmutableList.of("foo")).containsExactlyInAnyOrder(new String[] {"bar"}),
        assertThat(ImmutableList.of("foo")).containsExactlyInAnyOrder("bar"));
  }

  ObjectEnumerableAssert<?, String> testAssertThatContainsExactlySet() {
    return assertThat(ImmutableSet.of("foo")).containsOnly("bar");
  }

  ListAssert<String> testAssertThatContainsExactlyElementsOfList() {
    return assertThat(ImmutableList.of("foo")).isEqualTo(ImmutableList.of("bar"));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatHasSameElementsAsSet() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of("foo")).isEqualTo(ImmutableSet.of("bar")),
        assertThat(ImmutableSet.of("foo"))
            .containsExactlyInAnyOrderElementsOf(ImmutableSet.of("bar")));
  }

  AbstractAssert<?, ?> testAssertThatContainsExactlyInAnyOrderElementsOfMultiset() {
    return assertThat(ImmutableMultiset.of("foo")).isEqualTo(ImmutableMultiset.of("bar"));
  }

  AbstractAssert<?, ?> testAssertThatContainsEntry() {
    return assertThat(ImmutableMap.of("foo", 1).get("bar")).isEqualTo(2);
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsAnyElementsOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .containsAnyElementsOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo").collect(toImmutableSet()))
            .containsAnyElementsOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .containsAnyElementsOf(ImmutableList.of("bar")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsAnyOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .containsAnyOf(new String[] {"bar"}),
        assertThat(Stream.of("foo").collect(toImmutableSet())).containsAnyOf(new String[] {"bar"}),
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .containsAnyOf(new String[] {"bar"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsAnyOfVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .containsAnyOf("bar", "baz"),
        assertThat(Stream.of("foo").collect(toImmutableSet())).containsAnyOf("bar", "baz"),
        assertThat(Stream.of("foo").collect(toImmutableList())).containsAnyOf("bar", "baz"));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsAll() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .containsAll(ImmutableList.of("bar")),
        assertThat(Stream.of("foo").collect(toImmutableSet())).containsAll(ImmutableList.of("bar")),
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .containsAll(ImmutableList.of("bar")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContains() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .contains(new String[] {"bar"}),
        assertThat(Stream.of("foo").collect(toImmutableSet())).contains(new String[] {"bar"}),
        assertThat(Stream.of("foo").collect(toImmutableList())).contains(new String[] {"bar"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .contains("bar", "baz"),
        assertThat(Stream.of("foo").collect(toImmutableSet())).contains("bar", "baz"),
        assertThat(Stream.of("foo").collect(toImmutableList())).contains("bar", "baz"));
  }

  ListAssert<String> testAssertThatContainsExactlyElementsOfStream() {
    return assertThat(Stream.of("foo").collect(toImmutableList()))
        .containsExactlyElementsOf(ImmutableList.of("bar"));
  }

  ListAssert<String> testAssertThatContainsExactlyStream() {
    return assertThat(Stream.of("foo").collect(toImmutableList()))
        .containsExactly(new String[] {"bar"});
  }

  ListAssert<String> testAssertThatContainsExactlyVarargs() {
    return assertThat(Stream.of("foo").collect(toImmutableList())).containsExactly("bar", "baz");
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsExactlyInAnyOrderElementsOfStream() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .containsExactlyInAnyOrderElementsOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo").collect(toImmutableMultiset()))
            .containsExactlyInAnyOrderElementsOf(ImmutableList.of("bar")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsExactlyInAnyOrder() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .containsExactlyInAnyOrder(new String[] {"bar"}),
        assertThat(Stream.of("foo").collect(toImmutableMultiset()))
            .containsExactlyInAnyOrder(new String[] {"bar"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsExactlyInAnyOrderVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .containsExactlyInAnyOrder("bar", "baz"),
        assertThat(Stream.of("foo").collect(toImmutableMultiset()))
            .containsExactlyInAnyOrder("bar", "baz"));
  }

  ImmutableSet<ListAssert<String>> testAssertThatContainsSequence() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .containsSequence(ImmutableList.of("bar")),
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .containsSequence(new String[] {"bar"}));
  }

  ListAssert<String> testAssertThatContainsSequenceVarargs() {
    return assertThat(Stream.of("foo").collect(toImmutableList())).containsSequence("bar", "baz");
  }

  ImmutableSet<ListAssert<String>> testAssertThatContainsSubsequence() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .containsSubsequence(ImmutableList.of("bar")),
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .containsSubsequence(new String[] {"bar"}));
  }

  ListAssert<String> testAssertThatContainsSubsequenceVarargs() {
    return assertThat(Stream.of("foo").collect(toImmutableList()))
        .containsSubsequence("bar", "baz");
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatDoesNotContainAnyElementsOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .doesNotContainAnyElementsOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo").collect(toImmutableSet()))
            .doesNotContainAnyElementsOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .doesNotContainAnyElementsOf(ImmutableList.of("bar")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatDoesNotContain() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .doesNotContain(new String[] {"bar"}),
        assertThat(Stream.of("foo").collect(toImmutableSet())).doesNotContain(new String[] {"bar"}),
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .doesNotContain(new String[] {"bar"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatDoesNotContainVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .doesNotContain("bar", "baz"),
        assertThat(Stream.of("foo").collect(toImmutableSet())).doesNotContain("bar", "baz"),
        assertThat(Stream.of("foo").collect(toImmutableList())).doesNotContain("bar", "baz"));
  }

  ImmutableSet<ListAssert<String>> testAssertThatDoesNotContainSequence() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .doesNotContainSequence(ImmutableList.of("bar")),
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .doesNotContainSequence(new String[] {"bar"}));
  }

  ListAssert<String> testAssertThatDoesNotContainSequenceVarargs() {
    return assertThat(Stream.of("foo").collect(toImmutableList()))
        .doesNotContainSequence("bar", "baz");
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatHasSameElementsAsStream() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .hasSameElementsAs(ImmutableList.of("bar")),
        assertThat(Stream.of("foo").collect(toImmutableSet()))
            .hasSameElementsAs(ImmutableList.of("bar")),
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .hasSameElementsAs(ImmutableList.of("bar")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsOnly() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .containsOnly(new String[] {"bar"}),
        assertThat(Stream.of("foo").collect(toImmutableSet())).containsOnly(new String[] {"bar"}),
        assertThat(Stream.of("foo").collect(toImmutableList())).containsOnly(new String[] {"bar"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsOnlyVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .containsOnly("bar", "baz"),
        assertThat(Stream.of("foo").collect(toImmutableSet())).containsOnly("bar", "baz"),
        assertThat(Stream.of("foo").collect(toImmutableList())).containsOnly("bar", "baz"));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsSubsetOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .isSubsetOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .isSubsetOf(new String[] {"bar"}),
        assertThat(Stream.of("foo").collect(toImmutableSet())).isSubsetOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo").collect(toImmutableSet())).isSubsetOf(new String[] {"bar"}),
        assertThat(Stream.of("foo").collect(toImmutableList())).isSubsetOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo").collect(toImmutableList())).isSubsetOf(new String[] {"bar"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsSubsetOfVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .isSubsetOf("bar", "baz"),
        assertThat(Stream.of("foo").collect(toImmutableSet())).isSubsetOf("bar", "baz"),
        assertThat(Stream.of("foo").collect(toImmutableList())).isSubsetOf("bar", "baz"));
  }

  void testAssertThatAccepts() {
    assertThat(((Predicate<String>) String::isEmpty).test("foo")).isTrue();
  }

  void testAssertThatRejects() {
    assertThat(((Predicate<String>) String::isEmpty).test("foo")).isFalse();
  }
}
