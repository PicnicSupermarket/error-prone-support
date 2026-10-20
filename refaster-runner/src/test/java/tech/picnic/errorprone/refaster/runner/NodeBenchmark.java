package tech.picnic.errorprone.refaster.runner;

import static com.google.common.collect.ImmutableListMultimap.flatteningToImmutableListMultimap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.function.Function.identity;
import static org.openjdk.jmh.annotations.Mode.AverageTime;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jakewharton.nopen.annotation.Open;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import tech.picnic.errorprone.refaster.runner.NodeTestCase.NodeTestCaseEntry;

@BenchmarkMode(AverageTime)
@Fork(jvmArgs = {"-Xms1G", "-Xmx1G"})
@Measurement(iterations = 10)
@Open
@OutputTimeUnit(MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5)
public class NodeBenchmark {
  @SuppressWarnings("NullAway" /* Initialized by `@Setup` method. */)
  private ImmutableListMultimap<NodeTestCase<Integer>, NodeTestCaseEntry<Integer>> testCases;

  @SuppressWarnings("NullAway" /* Initialized by `@Setup` method. */)
  private ImmutableMap<NodeTestCase<Integer>, Node<Integer>> trees;

  public static void main(String[] args) throws RunnerException {
    // XXX: Update `ErrorProneRuntimeClasspath` to allow same-package `Class` references.
    @SuppressWarnings("ErrorProneRuntimeClasspath")
    String testRegex = Pattern.quote(NodeBenchmark.class.getCanonicalName());
    new Runner(new OptionsBuilder().include(testRegex).forks(1).build()).run();
  }

  @Setup
  public final void setUp() {
    Random random = new Random(0);

    testCases =
        Stream.of(
                NodeTestCase.generate(100, 5, 10, 10, random),
                NodeTestCase.generate(100, 5, 10, 100, random),
                NodeTestCase.generate(100, 5, 10, 1000, random),
                NodeTestCase.generate(1000, 10, 20, 10, random),
                NodeTestCase.generate(1000, 10, 20, 100, random),
                NodeTestCase.generate(1000, 10, 20, 1000, random),
                NodeTestCase.generate(1000, 10, 20, 10000, random))
            .collect(
                flatteningToImmutableListMultimap(
                    identity(), testCase -> testCase.generateTestCaseEntries(random)));
    trees = Maps.toMap(testCases.keySet(), NodeTestCase::buildTree);
  }

  @Benchmark
  public final void create(Blackhole bh) {
    for (NodeTestCase<Integer> testCase : testCases.keySet()) {
      bh.consume(testCase.buildTree());
    }
  }

  @Benchmark
  public final void collectReachableValues(Blackhole bh) {
    for (Map.Entry<NodeTestCase<Integer>, Node<Integer>> e : trees.entrySet()) {
      for (NodeTestCaseEntry<Integer> testCaseEntry : testCases.get(e.getKey())) {
        e.getValue().collectReachableValues(testCaseEntry.candidateEdges(), bh::consume);
      }
    }
  }
}
