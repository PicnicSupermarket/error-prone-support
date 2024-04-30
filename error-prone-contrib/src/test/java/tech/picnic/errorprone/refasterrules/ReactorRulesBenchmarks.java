package tech.picnic.errorprone.refasterrules;

import static org.openjdk.jmh.results.format.ResultFormatType.JSON;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.refaster.annotation.Benchmarked;

// XXX: Fix warmup and measurements etc.
// XXX: Flag cases where the `before` code is faster, taking into account variation.
@SuppressWarnings("FieldCanBeFinal") // XXX: Triggers CompilesWithFix!!!
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(
    jvmArgs = {"-Xms256M", "-Xmx256M"},
    value = 1)
@Warmup(iterations = 1)
@Measurement(iterations = 10, time = 1)
public class ReactorRulesBenchmarks {
  public static void main(String[] args) throws RunnerException {
    String testRegex = Pattern.quote(ReactorRulesBenchmarks.class.getCanonicalName());
    new Runner(
            new OptionsBuilder()
                .include(testRegex)
                .addProfiler(GCProfiler.class)
                .resultFormat(JSON) // XXX: Review.
                .build())
        .run();
  }

  // XXX: What a benchmarked rule could look like.
  @Benchmarked
  static final class MonoFromOptionalSwitchIfEmpty<T> {
    // XXX: These parameters could perhaps be inferred in the common case.
    @Benchmarked.Param private final Optional<String> emptyOptional = Optional.empty();
    @Benchmarked.Param private final Optional<String> nonEmptyOptional = Optional.of("foo");
    @Benchmarked.Param private final Mono<String> emptyMono = Mono.<String>empty().hide();
    @Benchmarked.Param private final Mono<String> nonEmptyMono = Mono.just("bar").hide();

    // XXX: Dropped to hide this class from `RefasterRuleCompilerTaskListener`: @BeforeTemplate
    Mono<T> before(Optional<T> optional, Mono<T> mono) {
      return optional.map(Mono::just).orElse(mono);
    }

    @AfterTemplate
    Mono<T> after(Optional<T> optional, Mono<T> mono) {
      return Mono.justOrEmpty(optional).switchIfEmpty(mono);
    }

    // XXX: Methods such as this one could perhaps be inferred in the common case.
    @Benchmarked.OnResult
    T subscribe(Mono<T> mono) {
      return mono.block();
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

    @Benchmark
    public Mono<String> after() {
      return rule.after(optional, mono);
    }

    @Benchmark
    public String afterSubscribe() {
      return after.block();
    }
  }

  /////////////////////////////

  abstract static class MonoFlatMapToFlux<T, S> {
    @Placeholder(allowsIdentity = true)
    abstract Mono<S> transformation(@MayOptionallyUse T value);

    // XXX: Dropped to hide this class from `RefasterRuleCompilerTaskListener`: @BeforeTemplate
    Flux<S> before(Mono<T> mono) {
      return mono.flatMapMany(v -> transformation(v));
    }

    @AfterTemplate
    Flux<S> after(Mono<T> mono) {
      return mono.flatMap(v -> transformation(v)).flux();
    }

    @Benchmarked.OnResult
    Long subscribe(Flux<T> flux) {
      return flux.count().block();
    }

    @Benchmarked.MinimalPlaceholder
    <X> Mono<X> identity(X value) {
      return Mono.just(value);
    }
  }

  public static class MonoFlatMapToFluxBenchmarks extends ReactorRulesBenchmarks {
    abstract static class Rule<T, S> {
      abstract Mono<S> transformation(T value);

      Flux<S> before(Mono<T> mono) {
        return mono.flatMapMany(v -> transformation(v));
      }

      Flux<S> after(Mono<T> mono) {
        return mono.flatMap(v -> transformation(v)).flux();
      }
    }

    // XXX: For variantions, we could use `@Param(strings)` indirection, like in
    // https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_35_Profilers.java

    // XXX: Or we can make the benchmark abstract and have subclasses for each variant:
    // https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_24_Inheritance.java

    public static class MonoFlatMapToFluxStringStringBenchmark extends MonoFlatMapToFluxBenchmarks {
      private final Rule<String, String> rule =
          new Rule<>() {
            @Override
            Mono<String> transformation(String value) {
              return Mono.just(value);
            }
          };
      private final Mono<String> mono = Mono.just("foo");
      private final Flux<String> before = rule.before(mono);
      private final Flux<String> after = rule.after(mono);

      @Benchmark
      public Flux<String> before() {
        return rule.before(mono);
      }

      @Benchmark
      public Flux<String> after() {
        return rule.after(mono);
      }

      @Benchmark
      public Long onResultOfBefore() {
        return before.count().block();
      }

      @Benchmark
      public Long onResultOfAfter() {
        return after.count().block();
      }
    }
  }
}
