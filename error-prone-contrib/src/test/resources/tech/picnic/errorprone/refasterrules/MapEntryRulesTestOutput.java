package tech.picnic.errorprone.refasterrules;

import static java.util.Map.Entry.comparingByKey;
import static java.util.Map.Entry.comparingByValue;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class MapEntryRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(AbstractMap.class, Maps.class);
  }

  ImmutableSet<Map.Entry<String, Integer>> testMapEntry() {
    return ImmutableSet.of(Map.entry("foo", 1), Map.entry("bar", 2));
  }

  ImmutableSet<Comparator<Map.Entry<Integer, String>>> testComparingByKey() {
    return ImmutableSet.of(comparingByKey(), comparingByKey());
  }

  Comparator<Map.Entry<Integer, String>> testComparingByKeyWithComparator() {
    return comparingByKey(Comparator.comparingInt(i -> i * 2));
  }

  ImmutableSet<Comparator<Map.Entry<Integer, String>>> testComparingByValue() {
    return ImmutableSet.of(comparingByValue(), comparingByValue());
  }

  Comparator<Map.Entry<Integer, String>> testComparingByValueWithComparator() {
    return comparingByValue(Comparator.comparingInt(String::length));
  }
}
