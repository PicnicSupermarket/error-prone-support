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
        assertThat(ImmutableList.of("foo")).contains("bar"),
        assertThat(ImmutableList.of("foo")).contains("bar"));
  }

  ObjectEnumerableAssert<?, String> testObjectEnumerableAssertDoesNotContain() {
    return assertThat(ImmutableList.of("foo")).doesNotContain("bar");
  }

  ImmutableSet<ObjectEnumerableAssert<?, String>> testObjectEnumerableAssertContainsExactly() {
    return ImmutableSet.of(
        assertThat(ImmutableList.of("foo")).containsExactlyInAnyOrder(new String[] {"bar"}),
        assertThat(ImmutableList.of("foo")).containsExactly("bar"));
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
        assertThat(ImmutableSet.of("foo")).hasSameElementsAs(ImmutableSet.of("bar")));
  }

  AbstractAssert<?, ?> testAssertThatContainsExactlyInAnyOrderElementsOfMultiset() {
    return assertThat(ImmutableMultiset.of("foo"))
        .containsExactlyInAnyOrderElementsOf(ImmutableMultiset.of("bar"));
  }

  AbstractAssert<?, ?> testAssertThatContainsEntry() {
    return assertThat(ImmutableMap.of("foo", 1)).containsEntry("bar", 2);
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsAnyElementsOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsAnyElementsOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).containsAnyElementsOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).containsAnyElementsOf(ImmutableList.of("bar")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsAnyOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsAnyOf(new String[] {"bar"}),
        assertThat(Stream.of("foo")).containsAnyOf(new String[] {"bar"}),
        assertThat(Stream.of("foo")).containsAnyOf(new String[] {"bar"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsAnyOfVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsAnyOf("bar", "baz"),
        assertThat(Stream.of("foo")).containsAnyOf("bar", "baz"),
        assertThat(Stream.of("foo")).containsAnyOf("bar", "baz"));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsAll() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsAll(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).containsAll(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).containsAll(ImmutableList.of("bar")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContains() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).contains(new String[] {"bar"}),
        assertThat(Stream.of("foo")).contains(new String[] {"bar"}),
        assertThat(Stream.of("foo")).contains(new String[] {"bar"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).contains("bar", "baz"),
        assertThat(Stream.of("foo")).contains("bar", "baz"),
        assertThat(Stream.of("foo")).contains("bar", "baz"));
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
        assertThat(Stream.of("foo")).containsExactlyInAnyOrderElementsOf(ImmutableList.of("bar")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsExactlyInAnyOrder() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsExactlyInAnyOrder(new String[] {"bar"}),
        assertThat(Stream.of("foo")).containsExactlyInAnyOrder(new String[] {"bar"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsExactlyInAnyOrderVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsExactlyInAnyOrder("bar", "baz"),
        assertThat(Stream.of("foo")).containsExactlyInAnyOrder("bar", "baz"));
  }

  ImmutableSet<ListAssert<String>> testAssertThatContainsSequence() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsSequence(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).containsSequence(new String[] {"bar"}));
  }

  ListAssert<String> testAssertThatContainsSequenceVarargs() {
    return assertThat(Stream.of("foo")).containsSequence("bar", "baz");
  }

  ImmutableSet<ListAssert<String>> testAssertThatContainsSubsequence() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsSubsequence(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).containsSubsequence(new String[] {"bar"}));
  }

  ListAssert<String> testAssertThatContainsSubsequenceVarargs() {
    return assertThat(Stream.of("foo")).containsSubsequence("bar", "baz");
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatDoesNotContainAnyElementsOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).doesNotContainAnyElementsOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).doesNotContainAnyElementsOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).doesNotContainAnyElementsOf(ImmutableList.of("bar")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatDoesNotContain() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).doesNotContain(new String[] {"bar"}),
        assertThat(Stream.of("foo")).doesNotContain(new String[] {"bar"}),
        assertThat(Stream.of("foo")).doesNotContain(new String[] {"bar"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatDoesNotContainVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).doesNotContain("bar", "baz"),
        assertThat(Stream.of("foo")).doesNotContain("bar", "baz"),
        assertThat(Stream.of("foo")).doesNotContain("bar", "baz"));
  }

  ImmutableSet<ListAssert<String>> testAssertThatDoesNotContainSequence() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).doesNotContainSequence(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).doesNotContainSequence(new String[] {"bar"}));
  }

  ListAssert<String> testAssertThatDoesNotContainSequenceVarargs() {
    return assertThat(Stream.of("foo")).doesNotContainSequence("bar", "baz");
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatHasSameElementsAsStream() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).hasSameElementsAs(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).hasSameElementsAs(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).hasSameElementsAs(ImmutableList.of("bar")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsOnly() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsOnly(new String[] {"bar"}),
        assertThat(Stream.of("foo")).containsOnly(new String[] {"bar"}),
        assertThat(Stream.of("foo")).containsOnly(new String[] {"bar"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatContainsOnlyVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsOnly("bar", "baz"),
        assertThat(Stream.of("foo")).containsOnly("bar", "baz"),
        assertThat(Stream.of("foo")).containsOnly("bar", "baz"));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsSubsetOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).isSubsetOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).isSubsetOf(new String[] {"bar"}),
        assertThat(Stream.of("foo")).isSubsetOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).isSubsetOf(new String[] {"bar"}),
        assertThat(Stream.of("foo")).isSubsetOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).isSubsetOf(new String[] {"bar"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsSubsetOfVarargs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).isSubsetOf("bar", "baz"),
        assertThat(Stream.of("foo")).isSubsetOf("bar", "baz"),
        assertThat(Stream.of("foo")).isSubsetOf("bar", "baz"));
  }

  void testAssertThatAccepts() {
    assertThat((Predicate<String>) String::isEmpty).accepts("foo");
  }

  void testAssertThatRejects() {
    assertThat((Predicate<String>) String::isEmpty).rejects("foo");
  }
}
