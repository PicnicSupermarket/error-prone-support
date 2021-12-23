package tech.picnic.errorprone.refastertemplates;

import static java.util.Map.Entry.comparingByKey;
import static java.util.Map.Entry.comparingByValue;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.MapEntryTemplates.MapEntry;
import tech.picnic.errorprone.refastertemplates.MapEntryTemplates.MapEntryComparingByKey;
import tech.picnic.errorprone.refastertemplates.MapEntryTemplates.MapEntryComparingByKeyWithCustomComparator;
import tech.picnic.errorprone.refastertemplates.MapEntryTemplates.MapEntryComparingByValue;
import tech.picnic.errorprone.refastertemplates.MapEntryTemplates.MapEntryComparingByValueWithCustomComparator;

@TemplateCollection(MapEntryTemplates.class)
final class MapEntryTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(AbstractMap.class, Maps.class);
  }

  @Template(MapEntry.class)
  ImmutableSet<Map.Entry<String, Integer>> testMapEntry() {
    return ImmutableSet.of(Map.entry("foo", 1), Map.entry("bar", 2));
  }

  @Template(MapEntryComparingByKey.class)
  ImmutableSet<Comparator<Map.Entry<Integer, String>>> testMapEntryComparingByKey() {
    return ImmutableSet.of(comparingByKey(), comparingByKey());
  }

  @Template(MapEntryComparingByKeyWithCustomComparator.class)
  Comparator<Map.Entry<Integer, String>> testMapEntryComparingByKeyWithCustomComparator() {
    return comparingByKey(Comparator.comparingInt(i -> i * 2));
  }

  @Template(MapEntryComparingByValue.class)
  ImmutableSet<Comparator<Map.Entry<Integer, String>>> testMapEntryComparingByValue() {
    return ImmutableSet.of(comparingByValue(), comparingByValue());
  }

  @Template(MapEntryComparingByValueWithCustomComparator.class)
  Comparator<Map.Entry<Integer, String>> testMapEntryComparingByValueWithCustomComparator() {
    return comparingByValue(Comparator.comparingInt(String::length));
  }
}
