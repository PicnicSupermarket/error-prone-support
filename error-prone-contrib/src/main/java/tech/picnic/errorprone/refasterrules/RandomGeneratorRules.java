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
   * Prefer {@link RandomGenerator#nextDouble(double)} over more fragile alternatives.
   *
   * <p><strong>Warning:</strong> this rewrite may change the domain of generated values; in
   * particular, the before-template can yield values outside {@code [0, bound)} or even {@link
   * Double#isInfinite() infinity}.
   */
  static final class RandomGeneratorNextDouble {
    @BeforeTemplate
    double before(RandomGenerator randomGenerator, double bound) {
      return Refaster.anyOf(
          randomGenerator.nextDouble() * bound, bound * randomGenerator.nextDouble());
    }

    @AfterTemplate
    double after(RandomGenerator randomGenerator, double bound) {
      return randomGenerator.nextDouble(bound);
    }
  }

  /**
   * Prefer {@link RandomGenerator#nextDouble(double origin, double bound)} over more fragile
   * alternatives.
   *
   * <p><strong>Warning:</strong> this rewrite may change the distribution of generated values; the
   * before-template can silently yield a non-uniform domain.
   */
  // XXX: This rule assumes that `a` is not an expensive or side-effectful expression.
  // XXX: The replacement code throws an `IllegalArgumentException` in more cases than the original
  // code, but only in situations that are likely unintended.
  static final class RandomGeneratorNextDoublePlus {
    @BeforeTemplate
    double before(RandomGenerator randomGenerator, double origin, double bound) {
      return origin + randomGenerator.nextDouble(bound);
    }

    @AfterTemplate
    double after(RandomGenerator randomGenerator, double origin, double bound) {
      return randomGenerator.nextDouble(origin, origin + bound);
    }
  }

  /** Prefer {@link RandomGenerator#nextInt(int)} over more contrived alternatives. */
  static final class RandomGeneratorNextInt {
    @BeforeTemplate
    @SuppressWarnings("RandomGeneratorNextLong" /* This is a more specific template. */)
    int before(RandomGenerator randomGenerator, int bound) {
      return Refaster.anyOf(
          (int) randomGenerator.nextDouble(bound),
          (int) Math.round(randomGenerator.nextDouble(bound)));
    }

    @AfterTemplate
    int after(RandomGenerator randomGenerator, int bound) {
      return randomGenerator.nextInt(bound);
    }
  }

  /**
   * Prefer {@link RandomGenerator#nextInt(int origin, int bound)} over more fragile alternatives.
   *
   * <p><strong>Warning:</strong> this rewrite may change the set of generated values; the
   * before-template can silently yield values outside the intended domain.
   */
  // XXX: This rule assumes that `a` is not an expensive or side-effectful expression.
  // XXX: The replacement code throws an `IllegalArgumentException` in more cases than the original
  // code, but only in situations that are likely unintended.
  static final class RandomGeneratorNextIntPlus {
    @BeforeTemplate
    int before(RandomGenerator randomGenerator, int origin, int bound) {
      return origin + randomGenerator.nextInt(bound);
    }

    @AfterTemplate
    int after(RandomGenerator randomGenerator, int origin, int bound) {
      return randomGenerator.nextInt(origin, origin + bound);
    }
  }

  /**
   * Prefer {@link RandomGenerator#nextLong(long)} over more contrived alternatives.
   *
   * <p><strong>Warning:</strong> for large bounds, the before-template's floating point arithmetic
   * prevents some {@code long} values from being generated.
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
    long before(RandomGenerator randomGenerator, long bound) {
      return Refaster.anyOf(
          (long) randomGenerator.nextDouble((double) bound),
          Math.round(randomGenerator.nextDouble((double) bound)),
          (long) randomGenerator.nextDouble(bound),
          Math.round(randomGenerator.nextDouble(bound)));
    }

    @AfterTemplate
    long after(RandomGenerator randomGenerator, long bound) {
      return randomGenerator.nextLong(bound);
    }
  }

  /**
   * Prefer {@link RandomGenerator#nextLong(long origin, long bound)} over more fragile
   * alternatives.
   *
   * <p><strong>Warning:</strong> this rewrite may change the set of generated values; the
   * before-template can silently yield values outside the intended domain.
   */
  // XXX: This rule assumes that `a` is not an expensive or side-effectful expression.
  // XXX: The replacement code throws an `IllegalArgumentException` in more cases than the original
  // code, but only in situations that are likely unintended.
  static final class RandomGeneratorNextLongPlus {
    @BeforeTemplate
    long before(RandomGenerator randomGenerator, long origin, long bound) {
      return origin + randomGenerator.nextLong(bound);
    }

    @AfterTemplate
    long after(RandomGenerator randomGenerator, long origin, long bound) {
      return randomGenerator.nextLong(origin, origin + bound);
    }
  }
}
