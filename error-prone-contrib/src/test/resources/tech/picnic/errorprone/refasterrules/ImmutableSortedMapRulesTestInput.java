package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static java.util.Comparator.naturalOrder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ImmutableSortedMapRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Stream.class, Streams.class, naturalOrder(), toImmutableSortedMap(null, null, null));
  }

  ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapOrderedBy() {
    return new ImmutableSortedMap.Builder<>(Comparator.comparingInt(String::length));
  }

  ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapNaturalOrder() {
    return ImmutableSortedMap.orderedBy(Comparator.<String>naturalOrder());
  }

  ImmutableSortedMap.Builder<String, Integer> testImmutableSortedMapReverseOrder() {
    return ImmutableSortedMap.orderedBy(Comparator.<String>reverseOrder());
  }

  ImmutableSortedMap<String, Integer> testImmutableSortedMapOf() {
    return ImmutableSortedMap.<String, Integer>naturalOrder().buildOrThrow();
  }

  ImmutableSortedMap<String, Integer> testImmutableSortedMapOf1() {
    return ImmutableSortedMap.<String, Integer>naturalOrder().put("foo", 1).buildOrThrow();
  }

  ImmutableSet<ImmutableSortedMap<String, Integer>>
      testImmutableSortedMapOfMapEntryGetKeyMapEntryGetValue() {
    return ImmutableSet.of(
        ImmutableSortedMap.<String, Integer>naturalOrder().put(Map.entry("foo", 1)).buildOrThrow(),
        Stream.of(Map.entry("bar", 2))
            .collect(toImmutableSortedMap(naturalOrder(), Map.Entry::getKey, Map.Entry::getValue)));
  }

  ImmutableSet<ImmutableSortedMap<String, Integer>> testImmutableSortedMapCopyOf() {
    return ImmutableSet.of(
        ImmutableSortedMap.copyOf(ImmutableSortedMap.of("foo", 1), naturalOrder()),
        ImmutableSortedMap.copyOf(ImmutableSortedMap.of("bar", 2).entrySet()),
        ImmutableSortedMap.<String, Integer>naturalOrder()
            .putAll(ImmutableSortedMap.of("baz", 3))
            .buildOrThrow(),
        ImmutableSortedMap.copyOf(Iterables.cycle(Map.entry("qux", 4)), naturalOrder()),
        ImmutableSortedMap.<String, Integer>naturalOrder()
            .putAll(ImmutableSortedMap.of("quux", 5).entrySet())
            .buildOrThrow(),
        Streams.stream(Iterables.cycle(Map.entry("corge", 6)))
            .collect(toImmutableSortedMap(naturalOrder(), Map.Entry::getKey, Map.Entry::getValue)),
        ImmutableSortedMap.of("grault", 7).entrySet().stream()
            .collect(toImmutableSortedMap(naturalOrder(), Map.Entry::getKey, Map.Entry::getValue)));
  }
}
