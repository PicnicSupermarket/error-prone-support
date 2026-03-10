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

  AbstractAssert<?, ?> testAssertThatOptionalDouble() {
    return assertThat(OptionalDouble.of(1.0)).hasValue(2.0);
  }

  AbstractAssert<?, ?> testAssertThatOptionalInt() {
    return assertThat(OptionalInt.of(1)).hasValue(2);
  }

  AbstractAssert<?, ?> testAssertThatOptionalLong() {
    return assertThat(OptionalLong.of(1L)).hasValue(2L);
  }

  ImmutableSet<ObjectEnumerableAssert<?, String>> testObjectEnumerableContainsOneElement() {
    return ImmutableSet.of(
        assertThat(ImmutableList.of("foo")).contains("bar"),
        assertThat(ImmutableList.of("foo")).contains("bar"),
        assertThat(ImmutableList.of("foo")).contains("bar"));
  }

  ObjectEnumerableAssert<?, String> testObjectEnumerableDoesNotContainOneElement() {
    return assertThat(ImmutableList.of("foo")).doesNotContain("bar");
  }

  ImmutableSet<ObjectEnumerableAssert<?, String>> testObjectEnumerableContainsExactlyOneElement() {
    return ImmutableSet.of(
        assertThat(ImmutableList.of("foo")).containsExactlyInAnyOrder(new String[] {"bar"}),
        assertThat(ImmutableList.of("foo")).containsExactly("bar"));
  }

  ObjectEnumerableAssert<?, String> testAssertThatSetContainsExactlyOneElement() {
    return assertThat(ImmutableSet.of("foo")).containsExactly("bar");
  }

  ListAssert<String> testAssertThatListsAreEqual() {
    return assertThat(ImmutableList.of("foo")).containsExactlyElementsOf(ImmutableList.of("bar"));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatSetsAreEqual() {
    return ImmutableSet.of(
        assertThat(ImmutableSet.of("foo")).hasSameElementsAs(ImmutableSet.of("bar")),
        assertThat(ImmutableSet.of("foo")).hasSameElementsAs(ImmutableSet.of("bar")));
  }

  AbstractAssert<?, ?> testAssertThatMultisetsAreEqual() {
    return assertThat(ImmutableMultiset.of("foo"))
        .containsExactlyInAnyOrderElementsOf(ImmutableMultiset.of("bar"));
  }

  AbstractAssert<?, ?> testAssertThatMapContainsEntry() {
    return assertThat(ImmutableMap.of("foo", 1)).containsEntry("bar", 2);
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatStreamContainsAnyElementsOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsAnyElementsOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).containsAnyElementsOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).containsAnyElementsOf(ImmutableList.of("bar")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatStreamContainsAnyOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsAnyOf(new String[] {"bar"}),
        assertThat(Stream.of("foo")).containsAnyOf(new String[] {"bar"}),
        assertThat(Stream.of("foo")).containsAnyOf(new String[] {"bar"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatStreamContainsAnyOfVarArgs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsAnyOf("bar", "baz"),
        assertThat(Stream.of("foo")).containsAnyOf("bar", "baz"),
        assertThat(Stream.of("foo")).containsAnyOf("bar", "baz"));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatStreamContainsAll() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsAll(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).containsAll(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).containsAll(ImmutableList.of("bar")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatStreamContains() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).contains(new String[] {"bar"}),
        assertThat(Stream.of("foo")).contains(new String[] {"bar"}),
        assertThat(Stream.of("foo")).contains(new String[] {"bar"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatStreamContainsVarArgs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).contains("bar", "baz"),
        assertThat(Stream.of("foo")).contains("bar", "baz"),
        assertThat(Stream.of("foo")).contains("bar", "baz"));
  }

  ListAssert<String> testAssertThatStreamContainsExactlyElementsOf() {
    return assertThat(Stream.of("foo")).containsExactlyElementsOf(ImmutableList.of("bar"));
  }

  ListAssert<String> testAssertThatStreamContainsExactly() {
    return assertThat(Stream.of("foo")).containsExactly(new String[] {"bar"});
  }

  ListAssert<String> testAssertThatStreamContainsExactlyVarargs() {
    return assertThat(Stream.of("foo")).containsExactly("bar", "baz");
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatStreamContainsExactlyInAnyOrderElementsOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsExactlyInAnyOrderElementsOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).containsExactlyInAnyOrderElementsOf(ImmutableList.of("bar")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatStreamContainsExactlyInAnyOrder() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsExactlyInAnyOrder(new String[] {"bar"}),
        assertThat(Stream.of("foo")).containsExactlyInAnyOrder(new String[] {"bar"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatStreamContainsExactlyInAnyOrderVarArgs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsExactlyInAnyOrder("bar", "baz"),
        assertThat(Stream.of("foo")).containsExactlyInAnyOrder("bar", "baz"));
  }

  ImmutableSet<ListAssert<String>> testAssertThatStreamContainsSequence() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsSequence(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).containsSequence(new String[] {"bar"}));
  }

  ListAssert<String> testAssertThatStreamContainsSequenceVarArgs() {
    return assertThat(Stream.of("foo")).containsSequence("bar", "baz");
  }

  ImmutableSet<ListAssert<String>> testAssertThatStreamContainsSubsequence() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsSubsequence(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).containsSubsequence(new String[] {"bar"}));
  }

  ListAssert<String> testAssertThatStreamContainsSubsequenceVarArgs() {
    return assertThat(Stream.of("foo")).containsSubsequence("bar", "baz");
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatStreamDoesNotContainAnyElementsOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).doesNotContainAnyElementsOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).doesNotContainAnyElementsOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).doesNotContainAnyElementsOf(ImmutableList.of("bar")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatStreamDoesNotContain() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).doesNotContain(new String[] {"bar"}),
        assertThat(Stream.of("foo")).doesNotContain(new String[] {"bar"}),
        assertThat(Stream.of("foo")).doesNotContain(new String[] {"bar"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatStreamDoesNotContainVarArgs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).doesNotContain("bar", "baz"),
        assertThat(Stream.of("foo")).doesNotContain("bar", "baz"),
        assertThat(Stream.of("foo")).doesNotContain("bar", "baz"));
  }

  ImmutableSet<ListAssert<String>> testAssertThatStreamDoesNotContainSequence() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).doesNotContainSequence(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).doesNotContainSequence(new String[] {"bar"}));
  }

  ListAssert<String> testAssertThatStreamDoesNotContainSequenceVarArgs() {
    return assertThat(Stream.of("foo")).doesNotContainSequence("bar", "baz");
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatStreamHasSameElementsAs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).hasSameElementsAs(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).hasSameElementsAs(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).hasSameElementsAs(ImmutableList.of("bar")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatStreamContainsOnly() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsOnly(new String[] {"bar"}),
        assertThat(Stream.of("foo")).containsOnly(new String[] {"bar"}),
        assertThat(Stream.of("foo")).containsOnly(new String[] {"bar"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatStreamContainsOnlyVarArgs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).containsOnly("bar", "baz"),
        assertThat(Stream.of("foo")).containsOnly("bar", "baz"),
        assertThat(Stream.of("foo")).containsOnly("bar", "baz"));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatStreamIsSubsetOf() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).isSubsetOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).isSubsetOf(new String[] {"bar"}),
        assertThat(Stream.of("foo")).isSubsetOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).isSubsetOf(new String[] {"bar"}),
        assertThat(Stream.of("foo")).isSubsetOf(ImmutableList.of("bar")),
        assertThat(Stream.of("foo")).isSubsetOf(new String[] {"bar"}));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatStreamIsSubsetOfVarArgs() {
    return ImmutableSet.of(
        assertThat(Stream.of("foo")).isSubsetOf("bar", "baz"),
        assertThat(Stream.of("foo")).isSubsetOf("bar", "baz"),
        assertThat(Stream.of("foo")).isSubsetOf("bar", "baz"));
  }

  void testAssertThatPredicateAccepts() {
    assertThat((Predicate<String>) String::isEmpty).accepts("foo");
  }

  void testAssertThatPredicateRejects() {
    assertThat((Predicate<String>) String::isEmpty).rejects("foo");
  }
}
