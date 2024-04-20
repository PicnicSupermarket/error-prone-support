package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.refaster.annotation.Benchmarked;
import tech.picnic.errorprone.refasterrules.ReactorRules.MonoFromOptionalSwitchIfEmpty;

// XXX: Fix warmup and measurements etc.
@SuppressWarnings("FieldCanBeFinal") // XXX: Triggers CompilesWithFix!!!
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(
    jvmArgs = {"-Xms256M", "-Xmx256M"},
    value = 1)
@Warmup(iterations = 1)
@Measurement(iterations = 1)
public class ReactorRulesBenchmarks {
  public static void main(String[] args) throws RunnerException {
    String testRegex = Pattern.quote(ReactorRulesBenchmarks.class.getCanonicalName());
    new Runner(new OptionsBuilder().include(testRegex).addProfiler(GCProfiler.class).build()).run();
  }

  // XXX: What a benchmarked rule could look like.
  @Benchmarked
  static final class MonoFromOptionalSwitchIfEmpty<T> {
    // XXX: These parameters could perhaps be inferred in the common case.
    @Benchmarked.Param private final Optional<String> emptyOptional = Optional.empty();
    @Benchmarked.Param private final Optional<String> nonEmptyOptional = Optional.of("foo");
    @Benchmarked.Param private final Mono<String> emptyMono = Mono.<String>empty().hide();
    @Benchmarked.Param private final Mono<String> nonEmptyMono = Mono.just("bar").hide();

    @BeforeTemplate
    Mono<T> before(Optional<T> optional, Mono<T> mono) {
      return optional.map(Mono::just).orElse(mono);
    }

    @AfterTemplate
    Mono<T> after(Optional<T> optional, Mono<T> mono) {
      return Mono.justOrEmpty(optional).switchIfEmpty(mono);
    }
  }

  // XXX: Variations like this would be generated.
  public static class MonoFromOptionalSwitchIfEmptyBenchmark extends ReactorRulesBenchmarks {
    private final MonoFromOptionalSwitchIfEmpty<String> rule = new MonoFromOptionalSwitchIfEmpty();
    //    private final Optional<String> optional = Optional.of("foo");
    //    private final Mono<String> mono = Mono.just("bar").hide();
    private final Optional<String> optional = Optional.empty();
    private final Mono<String> mono = Mono.<String>empty().hide();
    private final Mono<String> before = rule.before(optional, mono);
    private final Mono<String> after = rule.after(optional, mono);

    @Benchmark
    public Mono<String> before() {
      return rule.before(optional, mono);
    }

    @Benchmark
    public String beforeSubscribe1() {
      return before.block();
    }

    // XXX: In the common case the `x100` variant this doesn't add much. Leave out, at least by
    // default.
    @Benchmark
    @OperationsPerInvocation(100)
    public void beforeSubscribe100(Blackhole bh) {
      for (int i = 0; i < 100; i++) {
        bh.consume(before.block());
      }
    }

    @Benchmark
    public Mono<String> after() {
      return rule.after(optional, mono);
    }

    @Benchmark
    public String afterSubscribe() {
      return after.block();
    }

    @Benchmark
    @OperationsPerInvocation(100)
    public void afterSubscribe100(Blackhole bh) {
      for (int i = 0; i < 100; i++) {
        bh.consume(after.block());
      }
    }
  }
}
