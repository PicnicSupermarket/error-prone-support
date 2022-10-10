package tech.picnic.errorprone.refastertemplates;

import com.google.common.primitives.Ints;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/** Refaster rules related to expressions dealing with primitives. */
final class PrimitiveTemplates {
  private PrimitiveTemplates() {}

  /** Avoid contrived ways of expressing the "less than" relationship. */
  static final class LessThan {
    @BeforeTemplate
    boolean before(double a, double b) {
      return !(a >= b);
    }

    @AfterTemplate
    boolean after(long a, long b) {
      return a < b;
    }
  }

  /** Avoid contrived ways of expressing the "less than or equal to" relationship. */
  static final class LessThanOrEqualTo {
    @BeforeTemplate
    boolean before(double a, double b) {
      return !(a > b);
    }

    @AfterTemplate
    boolean after(long a, long b) {
      return a <= b;
    }
  }

  /** Avoid contrived ways of expressing the "greater than" relationship. */
  static final class GreaterThan {
    @BeforeTemplate
    boolean before(double a, double b) {
      return !(a <= b);
    }

    @AfterTemplate
    boolean after(long a, long b) {
      return a > b;
    }
  }

  /** Avoid contrived ways of expressing the "greater than or equal to" relationship. */
  static final class GreaterThanOrEqualTo {
    @BeforeTemplate
    boolean before(double a, double b) {
      return !(a < b);
    }

    @AfterTemplate
    boolean after(long a, long b) {
      return a >= b;
    }
  }

  /** Prefer {@link Math#toIntExact(long)} over the Guava alternative. */
  static final class LongToIntExact {
    @BeforeTemplate
    int before(long a) {
      return Ints.checkedCast(a);
    }

    @AfterTemplate
    int after(long a) {
      return Math.toIntExact(a);
    }
  }
}
