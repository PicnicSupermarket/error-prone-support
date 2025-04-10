package tech.picnic.errorprone.refaster.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class NodeTest {
  private static Stream<Arguments> collectReachableValuesTestCases() {
    Random random = new Random(0);

    return Stream.of(
            NodeTestCase.generate(0, 0, 0, 0, random),
            NodeTestCase.generate(1, 1, 1, 1, random),
            NodeTestCase.generate(2, 2, 2, 10, random),
            NodeTestCase.generate(10, 2, 5, 10, random),
            NodeTestCase.generate(10, 2, 5, 100, random),
            NodeTestCase.generate(100, 5, 10, 100, random),
            NodeTestCase.generate(100, 5, 10, 1000, random))
        .flatMap(
            testCase -> {
              Node<Integer> tree = testCase.buildTree();
              return testCase
                  .generateTestCaseEntries(random)
                  .map(e -> arguments(tree, e.candidateEdges(), e.reachableValues()));
            });
  }

  @MethodSource("collectReachableValuesTestCases")
  @ParameterizedTest
  void collectReachableValues(
      Node<Integer> tree,
      ImmutableSet<String> candidateEdges,
      Collection<Integer> expectedReachable) {
    List<Integer> actualReachable = new ArrayList<>();
    tree.collectReachableValues(candidateEdges, actualReachable::add);
    assertThat(actualReachable).hasSameElementsAs(expectedReachable);
  }
}
