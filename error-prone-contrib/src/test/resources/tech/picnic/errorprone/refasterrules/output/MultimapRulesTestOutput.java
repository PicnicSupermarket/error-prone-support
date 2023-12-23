package tech.picnic.errorprone.refasterrules.output;

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
    return ImmutableSetMultimap.of("foo", "bar").keySet();
  }

  ImmutableSet<Boolean> testMultimapIsEmpty() {
    return ImmutableSet.of(
        ImmutableSetMultimap.of("foo", 1).isEmpty(),
        ImmutableSetMultimap.of("bar", 2).isEmpty(),
        ImmutableSetMultimap.of("baz", 3).isEmpty(),
        ImmutableSetMultimap.of("qux", 54).isEmpty());
  }

  int testMultimapSize() {
    return ImmutableSetMultimap.of().size();
  }

  ImmutableSet<Boolean> testMultimapContainsKey() {
    return ImmutableSet.of(
        ImmutableSetMultimap.of("foo", 1).containsKey("bar"),
        ImmutableSetMultimap.of("baz", 1).containsKey("qux"));
  }

  boolean testMultimapContainsValue() {
    return ImmutableSetMultimap.of("foo", 1).containsValue(2);
  }

  ImmutableSet<Collection<Integer>> testMultimapGet() {
    return ImmutableSet.of(
        ImmutableSetMultimap.of(1, 2).get(1),
        ((Multimap<Integer, Integer>) ImmutableSetMultimap.of(1, 2)).get(1));
  }

  Stream<String> testMultimapKeysStream() {
    return ImmutableSetMultimap.of("foo", 1).keys().stream();
  }

  Stream<Integer> testMultimapValuesStream() {
    return ImmutableSetMultimap.of("foo", 1).values().stream();
  }
}
