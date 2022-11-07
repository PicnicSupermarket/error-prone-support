package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class MapRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(HashMap.class);
  }

  Map<RoundingMode, String> testCreateEnumMap() {
    return new EnumMap<>(RoundingMode.class);
  }

  String testMapGetOrNull() {
    return ImmutableMap.of(1, "foo").get("bar");
  }

  ImmutableSet<Integer> testMapSize() {
    return ImmutableSet.of(
        ImmutableMap.of("foo", 1).size(),
        ImmutableMap.of("bar", 2).size(),
        ImmutableMap.of("baz", 3).size());
  }

  boolean testMapContainsKey() {
    return ImmutableMap.of("foo", 1).containsKey("bar");
  }

  boolean testMapContainsValue() {
    return ImmutableMap.of("foo", 1).containsValue(2);
  }

  Stream<String> testMapKeyStream() {
    return ImmutableMap.of("foo", 1).keySet().stream();
  }

  Stream<Integer> testMapValueStream() {
    return ImmutableMap.of("foo", 1).values().stream();
  }

  Stream<boolean> testMapIsEmpty() {
    return ImmutableMap.of("foo", 1).isEmpty();
  }
}
