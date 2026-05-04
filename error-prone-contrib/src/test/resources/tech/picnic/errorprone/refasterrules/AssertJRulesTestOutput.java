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
    return assertThat(OptionalDouble.of(1.0)).hasValue(2.0);
  }

  AbstractAssert<?, ?> testAssertThatHasValueOptionalInt() {
    return assertThat(OptionalInt.of(1)).hasValue(2);
  }

  AbstractAssert<?, ?> testAssertThatHasValueOptionalLong() {
    return assertThat(OptionalLong.of(1L)).hasValue(2L);
  }

  ImmutableSet<ObjectEnumerableAssert<?, String>> testObjectEnumerableAssertContains() {
    return ImmutableSet.of(
        assertThat(ImmutableList.of("foo")).contains("bar"),
        assertThat(ImmutableList.of("baz")).contains("qux"),
        assertThat(ImmutableList.of("quux")).contains("corge"));
  }

  ObjectEnumerableAssert<?, String> testObjectEnumerableAssertDoesNotContain() {
    return assertThat(ImmutableList.of("foo")).doesNotContain("bar");
  }

  ImmutableSet<ObjectEnumerableAssert<?, String>> testObjectEnumerableAssertContainsExactly() {
    return ImmutableSet.of(
        assertThat(ImmutableList.of("foo")).containsExactlyInAnyOrder(new String[] {"bar"}),
        assertThat(ImmutableList.of("baz")).containsExactly("qux"));
  }

  ObjectEnumerableAssert<?, String> testAssertThatContainsExactlySet() {
    return assertThat(ImmutableSet.of("foo")).containsExactly("bar");
  }

  ListAssert<String> testAssertThatContainsExactlyElementsOfList() {
    return assertThat(ImmutableList.of("foo")).containsExactlyElementsOf(ImmutableList.of("bar"));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatHasSameElementsAsSet() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of("foo")).hasSameElementsAs(ImmutableSet.of("bar")),
        assertThat(ImmutableSet.of("baz")).hasSameElementsAs(ImmutableSet.of("qux")));
  }

  AbstractAssert<?, ?> testAssertThatContainsExactlyInAnyOrderElementsOfMultiset() {
    return assertThat(ImmutableMultiset.of("foo"))
        .containsExactlyInAnyOrderElementsOf(ImmutableMultiset.of("bar"));
  }

  AbstractAssert<?, ?> testAssertThatContainsEntry() {
    return assertThat(ImmutableMap.<String, Object>of("foo", 1)).containsEntry("bar", 2);
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsAnyElementsOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsAnyElementsOf(ImmutableList.of("bar")),
        assertThat(Stream.of("baz")).containsAnyElementsOf(ImmutableList.of("qux")),
        assertThat(Stream.of("quux")).containsAnyElementsOf(ImmutableList.of("corge")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsAnyOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsAnyOf(new String[] {"bar"}),
        assertThat(Stream.of("baz")).containsAnyOf(new String[] {"qux"}),
        assertThat(Stream.of("quux")).containsAnyOf(new String[] {"corge"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsAnyOfVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsAnyOf("bar", "baz"),
        assertThat(Stream.of("qux")).containsAnyOf("quux", "corge"),
        assertThat(Stream.of("grault")).containsAnyOf("garply", "waldo"));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsAll() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsAll(ImmutableList.of("bar")),
        assertThat(Stream.of("baz")).containsAll(ImmutableList.of("qux")),
        assertThat(Stream.of("quux")).containsAll(ImmutableList.of("corge")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContains() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).contains(new String[] {"bar"}),
        assertThat(Stream.of("baz")).contains(new String[] {"qux"}),
        assertThat(Stream.of("quux")).contains(new String[] {"corge"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).contains("bar", "baz"),
        assertThat(Stream.of("qux")).contains("quux", "corge"),
        assertThat(Stream.of("grault")).contains("garply", "waldo"));
  }

  ListAssert<String> testAssertThatContainsExactlyElementsOfStream() {
    return assertThat(Stream.of("foo")).containsExactlyElementsOf(ImmutableList.of("bar"));
  }

  ListAssert<String> testAssertThatContainsExactlyStream() {
    return assertThat(Stream.of("foo")).containsExactly(new String[] {"bar"});
  }

  ListAssert<String> testAssertThatContainsExactlyVarargs() {
    return assertThat(Stream.of("foo")).containsExactly("bar", "baz");
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsExactlyInAnyOrderElementsOfStream() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsExactlyInAnyOrderElementsOf(ImmutableList.of("bar")),
        assertThat(Stream.of("baz")).containsExactlyInAnyOrderElementsOf(ImmutableList.of("qux")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsExactlyInAnyOrder() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsExactlyInAnyOrder(new String[] {"bar"}),
        assertThat(Stream.of("baz")).containsExactlyInAnyOrder(new String[] {"qux"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsExactlyInAnyOrderVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsExactlyInAnyOrder("bar", "baz"),
        assertThat(Stream.of("qux")).containsExactlyInAnyOrder("quux", "corge"));
  }

  ImmutableSet<ListAssert<String>> testAssertThatContainsSequence() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsSequence(ImmutableList.of("bar")),
        assertThat(Stream.of("baz")).containsSequence(new String[] {"qux"}));
  }

  ListAssert<String> testAssertThatContainsSequenceVarargs() {
    return assertThat(Stream.of("foo")).containsSequence("bar", "baz");
  }

  ImmutableSet<ListAssert<String>> testAssertThatContainsSubsequence() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsSubsequence(ImmutableList.of("bar")),
        assertThat(Stream.of("baz")).containsSubsequence(new String[] {"qux"}));
  }

  ListAssert<String> testAssertThatContainsSubsequenceVarargs() {
    return assertThat(Stream.of("foo")).containsSubsequence("bar", "baz");
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatDoesNotContainAnyElementsOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).doesNotContainAnyElementsOf(ImmutableList.of("bar")),
        assertThat(Stream.of("baz")).doesNotContainAnyElementsOf(ImmutableList.of("qux")),
        assertThat(Stream.of("quux")).doesNotContainAnyElementsOf(ImmutableList.of("corge")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatDoesNotContain() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).doesNotContain(new String[] {"bar"}),
        assertThat(Stream.of("baz")).doesNotContain(new String[] {"qux"}),
        assertThat(Stream.of("quux")).doesNotContain(new String[] {"corge"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatDoesNotContainVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).doesNotContain("bar", "baz"),
        assertThat(Stream.of("qux")).doesNotContain("quux", "corge"),
        assertThat(Stream.of("grault")).doesNotContain("garply", "waldo"));
  }

  ImmutableSet<ListAssert<String>> testAssertThatDoesNotContainSequence() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).doesNotContainSequence(ImmutableList.of("bar")),
        assertThat(Stream.of("baz")).doesNotContainSequence(new String[] {"qux"}));
  }

  ListAssert<String> testAssertThatDoesNotContainSequenceVarargs() {
    return assertThat(Stream.of("foo")).doesNotContainSequence("bar", "baz");
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatHasSameElementsAsStream() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).hasSameElementsAs(ImmutableList.of("bar")),
        assertThat(Stream.of("baz")).hasSameElementsAs(ImmutableList.of("qux")),
        assertThat(Stream.of("quux")).hasSameElementsAs(ImmutableList.of("corge")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsOnly() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsOnly(new String[] {"bar"}),
        assertThat(Stream.of("baz")).containsOnly(new String[] {"qux"}),
        assertThat(Stream.of("quux")).containsOnly(new String[] {"corge"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsOnlyVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsOnly("bar", "baz"),
        assertThat(Stream.of("qux")).containsOnly("quux", "corge"),
        assertThat(Stream.of("grault")).containsOnly("garply", "waldo"));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsSubsetOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).isSubsetOf(ImmutableList.of("bar")),
        assertThat(Stream.of("baz")).isSubsetOf(new String[] {"qux"}),
        assertThat(Stream.of("quux")).isSubsetOf(ImmutableList.of("corge")),
        assertThat(Stream.of("grault")).isSubsetOf(new String[] {"garply"}),
        assertThat(Stream.of("waldo")).isSubsetOf(ImmutableList.of("fred")),
        assertThat(Stream.of("plugh")).isSubsetOf(new String[] {"xyzzy"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsSubsetOfVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).isSubsetOf("bar", "baz"),
        assertThat(Stream.of("qux")).isSubsetOf("quux", "corge"),
        assertThat(Stream.of("grault")).isSubsetOf("garply", "waldo"));
  }

  void testAssertThatAccepts() {
    assertThat(((Predicate<String>) String::isEmpty)).accepts("foo");
  }

  void testAssertThatRejects() {
    assertThat(((Predicate<String>) String::isEmpty)).rejects("foo");
  }
}
