package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.assertj.core.api.ObjectEnumerableAssert;

final class AssertJObjectEnumerableTemplatesTest implements RefasterTemplateTestCase {
  ImmutableSet<ObjectEnumerableAssert<?, ?>> testObjectEnumerableAssertContains() {
    return ImmutableSet.of(
        assertThat(ImmutableSortedSet.of(1)).containsAnyElementsOf(ImmutableSortedSet.of(1)),
        assertThat(ImmutableSortedSet.of(1)).containsAll(ImmutableSortedSet.of(1)),
        assertThat(ImmutableSortedSet.of(1)).containsSequence(ImmutableSortedSet.of(1)),
        assertThat(ImmutableSortedSet.of(1)).containsSubsequence(ImmutableSortedSet.of(1)),
        assertThat(ImmutableList.of(1)).containsAnyElementsOf(ImmutableList.of(1)),
        assertThat(ImmutableList.of(1)).containsAnyOf(1),
        assertThat(ImmutableList.of(1)).containsAll(ImmutableList.of(1)),
        assertThat(ImmutableList.of(1)).containsSequence(ImmutableList.of(1)),
        assertThat(ImmutableList.of(1)).containsSequence(1),
        assertThat(ImmutableList.of(1)).containsSubsequence(ImmutableList.of(1)),
        assertThat(ImmutableList.of(1)).containsSubsequence(1));
  }

  ImmutableSet<ObjectEnumerableAssert<?, ?>> testObjectEnumerableAssertDoesNotContain() {
    return ImmutableSet.of(
        assertThat(ImmutableSortedSet.of(1)).doesNotContainAnyElementsOf(ImmutableSortedSet.of(1)),
        assertThat(ImmutableSortedSet.of(1)).doesNotContainSequence(ImmutableSortedSet.of(1)),
        assertThat(ImmutableList.of(1)).doesNotContainAnyElementsOf(ImmutableList.of(1)),
        assertThat(ImmutableList.of(1)).doesNotContainSequence(ImmutableList.of(1)),
        assertThat(ImmutableList.of(1)).doesNotContainSequence(1));
  }
}
