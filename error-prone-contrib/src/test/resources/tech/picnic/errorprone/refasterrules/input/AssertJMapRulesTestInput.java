package tech.picnic.errorprone.refasterrules.input;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedMultiset;
import com.google.common.collect.ImmutableSortedSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.MapAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJMapRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        ArrayList.class,
        HashMap.class,
        HashSet.class,
        ImmutableBiMap.class,
        ImmutableList.class,
        ImmutableMultiset.class,
        ImmutableSortedMap.class,
        ImmutableSortedMultiset.class,
        ImmutableSortedSet.class,
        LinkedHashMap.class,
        TreeMap.class,
        TreeSet.class);
  }

  void testAbstractMapAssertIsEmpty() {
    assertThat(ImmutableMap.of(1, 0)).containsExactlyEntriesOf(ImmutableMap.of());
    assertThat(ImmutableMap.of(2, 0)).containsExactlyEntriesOf(ImmutableMap.of(1, 2));
    assertThat(ImmutableMap.of(3, 0)).containsExactlyInAnyOrderEntriesOf(ImmutableMap.of());
    assertThat(ImmutableMap.of(4, 0))
        .containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(1, 2, 3, 4));
    assertThat(ImmutableMap.of(5, 0)).hasSameSizeAs(ImmutableMap.of());
    assertThat(ImmutableMap.of(6, 0)).hasSameSizeAs(ImmutableMap.of(1, 2));
    assertThat(ImmutableMap.of(7, 0)).isEqualTo(ImmutableMap.of());
    assertThat(ImmutableMap.of(8, 0)).isEqualTo(ImmutableMap.of("foo", "bar"));
    assertThat(ImmutableMap.of(9, 0)).containsOnlyKeys(ImmutableList.of());
    assertThat(ImmutableMap.of(10, 0)).containsOnlyKeys(ImmutableList.of(1));
    assertThat(ImmutableMap.of(11, 0)).containsExactly();
    assertThat(ImmutableMap.of(12, 0)).containsOnly();
    assertThat(ImmutableMap.of(13, 0)).containsOnlyKeys();
  }

  void testAssertThatMapIsEmpty() {
    assertThat(ImmutableMap.of(1, 0)).hasSize(0);
    assertThat(ImmutableMap.of(2, 0).isEmpty()).isTrue();
    assertThat(ImmutableMap.of(3, 0).size()).isEqualTo(0L);
    assertThat(ImmutableMap.of(4, 0).size()).isNotPositive();
    assertThat(ImmutableMap.of(5, 0).keySet()).isEmpty();
    assertThat(ImmutableMap.of(6, 0).values()).isEmpty();
    assertThat(ImmutableMap.of(7, 0).entrySet()).isEmpty();
  }

  ImmutableSet<MapAssert<Integer, Integer>> testAbstractMapAssertIsNotEmpty() {
    return ImmutableSet.of(
        assertThat(ImmutableMap.of(1, 0)).isNotEqualTo(ImmutableMap.of()),
        assertThat(ImmutableMap.of(2, 0)).isNotEqualTo(ImmutableMap.of("foo", "bar")));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatMapIsNotEmpty() {
    return ImmutableSet.of(
        assertThat(ImmutableMap.of(1, 0).isEmpty()).isFalse(),
        assertThat(ImmutableMap.of(2, 0).size()).isNotEqualTo(0),
        assertThat(ImmutableMap.of(3, 0).size()).isPositive(),
        assertThat(ImmutableMap.of(4, 0).keySet()).isNotEmpty(),
        assertThat(ImmutableMap.of(5, 0).values()).isNotEmpty(),
        assertThat(ImmutableMap.of(6, 0).entrySet()).isNotEmpty());
  }

  MapAssert<Integer, Integer> testAbstractMapAssertContainsExactlyInAnyOrderEntriesOf() {
    return assertThat(ImmutableMap.of(1, 2, 3, 4)).isEqualTo(ImmutableMap.of(1, 2, 3, 4));
  }

  MapAssert<Integer, Integer> testAbstractMapAssertContainsExactlyEntriesOf() {
    return assertThat(ImmutableMap.of(1, 2))
        .containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(1, 2));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatMapHasSize() {
    return ImmutableSet.of(
        assertThat(ImmutableMap.of(1, 2).size()).isEqualTo(1),
        assertThat(ImmutableMap.of(3, 4).keySet()).hasSize(1),
        assertThat(ImmutableMap.of(5, 6).values()).hasSize(1),
        assertThat(ImmutableMap.of(7, 8).entrySet()).hasSize(1));
  }

  MapAssert<Integer, Integer> testAbstractMapAssertHasSameSizeAs() {
    return assertThat(ImmutableMap.of(1, 2)).hasSize(ImmutableMap.of(3, 4).size());
  }

  AbstractAssert<?, ?> testAssertThatMapContainsKey() {
    return assertThat(ImmutableMap.of(1, 2).containsKey(3)).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatMapDoesNotContainKey() {
    return assertThat(ImmutableMap.of(1, 2).containsKey(3)).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatMapContainsOnlyKeys() {
    return assertThat(ImmutableMap.of(1, 2).keySet()).hasSameElementsAs(ImmutableSet.of(3));
  }

  AbstractAssert<?, ?> testAssertThatMapContainsValue() {
    return assertThat(ImmutableMap.of(1, 2).containsValue(3)).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatMapDoesNotContainValue() {
    return assertThat(ImmutableMap.of(1, 2).containsValue(3)).isFalse();
  }

  AbstractObjectAssert<?, ?> testAssertThatMapContainsEntry() {
    return assertThat(ImmutableMap.of(1, 2).get(1)).isEqualTo(2);
  }
}
