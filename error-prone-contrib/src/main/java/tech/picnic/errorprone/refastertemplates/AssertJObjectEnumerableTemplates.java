package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Arrays;
import org.assertj.core.api.ObjectEnumerableAssert;

final class AssertJObjectEnumerableTemplates {
  private AssertJObjectEnumerableTemplates() {}

  static final class ObjectEnumerableAssertContains<
      S, T extends S, C extends Comparable<? super C>> {
    @BeforeTemplate
    ObjectEnumerableAssert<?, C> before(ObjectEnumerableAssert<?, C> iterAssert, C element) {
      return Refaster.anyOf(
          iterAssert.containsAnyElementsOf(ImmutableSortedSet.of(element)),
          iterAssert.containsAll(ImmutableSortedSet.of(element)),
          iterAssert.containsSequence(ImmutableSortedSet.of(element)),
          iterAssert.containsSubsequence(ImmutableSortedSet.of(element)));
    }

    @BeforeTemplate
    @SuppressWarnings("unchecked")
    ObjectEnumerableAssert<?, S> before(ObjectEnumerableAssert<?, S> iterAssert, T element) {
      return Refaster.anyOf(
          iterAssert.containsAnyElementsOf(
              Refaster.anyOf(
                  ImmutableList.of(element),
                  Arrays.asList(element),
                  ImmutableSet.of(element),
                  ImmutableMultiset.of(element))),
          iterAssert.containsAnyOf(element),
          iterAssert.containsAll(
              Refaster.anyOf(
                  ImmutableList.of(element),
                  Arrays.asList(element),
                  ImmutableSet.of(element),
                  ImmutableMultiset.of(element))),
          iterAssert.containsSequence(
              Refaster.anyOf(
                  ImmutableList.of(element),
                  Arrays.asList(element),
                  ImmutableSet.of(element),
                  ImmutableMultiset.of(element))),
          iterAssert.containsSequence(element),
          iterAssert.containsSubsequence(
              Refaster.anyOf(
                  ImmutableList.of(element),
                  Arrays.asList(element),
                  ImmutableSet.of(element),
                  ImmutableMultiset.of(element))),
          iterAssert.containsSubsequence(element));
    }

    @AfterTemplate
    @SuppressWarnings("unchecked")
    ObjectEnumerableAssert<?, S> after(ObjectEnumerableAssert<?, S> iterAssert, T element) {
      return iterAssert.contains(element);
    }
  }

  static final class ObjectEnumerableAssertDoesNotContain<
      S, T extends S, C extends Comparable<? super C>> {
    @BeforeTemplate
    ObjectEnumerableAssert<?, C> before(ObjectEnumerableAssert<?, C> iterAssert, C element) {
      return Refaster.anyOf(
          iterAssert.doesNotContainAnyElementsOf(ImmutableSortedSet.of(element)),
          iterAssert.doesNotContainSequence(ImmutableSortedSet.of(element)));
    }

    @BeforeTemplate
    @SuppressWarnings("unchecked")
    ObjectEnumerableAssert<?, S> before(ObjectEnumerableAssert<?, S> iterAssert, T element) {
      return Refaster.anyOf(
          iterAssert.doesNotContainAnyElementsOf(
              Refaster.anyOf(
                  ImmutableList.of(element),
                  Arrays.asList(element),
                  ImmutableSet.of(element),
                  ImmutableMultiset.of(element))),
          iterAssert.doesNotContainSequence(
              Refaster.anyOf(
                  ImmutableList.of(element),
                  Arrays.asList(element),
                  ImmutableSet.of(element),
                  ImmutableMultiset.of(element))),
          iterAssert.doesNotContainSequence(element));
    }

    @AfterTemplate
    @SuppressWarnings("unchecked")
    ObjectEnumerableAssert<?, S> after(ObjectEnumerableAssert<?, S> iterAssert, T element) {
      return iterAssert.doesNotContain(element);
    }
  }
}
