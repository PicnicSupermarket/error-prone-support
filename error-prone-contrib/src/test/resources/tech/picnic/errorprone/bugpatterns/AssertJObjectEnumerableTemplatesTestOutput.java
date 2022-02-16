package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.assertj.core.api.ObjectEnumerableAssert;

final class AssertJObjectEnumerableTemplatesTest implements RefasterTemplateTestCase {
  ImmutableSet<ObjectEnumerableAssert<?, ?>> testObjectEnumerableAssertContains() {
    return ImmutableSet.of(
        assertThat(ImmutableSortedSet.of(1)).contains(1),
        assertThat(ImmutableSortedSet.of(1)).contains(1),
        assertThat(ImmutableSortedSet.of(1)).contains(1),
        assertThat(ImmutableSortedSet.of(1)).contains(1),
        assertThat(ImmutableList.of(1)).contains(1),
        assertThat(ImmutableList.of(1)).contains(1),
        assertThat(ImmutableList.of(1)).contains(1),
        assertThat(ImmutableList.of(1)).contains(1),
        assertThat(ImmutableList.of(1)).contains(1),
        assertThat(ImmutableList.of(1)).contains(1),
        assertThat(ImmutableList.of(1)).contains(1));
  }

  ImmutableSet<ObjectEnumerableAssert<?, ?>> testObjectEnumerableAssertDoesNotContain() {
    return ImmutableSet.of(
        assertThat(ImmutableSortedSet.of(1)).doesNotContain(1),
        assertThat(ImmutableSortedSet.of(1)).doesNotContain(1),
        assertThat(ImmutableList.of(1)).doesNotContain(1),
        assertThat(ImmutableList.of(1)).doesNotContain(1),
        assertThat(ImmutableList.of(1)).doesNotContain(1));
  }
}
