package tech.picnic.errorprone.refastertemplates;

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
    return ImmutableSet.of(
        Maps.immutableEntry("foo", 1), new AbstractMap.SimpleImmutableEntry<>("bar", 2));
  }

  @Template(MapEntryComparingByKey.class)
  ImmutableSet<Comparator<Map.Entry<Integer, String>>> testMapEntryComparingByKey() {
    return ImmutableSet.of(
        Comparator.comparing(Map.Entry::getKey),
        Map.Entry.comparingByKey(Comparator.naturalOrder()));
  }

  @Template(MapEntryComparingByKeyWithCustomComparator.class)
  Comparator<Map.Entry<Integer, String>> testMapEntryComparingByKeyWithCustomComparator() {
    return Comparator.comparing(Map.Entry::getKey, Comparator.comparingInt(i -> i * 2));
  }

  @Template(MapEntryComparingByValue.class)
  ImmutableSet<Comparator<Map.Entry<Integer, String>>> testMapEntryComparingByValue() {
    return ImmutableSet.of(
        Comparator.comparing(Map.Entry::getValue),
        Map.Entry.comparingByValue(Comparator.naturalOrder()));
  }

  @Template(MapEntryComparingByValueWithCustomComparator.class)
  Comparator<Map.Entry<Integer, String>> testMapEntryComparingByValueWithCustomComparator() {
    return Comparator.comparing(Map.Entry::getValue, Comparator.comparingInt(String::length));
  }
}
