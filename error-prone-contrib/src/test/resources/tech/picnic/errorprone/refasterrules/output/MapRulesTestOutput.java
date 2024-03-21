package tech.picnic.errorprone.refasterrules.output;

import static java.util.Objects.requireNonNullElse;

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
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(HashMap.class, requireNonNullElse(null, null));
  }

  Map<RoundingMode, String> testCreateEnumMap() {
    return new EnumMap<>(RoundingMode.class);
  }

  String testMapGetOrNull() {
    return ImmutableMap.of(1, "foo").get("bar");
  }

  String testMapGetOrDefault() {
    return ImmutableMap.of(1, "foo").getOrDefault("bar", "baz");
  }

  ImmutableSet<Boolean> testMapIsEmpty() {
    return ImmutableSet.of(
        ImmutableMap.of("foo", 1).isEmpty(),
        ImmutableMap.of("bar", 2).isEmpty(),
        ImmutableMap.of("baz", 3).isEmpty());
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
}
