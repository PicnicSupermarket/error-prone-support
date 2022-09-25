package tech.picnic.errorprone.refaster.runner;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.ImmutableSetMultimap.flatteningToImmutableSetMultimap;
import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class NodeTest {
  private static Stream<Arguments> verifyTestCases() {
    Random random = new Random(0);

    /* { source, random } */
    return Stream.of(
        arguments(generateTestInput(random, 0, 0, 0, 0), random),
        arguments(generateTestInput(random, 1, 1, 1, 1), random),
        arguments(generateTestInput(random, 2, 2, 2, 10), random),
        arguments(generateTestInput(random, 2, 2, 2, 100), random),
        arguments(generateTestInput(random, 2, 2, 5, 10), random),
        arguments(generateTestInput(random, 2, 2, 5, 100), random),
        arguments(generateTestInput(random, 2, 2, 10, 10), random),
        arguments(generateTestInput(random, 2, 2, 10, 100), random),
        arguments(generateTestInput(random, 100, 1, 1, 10), random),
        arguments(generateTestInput(random, 100, 1, 1, 100), random),
        arguments(generateTestInput(random, 100, 1, 5, 10), random),
        arguments(generateTestInput(random, 100, 1, 5, 100), random),
        arguments(generateTestInput(random, 100, 5, 5, 10), random),
        arguments(generateTestInput(random, 100, 5, 5, 100), random),
        arguments(generateTestInput(random, 100, 5, 5, 1000), random),
        arguments(generateTestInput(random, 100, 5, 10, 10), random),
        arguments(generateTestInput(random, 100, 5, 10, 100), random),
        arguments(generateTestInput(random, 100, 5, 10, 1000), random),
        arguments(generateTestInput(random, 1000, 1, 5, 10), random),
        arguments(generateTestInput(random, 1000, 1, 5, 100), random),
        arguments(generateTestInput(random, 1000, 1, 5, 1000), random),
        arguments(generateTestInput(random, 1000, 5, 5, 10), random),
        arguments(generateTestInput(random, 1000, 5, 5, 100), random),
        arguments(generateTestInput(random, 1000, 5, 5, 1000), random),
        arguments(generateTestInput(random, 1000, 10, 5, 10), random),
        arguments(generateTestInput(random, 1000, 10, 5, 100), random),
        arguments(generateTestInput(random, 1000, 10, 5, 1000), random),
        arguments(generateTestInput(random, 1000, 10, 5, 10000), random),
        arguments(generateTestInput(random, 1000, 5, 10, 10), random),
        arguments(generateTestInput(random, 1000, 5, 10, 100), random),
        arguments(generateTestInput(random, 1000, 5, 10, 1000), random),
        arguments(generateTestInput(random, 1000, 5, 10, 10000), random));
  }

  // XXX: Improve `source` param name.
  @MethodSource("verifyTestCases")
  @ParameterizedTest
  void verify(ImmutableSetMultimap<Integer, ImmutableSet<String>> source, Random random) {
    Node<Integer> tree = Node.create(source.keySet().asList(), source::get);

    // XXX: Drop.
    // System.out.println(size(tree));

    verifyConstruction(tree, source, random);
  }

  // XXX: Drop.
  // private static <T> int size(Node<T> t) {
  //  return t.values().size()
  //      + t.children().size()
  //      + t.children().values().stream().mapToInt(NodeTest::size).sum();
  // }

  private static void verifyConstruction(
      Node<Integer> tree,
      ImmutableSetMultimap<Integer, ImmutableSet<String>> source,
      Random random) {
    ImmutableSet<String> allPathValues =
        source.values().stream().flatMap(ImmutableSet::stream).collect(toImmutableSet());

    for (Map.Entry<Integer, ImmutableSet<String>> e : source.entries()) {
      verifyReachability(tree, e.getKey(), shuffle(e.getValue(), random), allPathValues, random);
    }
  }

  private static <T> void verifyReachability(
      Node<T> tree,
      T leaf,
      ImmutableSet<String> unorderedEdgesToLeaf,
      ImmutableSet<String> allEdges,
      Random random) {
    String unknownEdge = "unknown";

    assertThat(isReachable(tree, leaf, unorderedEdgesToLeaf)).isTrue();
    assertThat(isReachable(tree, leaf, insertValue(unorderedEdgesToLeaf, unknownEdge, random)))
        .isTrue();
    if (!allEdges.isEmpty()) {
      String knownEdge = selectRandomElement(allEdges, random);
      assertThat(isReachable(tree, leaf, insertValue(unorderedEdgesToLeaf, knownEdge, random)))
          .isTrue();
    }

    // XXX: Strictly speaking this is wrong: these paths _could_ exist.
    // XXX: Implement something better.
    if (!unorderedEdgesToLeaf.isEmpty()) {
      // assertThat(isReachable(tree, leaf, randomStrictSubset(unorderedEdgesToLeaf, random)))
      //    .isFalse();
      // assertThat(
      //        isReachable(
      //            tree,
      //            leaf,
      //            insertValue(
      //                randomStrictSubset(unorderedEdgesToLeaf, random), unknownEdge, random)))
      //    .isFalse();
    }
  }

  private static <T> ImmutableSet<T> shuffle(ImmutableSet<T> values, Random random) {
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

  private static <T> T selectRandomElement(ImmutableSet<T> collection, Random random) {
    return collection.asList().get(random.nextInt(collection.size()));
  }

  // XXX: Use or drop.
  // private static <T> ImmutableSet<T> randomStrictSubset(ImmutableSet<T> values, Random random) {
  //  checkArgument(!values.isEmpty(), "Cannot select strict subset of random collection");
  //
  //  List<T> allValues = new ArrayList<>(values);
  //  Collections.shuffle(allValues, random);
  //  return ImmutableSet.copyOf(allValues.subList(0, random.nextInt(allValues.size())));
  // }

  private static <T> boolean isReachable(
      Node<T> tree, T target, ImmutableSet<String> candidateEdges) {
    Set<T> matches = new HashSet<>();
    tree.collectReachableValues(candidateEdges, matches::add);
    return matches.contains(target);
  }

  private static ImmutableSetMultimap<Integer, ImmutableSet<String>> generateTestInput(
      Random random, int entryCount, int maxPathCount, int maxPathLength, int pathValueDomainSize) {
    return random
        .ints(entryCount)
        .boxed()
        .collect(
            flatteningToImmutableSetMultimap(
                identity(),
                i ->
                    random
                        .ints(random.nextInt(maxPathCount + 1))
                        .mapToObj(
                            p ->
                                random
                                    .ints(random.nextInt(maxPathLength + 1), 0, pathValueDomainSize)
                                    .mapToObj(String::valueOf)
                                    .collect(toImmutableSet()))));
  }
}
