package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.math.RoundingMode;
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
    return new HashMap<>();
  }

  String testMapGetOrNull() {
    return ImmutableMap.of(1, "foo").getOrDefault("bar", null);
  }

  ImmutableSet<Boolean> testMapIsEmpty() {
    return ImmutableSet.of(
        ImmutableMap.of("foo", 1).keySet().isEmpty(),
        ImmutableMap.of("bar", 2).values().isEmpty(),
        ImmutableMap.of("baz", 3).entrySet().isEmpty());
  }

  ImmutableSet<Integer> testMapSize() {
    return ImmutableSet.of(
        ImmutableMap.of("foo", 1).keySet().size(),
        ImmutableMap.of("bar", 2).values().size(),
        ImmutableMap.of("baz", 3).entrySet().size());
  }

  boolean testMapContainsKey() {
    return ImmutableMap.of("foo", 1).keySet().contains("bar");
  }

  boolean testMapContainsValue() {
    return ImmutableMap.of("foo", 1).values().contains(2);
  }

  Stream<String> testMapKeyStream() {
    return ImmutableMap.of("foo", 1).entrySet().stream().map(Map.Entry::getKey);
  }

  Stream<Integer> testMapValueStream() {
    return ImmutableMap.of("foo", 1).entrySet().stream().map(Map.Entry::getValue);
  }
}
