package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.random.RandomGenerator;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link RandomGenerator} instances. */
// XXX: Consider introducing an Error Prone bug pattern that identifies expressions of the form `c +
// r.next{Int,Long,Double}(b)` for literals `c` and `b`, and replaces them with
// `r.next{Int,Long,Double}(c, s)` where `s = c + b`. Similarly with the operands commuted, and
// for the latter variant also for operator `-`.
@OnlineDocumentation
final class RandomGeneratorRules {
  private RandomGeneratorRules() {}

  /**
   * Prefer {@link RandomGenerator#nextDouble(double)} over alternatives that yield a smaller domain
   * of values and may result in {@link Double#isInfinite() inifinity}.
   */
  static final class RandomGeneratorNextDouble {
    @BeforeTemplate
    double before(RandomGenerator random, double bound) {
      return Refaster.anyOf(random.nextDouble() * bound, bound * random.nextDouble());
    }

    @AfterTemplate
    double after(RandomGenerator random, double bound) {
      return random.nextDouble(bound);
    }
  }

  /**
   * Prefer {@link RandomGenerator#nextDouble(double origin, double bound)} over more contrived
   * alternatives.
   */
  static final class RandomGeneratorNextDoubleWithOrigin {
    @BeforeTemplate
    double before(RandomGenerator random, double a, double b) {
      return a + random.nextDouble(b);
    }

    @AfterTemplate
    double after(RandomGenerator random, double a, double b) {
      return random.nextDouble(a, a + b);
    }
  }

  /** Prefer {@link RandomGenerator#nextInt(int)} over more contrived alternatives. */
  static final class RandomGeneratorNextInt {
    @BeforeTemplate
    @SuppressWarnings("RandomGeneratorNextLong" /* This is a more specific template. */)
    int before(RandomGenerator random, int bound) {
      return Refaster.anyOf(
          (int) random.nextDouble(bound), (int) Math.round(random.nextDouble(bound)));
    }

    @AfterTemplate
    int after(RandomGenerator random, int bound) {
      return random.nextInt(bound);
    }
  }

  /**
   * Prefer {@link RandomGenerator#nextInt(int origin, int bound)} over more contrived alternatives.
   */
  static final class RandomGeneratorNextIntWithOrigin {
    @BeforeTemplate
    int before(RandomGenerator random, int a, int b) {
      return a + random.nextInt(b);
    }

    @AfterTemplate
    int after(RandomGenerator random, int a, int b) {
      return random.nextInt(a, a + b);
    }
  }

  /**
   * Prefer {@link RandomGenerator#nextLong(long)} over more contrived alternatives.
   *
   * <p>Additionally, for large bounds, the unnecessary floating point arithmetic prevents some
   * {@code long} values from being generated.
   */
  static final class RandomGeneratorNextLong {
    // XXX: By including expressions with and without a cast from `long` to `double`, we cater both
    // to expressions that adhere to Error Prone's `LongDoubleConversion` check, and to expressions
    // that don't.
    @BeforeTemplate
    @SuppressWarnings({
      "java:S1905" /* This violation will be rewritten. */,
      "LongDoubleConversion" /* This violation will be rewritten. */,
      "z-key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"
    })
    long before(RandomGenerator random, long bound) {
      return Refaster.anyOf(
          (long) random.nextDouble((double) bound),
          Math.round(random.nextDouble((double) bound)),
          (long) random.nextDouble(bound),
          Math.round(random.nextDouble(bound)));
    }

    @AfterTemplate
    long after(RandomGenerator random, long bound) {
      return random.nextLong(bound);
    }
  }

  /**
   * Prefer {@link RandomGenerator#nextLong(long origin, long bound)} over more contrived
   * alternatives.
   */
  static final class RandomGeneratorNextLongWithOrigin {
    @BeforeTemplate
    long before(RandomGenerator random, long a, long b) {
      return a + random.nextLong(b);
    }

    @AfterTemplate
    long after(RandomGenerator random, long a, long b) {
      return random.nextLong(a, a + b);
    }
  }
}
