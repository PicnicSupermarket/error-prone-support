package tech.picnic.errorprone.refasterrules.output;

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
  public ImmutableSet<?> elidedTypesAndStaticImports() {
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
    assertThat(ImmutableMap.of(1, 0)).isEmpty();
    assertThat(ImmutableMap.of(2, 0)).isEmpty();
    assertThat(ImmutableMap.of(3, 0)).isEmpty();
    assertThat(ImmutableMap.of(4, 0)).isEmpty();
    assertThat(ImmutableMap.of(5, 0)).isEmpty();
    assertThat(ImmutableMap.of(6, 0)).isEmpty();
    assertThat(ImmutableMap.of(7, 0)).isEmpty();
    assertThat(ImmutableMap.of(8, 0)).isEmpty();
    assertThat(ImmutableMap.of(9, 0)).isEmpty();
    assertThat(ImmutableMap.of(10, 0)).isEmpty();
    assertThat(ImmutableMap.of(11, 0)).isEmpty();
    assertThat(ImmutableMap.of(12, 0)).isEmpty();
    assertThat(ImmutableMap.of(13, 0)).isEmpty();
    assertThat(ImmutableMap.of(14, 0)).isEmpty();
    assertThat(ImmutableMap.of(15, 0)).isEmpty();
    assertThat(ImmutableMap.of(16, 0)).isEmpty();
    assertThat(ImmutableMap.of(17, 0)).isEmpty();
    assertThat(ImmutableMap.of(18, 0)).isEmpty();
    assertThat(ImmutableMap.of(19, 0)).isEmpty();
    assertThat(ImmutableMap.of(20, 0)).isEmpty();
    assertThat(ImmutableMap.of(21, 0)).isEmpty();
    assertThat(ImmutableMap.of(22, 0)).isEmpty();
    assertThat(ImmutableMap.of(23, 0)).isEmpty();
    assertThat(ImmutableMap.of(24, 0)).isEmpty();
    assertThat(ImmutableMap.of(25, 0)).isEmpty();
    assertThat(ImmutableMap.of(26, 0)).isEmpty();
    assertThat(ImmutableMap.of(27, 0)).isEmpty();
    assertThat(ImmutableMap.of(28, 0)).isEmpty();
    assertThat(ImmutableMap.of(29, 0)).isEmpty();
  }

  void testAssertThatMapIsEmpty() {
    assertThat(ImmutableMap.of(1, 0)).isEmpty();
    assertThat(ImmutableMap.of(2, 0)).isEmpty();
    assertThat(ImmutableMap.of(3, 0)).isEmpty();
    assertThat(ImmutableMap.of(4, 0)).isEmpty();
    assertThat(ImmutableMap.of(5, 0)).isEmpty();
    assertThat(ImmutableMap.of(6, 0)).isEmpty();
    assertThat(ImmutableMap.of(7, 0)).isEmpty();
  }

  ImmutableSet<MapAssert<Integer, Integer>> testAbstractMapAssertIsNotEmpty() {
    return ImmutableSet.of(
        assertThat(ImmutableMap.of(1, 0)).isNotEmpty(),
        assertThat(ImmutableMap.of(2, 0)).isNotEmpty(),
        assertThat(ImmutableMap.of(3, 0)).isNotEmpty(),
        assertThat(ImmutableMap.of(4, 0)).isNotEmpty(),
        assertThat(ImmutableMap.of(5, 0)).isNotEmpty(),
        assertThat(ImmutableMap.of(6, 0)).isNotEmpty());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatMapIsNotEmpty() {
    return ImmutableSet.of(
        assertThat(ImmutableMap.of(1, 0)).isNotEmpty(),
        assertThat(ImmutableMap.of(2, 0)).isNotEmpty(),
        assertThat(ImmutableMap.of(3, 0)).isNotEmpty(),
        assertThat(ImmutableMap.of(4, 0)).isNotEmpty(),
        assertThat(ImmutableMap.of(5, 0)).isNotEmpty(),
        assertThat(ImmutableMap.of(6, 0)).isNotEmpty());
  }

  MapAssert<Integer, Integer> testAbstractMapAssertContainsExactlyInAnyOrderEntriesOf() {
    return assertThat(ImmutableMap.of(1, 2, 3, 4))
        .containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(1, 2, 3, 4));
  }

  MapAssert<Integer, Integer> testAbstractMapAssertContainsExactlyEntriesOf() {
    return assertThat(ImmutableMap.of(1, 2)).containsExactlyEntriesOf(ImmutableMap.of(1, 2));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatMapHasSize() {
    return ImmutableSet.of(
        assertThat(ImmutableMap.of(1, 2)).hasSize(1),
        assertThat(ImmutableMap.of(3, 4)).hasSize(1),
        assertThat(ImmutableMap.of(5, 6)).hasSize(1),
        assertThat(ImmutableMap.of(7, 8)).hasSize(1));
  }

  MapAssert<Integer, Integer> testAbstractMapAssertHasSameSizeAs() {
    return assertThat(ImmutableMap.of(1, 2)).hasSameSizeAs(ImmutableMap.of(3, 4));
  }

  AbstractAssert<?, ?> testAssertThatMapContainsKey() {
    return assertThat(ImmutableMap.of(1, 2)).containsKey(3);
  }

  AbstractAssert<?, ?> testAssertThatMapDoesNotContainKey() {
    return assertThat(ImmutableMap.of(1, 2)).doesNotContainKey(3);
  }

  AbstractAssert<?, ?> testAssertThatMapContainsValue() {
    return assertThat(ImmutableMap.of(1, 2)).containsValue(3);
  }

  AbstractAssert<?, ?> testAssertThatMapDoesNotContainValue() {
    return assertThat(ImmutableMap.of(1, 2)).doesNotContainValue(3);
  }

  AbstractObjectAssert<?, ?> testAssertThatMapContainsEntry() {
    return assertThat(ImmutableMap.of(1, 2).get(1)).isEqualTo(2);
  }
}
