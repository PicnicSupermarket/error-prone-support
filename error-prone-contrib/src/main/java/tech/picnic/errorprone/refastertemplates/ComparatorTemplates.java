package tech.picnic.errorprone.refastertemplates;

import static java.util.function.Function.identity;

import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Comparator;
import java.util.function.Function;

/** Refaster templates related to expressions dealing with {@link Comparator}s. */
final class ComparatorTemplates {
  private ComparatorTemplates() {}

  /** Prefer {@link Comparator#naturalOrder()} over more complicated constructs. */
  // XXX: Drop the `Refaster.anyOf` if/when we decide to rewrite one to the other.
  static final class NaturalOrderComparator<T extends Comparable<? super T>> {
    @BeforeTemplate
    Comparator<T> before() {
      return Refaster.anyOf(Comparator.comparing(Refaster.anyOf(identity(), v -> v)));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    Comparator<T> after() {
      return Comparator.naturalOrder();
    }
  }

  /**
   * Where applicable, prefer {@link Comparator#naturalOrder()} over {@link Function#identity()}, as
   * it more clearly states intent.
   */
  static final class NaturalOrderComparatorFallback<T extends Comparable<? super T>> {
    @BeforeTemplate
    Comparator<T> before(Comparator<T> cmp) {
      return cmp.thenComparing(Refaster.anyOf(identity(), v -> v));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    Comparator<T> after(Comparator<T> cmp) {
      return cmp.thenComparing(Comparator.naturalOrder());
    }
  }

  /** Prefer {@link Comparator#reverseOrder()} over more complicated constructs. */
  static final class ReverseOrder<T extends Comparable<? super T>> {
    @BeforeTemplate
    Comparator<T> before() {
      return Comparator.<T>naturalOrder().reversed();
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    Comparator<T> after() {
      return Comparator.reverseOrder();
    }
  }
}
