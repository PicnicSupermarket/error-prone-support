package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.random.RandomGenerator;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link RandomGenerator} instances. */
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
      return random.nextDouble() * bound;
    }

    @AfterTemplate
    double after(RandomGenerator random, double bound) {
      return random.nextDouble(bound);
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
    @SuppressWarnings("LongDoubleConversion" /* This violation will be rewritten. */)
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
}
