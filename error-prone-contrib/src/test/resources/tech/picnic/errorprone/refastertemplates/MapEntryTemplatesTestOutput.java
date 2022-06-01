package tech.picnic.errorprone.refastertemplates;

import static java.util.Map.Entry.comparingByKey;
import static java.util.Map.Entry.comparingByValue;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;

final class MapEntryTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(AbstractMap.class, Maps.class);
  }

  ImmutableSet<Map.Entry<String, Integer>> testMapEntry() {
    return ImmutableSet.of(Map.entry("foo", 1), Map.entry("bar", 2));
  }

  ImmutableSet<Comparator<Map.Entry<Integer, String>>> testMapEntryComparingByKey() {
    return ImmutableSet.of(comparingByKey(), comparingByKey());
  }

  Comparator<Map.Entry<Integer, String>> testMapEntryComparingByKeyWithCustomComparator() {
    return comparingByKey(Comparator.comparingInt(i -> i * 2));
  }

  ImmutableSet<Comparator<Map.Entry<Integer, String>>> testMapEntryComparingByValue() {
    return ImmutableSet.of(comparingByValue(), comparingByValue());
  }

  Comparator<Map.Entry<Integer, String>> testMapEntryComparingByValueWithCustomComparator() {
    return comparingByValue(Comparator.comparingInt(String::length));
  }
}
