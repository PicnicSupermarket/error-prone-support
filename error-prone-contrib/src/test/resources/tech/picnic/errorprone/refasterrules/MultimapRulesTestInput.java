package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class MultimapRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Map.class, Multimaps.class);
  }

  Set<String> testMultimapKeySet() {
    return ImmutableSetMultimap.of("foo", "bar").asMap().keySet();
  }

  ImmutableSet<Boolean> testMultimapIsEmpty() {
    return ImmutableSet.of(
        ImmutableSetMultimap.of("foo", 1).keySet().isEmpty(),
        ImmutableSetMultimap.of("bar", 2).keys().isEmpty(),
        ImmutableSetMultimap.of("baz", 3).values().isEmpty(),
        ImmutableSetMultimap.of("qux", 54).entries().isEmpty());
  }

  int testMultimapSize() {
    return ImmutableSetMultimap.of().values().size();
  }

  ImmutableSet<Boolean> testMultimapContainsKey() {
    return ImmutableSet.of(
        ImmutableSetMultimap.of("foo", 1).keySet().contains("bar"),
        ImmutableSetMultimap.of("baz", 1).keys().contains("qux"));
  }

  boolean testMultimapContainsValue() {
    return ImmutableSetMultimap.of("foo", 1).values().contains(2);
  }

  ImmutableSet<Collection<Integer>> testMultimapGet() {
    return ImmutableSet.of(
        ImmutableSetMultimap.of(1, 2).asMap().get(1),
        Multimaps.asMap((Multimap<Integer, Integer>) ImmutableSetMultimap.of(1, 2)).get(1));
  }

  Stream<String> testMultimapKeysStream() {
    return ImmutableSetMultimap.of("foo", 1).entries().stream().map(Map.Entry::getKey);
  }

  Stream<Integer> testMultimapValuesStream() {
    return ImmutableSetMultimap.of("foo", 1).entries().stream().map(Map.Entry::getValue);
  }
}
