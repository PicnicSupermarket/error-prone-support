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
        assertThat(ImmutableList.of("baz")).containsSequence("qux"),
        assertThat(ImmutableList.of("quux")).containsSubsequence("corge"));
  }

  ObjectEnumerableAssert<?, String> testObjectEnumerableAssertDoesNotContain() {
    return assertThat(ImmutableList.of("foo")).doesNotContainSequence("bar");
  }

  ImmutableSet<ObjectEnumerableAssert<?, String>> testObjectEnumerableAssertContainsExactly() {
    return ImmutableSet.of(
        assertThat(ImmutableList.of("foo")).containsExactlyInAnyOrder(new String[] {"bar"}),
        assertThat(ImmutableList.of("baz")).containsExactlyInAnyOrder("qux"));
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
        assertThat(ImmutableSet.of("baz"))
            .containsExactlyInAnyOrderElementsOf(ImmutableSet.of("qux")));
  }

  AbstractAssert<?, ?> testAssertThatContainsExactlyInAnyOrderElementsOfMultiset() {
    return assertThat(ImmutableMultiset.of("foo")).isEqualTo(ImmutableMultiset.of("bar"));
  }

  AbstractAssert<?, ?> testAssertThatContainsEntry() {
    return assertThat(ImmutableMap.<String, Object>of("foo", 1).get("bar")).isEqualTo(2);
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsAnyElementsOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .containsAnyElementsOf(ImmutableList.of("bar")),
        assertThat(Stream.of("baz").collect(toImmutableSet()))
            .containsAnyElementsOf(ImmutableList.of("qux")),
        assertThat(Stream.of("quux").collect(toImmutableList()))
            .containsAnyElementsOf(ImmutableList.of("corge")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsAnyOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .containsAnyOf(new String[] {"bar"}),
        assertThat(Stream.of("baz").collect(toImmutableSet())).containsAnyOf(new String[] {"qux"}),
        assertThat(Stream.of("quux").collect(toImmutableList()))
            .containsAnyOf(new String[] {"corge"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsAnyOfVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .containsAnyOf("bar", "baz"),
        assertThat(Stream.of("qux").collect(toImmutableSet())).containsAnyOf("quux", "corge"),
        assertThat(Stream.of("grault").collect(toImmutableList()))
            .containsAnyOf("garply", "waldo"));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsAll() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .containsAll(ImmutableList.of("bar")),
        assertThat(Stream.of("baz").collect(toImmutableSet())).containsAll(ImmutableList.of("qux")),
        assertThat(Stream.of("quux").collect(toImmutableList()))
            .containsAll(ImmutableList.of("corge")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContains() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .contains(new String[] {"bar"}),
        assertThat(Stream.of("baz").collect(toImmutableSet())).contains(new String[] {"qux"}),
        assertThat(Stream.of("quux").collect(toImmutableList())).contains(new String[] {"corge"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .contains("bar", "baz"),
        assertThat(Stream.of("qux").collect(toImmutableSet())).contains("quux", "corge"),
        assertThat(Stream.of("grault").collect(toImmutableList())).contains("garply", "waldo"));
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
        assertThat(Stream.of("baz").collect(toImmutableMultiset()))
            .containsExactlyInAnyOrderElementsOf(ImmutableList.of("qux")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsExactlyInAnyOrder() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .containsExactlyInAnyOrder(new String[] {"bar"}),
        assertThat(Stream.of("baz").collect(toImmutableMultiset()))
            .containsExactlyInAnyOrder(new String[] {"qux"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsExactlyInAnyOrderVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .containsExactlyInAnyOrder("bar", "baz"),
        assertThat(Stream.of("qux").collect(toImmutableMultiset()))
            .containsExactlyInAnyOrder("quux", "corge"));
  }

  ImmutableSet<ListAssert<String>> testAssertThatContainsSequence() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .containsSequence(ImmutableList.of("bar")),
        assertThat(Stream.of("baz").collect(toImmutableList()))
            .containsSequence(new String[] {"qux"}));
  }

  ListAssert<String> testAssertThatContainsSequenceVarargs() {
    return assertThat(Stream.of("foo").collect(toImmutableList())).containsSequence("bar", "baz");
  }

  ImmutableSet<ListAssert<String>> testAssertThatContainsSubsequence() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .containsSubsequence(ImmutableList.of("bar")),
        assertThat(Stream.of("baz").collect(toImmutableList()))
            .containsSubsequence(new String[] {"qux"}));
  }

  ListAssert<String> testAssertThatContainsSubsequenceVarargs() {
    return assertThat(Stream.of("foo").collect(toImmutableList()))
        .containsSubsequence("bar", "baz");
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatDoesNotContainAnyElementsOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .doesNotContainAnyElementsOf(ImmutableList.of("bar")),
        assertThat(Stream.of("baz").collect(toImmutableSet()))
            .doesNotContainAnyElementsOf(ImmutableList.of("qux")),
        assertThat(Stream.of("quux").collect(toImmutableList()))
            .doesNotContainAnyElementsOf(ImmutableList.of("corge")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatDoesNotContain() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .doesNotContain(new String[] {"bar"}),
        assertThat(Stream.of("baz").collect(toImmutableSet())).doesNotContain(new String[] {"qux"}),
        assertThat(Stream.of("quux").collect(toImmutableList()))
            .doesNotContain(new String[] {"corge"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatDoesNotContainVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .doesNotContain("bar", "baz"),
        assertThat(Stream.of("qux").collect(toImmutableSet())).doesNotContain("quux", "corge"),
        assertThat(Stream.of("grault").collect(toImmutableList()))
            .doesNotContain("garply", "waldo"));
  }

  ImmutableSet<ListAssert<String>> testAssertThatDoesNotContainSequence() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect(toImmutableList()))
            .doesNotContainSequence(ImmutableList.of("bar")),
        assertThat(Stream.of("baz").collect(toImmutableList()))
            .doesNotContainSequence(new String[] {"qux"}));
  }

  ListAssert<String> testAssertThatDoesNotContainSequenceVarargs() {
    return assertThat(Stream.of("foo").collect(toImmutableList()))
        .doesNotContainSequence("bar", "baz");
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatHasSameElementsAsStream() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .hasSameElementsAs(ImmutableList.of("bar")),
        assertThat(Stream.of("baz").collect(toImmutableSet()))
            .hasSameElementsAs(ImmutableList.of("qux")),
        assertThat(Stream.of("quux").collect(toImmutableList()))
            .hasSameElementsAs(ImmutableList.of("corge")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsOnly() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .containsOnly(new String[] {"bar"}),
        assertThat(Stream.of("baz").collect(toImmutableSet())).containsOnly(new String[] {"qux"}),
        assertThat(Stream.of("quux").collect(toImmutableList()))
            .containsOnly(new String[] {"corge"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsOnlyVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .containsOnly("bar", "baz"),
        assertThat(Stream.of("qux").collect(toImmutableSet())).containsOnly("quux", "corge"),
        assertThat(Stream.of("grault").collect(toImmutableList())).containsOnly("garply", "waldo"));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsSubsetOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .isSubsetOf(ImmutableList.of("bar")),
        assertThat(Stream.of("baz").collect((Collector<String, Object, Iterable<String>>) null))
            .isSubsetOf(new String[] {"qux"}),
        assertThat(Stream.of("quux").collect(toImmutableSet()))
            .isSubsetOf(ImmutableList.of("corge")),
        assertThat(Stream.of("grault").collect(toImmutableSet()))
            .isSubsetOf(new String[] {"garply"}),
        assertThat(Stream.of("waldo").collect(toImmutableList()))
            .isSubsetOf(ImmutableList.of("fred")),
        assertThat(Stream.of("plugh").collect(toImmutableList()))
            .isSubsetOf(new String[] {"xyzzy"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsSubsetOfVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo").collect((Collector<String, Object, Iterable<String>>) null))
            .isSubsetOf("bar", "baz"),
        assertThat(Stream.of("qux").collect(toImmutableSet())).isSubsetOf("quux", "corge"),
        assertThat(Stream.of("grault").collect(toImmutableList())).isSubsetOf("garply", "waldo"));
  }

  void testAssertThatAccepts() {
    assertThat(((Predicate<String>) String::isEmpty).test("foo")).isTrue();
  }

  void testAssertThatRejects() {
    assertThat(((Predicate<String>) String::isEmpty).test("foo")).isFalse();
  }
}
