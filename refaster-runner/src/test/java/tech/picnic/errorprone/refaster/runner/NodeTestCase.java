package tech.picnic.errorprone.refaster.runner;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.ImmutableSetMultimap.flatteningToImmutableSetMultimap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.collectingAndThen;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

@AutoValue
abstract class NodeTestCase<V> {
  static NodeTestCase<Integer> generate(
      int entryCount, int maxPathCount, int maxPathLength, int pathValueDomainSize, Random random) {
    return random
        .ints(entryCount)
        .boxed()
        .collect(
            collectingAndThen(
                flatteningToImmutableSetMultimap(
                    identity(),
                    i ->
                        random
                            .ints(random.nextInt(maxPathCount + 1))
                            .mapToObj(
                                p ->
                                    random
                                        .ints(
                                            random.nextInt(maxPathLength + 1),
                                            0,
                                            pathValueDomainSize)
                                        .mapToObj(String::valueOf)
                                        .collect(toImmutableSet()))),
                AutoValue_NodeTestCase::new));
  }

  abstract ImmutableSetMultimap<V, ImmutableSet<String>> input();

  final Node<V> buildTree() {
    return Node.create(input().keySet(), input()::get);
  }

  final Stream<NodeTestCaseEntry<V>> generateTestCaseEntries(Random random) {
    return generatePathTestCases(input(), random);
  }

  private static <V> Stream<NodeTestCaseEntry<V>> generatePathTestCases(
      ImmutableSetMultimap<V, ImmutableSet<String>> treeInput, Random random) {
    ImmutableSet<String> allEdges =
        treeInput.values().stream().flatMap(ImmutableSet::stream).collect(toImmutableSet());

    return Stream.concat(
            Stream.of(ImmutableSet.<String>of()), shuffle(treeInput.values(), random).stream())
        // XXX: Use `random.nextInt(20, 100)` once we no longer target JDK 11. (And consider
        // introducing a Refaster template for this case.)
        .limit(20 + random.nextInt(80))
        .flatMap(edges -> generateVariations(edges, allEdges, "unused", random))
        .distinct()
        .map(edges -> createTestCaseEntry(treeInput, edges));
  }

  private static <T> Stream<ImmutableSet<T>> generateVariations(
      ImmutableSet<T> baseEdges, ImmutableSet<T> allEdges, T unusedEdge, Random random) {
    Optional<T> knownEdge = selectRandomElement(allEdges, random);

    return Stream.of(
            random.nextBoolean() ? null : baseEdges,
            random.nextBoolean() ? null : shuffle(baseEdges, random),
            random.nextBoolean() ? null : insertValue(baseEdges, unusedEdge, random),
            baseEdges.isEmpty() || random.nextBoolean()
                ? null
                : randomStrictSubset(baseEdges, random),
            baseEdges.isEmpty() || random.nextBoolean()
                ? null
                : insertValue(randomStrictSubset(baseEdges, random), unusedEdge, random),
            baseEdges.isEmpty() || random.nextBoolean()
                ? null
                : knownEdge
                    .map(edge -> insertValue(randomStrictSubset(baseEdges, random), edge, random))
                    .orElse(null))
        .filter(Objects::nonNull);
  }

  private static <T> Optional<T> selectRandomElement(ImmutableSet<T> collection, Random random) {
    return collection.isEmpty()
        ? Optional.empty()
        : Optional.of(collection.asList().get(random.nextInt(collection.size())));
  }

  private static <T> ImmutableSet<T> shuffle(ImmutableCollection<T> values, Random random) {
    List<T> allValues = new ArrayList<>(values);
    Collections.shuffle(allValues, random);
    return ImmutableSet.copyOf(allValues);
  }

  private static <T> ImmutableSet<T> insertValue(
      ImmutableSet<T> values, T extraValue, Random random) {
    List<T> allValues = new ArrayList<>(values);
    allValues.add(random.nextInt(values.size() + 1), extraValue);
    return ImmutableSet.copyOf(allValues);
  }

  private static <T> ImmutableSet<T> randomStrictSubset(ImmutableSet<T> values, Random random) {
    checkArgument(!values.isEmpty(), "Cannot select strict subset of random collection");

    List<T> allValues = new ArrayList<>(values);
    Collections.shuffle(allValues, random);
    return ImmutableSet.copyOf(allValues.subList(0, random.nextInt(allValues.size())));
  }

  private static <V> NodeTestCaseEntry<V> createTestCaseEntry(
      ImmutableSetMultimap<V, ImmutableSet<String>> treeInput, ImmutableSet<String> edges) {
    return new AutoValue_NodeTestCase_NodeTestCaseEntry<>(
        edges,
        treeInput.asMap().entrySet().stream()
            .filter(e -> e.getValue().stream().anyMatch(edges::containsAll))
            .map(Map.Entry::getKey)
            .collect(toImmutableList()));
  }

  @AutoValue
  abstract static class NodeTestCaseEntry<V> {
    abstract ImmutableSet<String> candidateEdges();

    abstract ImmutableList<V> reachableValues();
  }
}
