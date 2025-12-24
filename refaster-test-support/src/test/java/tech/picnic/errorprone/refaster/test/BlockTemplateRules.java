package tech.picnic.errorprone.refaster.test;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.function.Predicate;

/** Refaster rule collection with BlockTemplate examples. */
final class BlockTemplateRules {
  private BlockTemplateRules() {}

  static final class ThrowIllegalArgumentException {
    @BeforeTemplate
    void before(boolean condition) {
      if (condition) {
        throw new IllegalArgumentException();
      }
    }

    @AfterTemplate
    void after(boolean condition) {
      if (!condition) {
        throw new IllegalArgumentException();
      }
    }
  }

  static final class PredicateTest<T> {
    @BeforeTemplate
    boolean before(Predicate<T> predicate, T value) {
      return predicate.test(value);
    }

    @AfterTemplate
    boolean after(Predicate<T> predicate, T value) {
      return predicate.test(value);
    }
  }
}
