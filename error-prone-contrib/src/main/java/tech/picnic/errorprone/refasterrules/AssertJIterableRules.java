package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Iterables;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Collection;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.AbstractIterableSizeAssert;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.IteratorAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.ObjectEnumerableAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;

/** Refaster rules related to AssertJ assertions over {@link Iterable}s. */
@OnlineDocumentation
final class AssertJIterableRules {
  private AssertJIterableRules() {}

  /** Prefer {@code assertThat(iterable).isEmpty()} over more contrived alternatives. */
  static final class AssertThatIsEmpty<E> {
    @BeforeTemplate
    void before(Iterable<E> actual) {
      assertThat(actual.iterator()).isExhausted();
    }

    @BeforeTemplate
    void before(Collection<E> actual) {
      assertThat(actual.isEmpty()).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Iterable<E> actual) {
      assertThat(actual).isEmpty();
    }
  }

  /** Prefer {@code assertThat(iterable).isNotEmpty()} over more contrived alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatIsNotEmpty<E> {
    @BeforeTemplate
    IteratorAssert<E> before(Iterable<E> actual) {
      return assertThat(actual.iterator()).hasNext();
    }

    @BeforeTemplate
    AbstractBooleanAssert<?> before(Collection<E> actual) {
      return assertThat(actual.isEmpty()).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> actual) {
      return assertThat(actual).isNotEmpty();
    }
  }

  /** Prefer {@code assertThat(iterable).size()} over non-JDK or more contrived alternatives. */
  static final class AssertThatSize<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(Iterable<E> actual) {
      return assertThat(Iterables.size(actual));
    }

    @BeforeTemplate
    AbstractIntegerAssert<?> before(Collection<E> actual) {
      return assertThat(actual.size());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractIterableSizeAssert<IterableAssert<E>, Iterable<? extends E>, E, ObjectAssert<E>> after(
        Iterable<E> actual) {
      return assertThat(actual).size();
    }
  }

  /** Prefer {@link ObjectEnumerableAssert#contains(Object[])} over less explicit alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatContains<E> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Collection<E> actual, E element) {
      return assertThat(actual.contains(element)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> actual, E element) {
      return assertThat(actual).contains(element);
    }
  }

  /**
   * Prefer {@link ObjectEnumerableAssert#doesNotContain(Object[])} over less explicit alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatDoesNotContain<E> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Collection<E> actual, E element) {
      return assertThat(actual.contains(element)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> actual, E element) {
      return assertThat(actual).doesNotContain(element);
    }
  }

  /**
   * Prefer {@link AbstractIterableAssert#containsAll(Iterable)} over less explicit alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatContainsAll<E> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Collection<E> actual, Collection<? extends E> iterable) {
      return assertThat(actual.containsAll(iterable)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> actual, Collection<? extends E> iterable) {
      return assertThat(actual).containsAll(iterable);
    }
  }

  /**
   * Prefer {@code assertThat(iterable).containsExactly(element)} over more contrived alternatives.
   */
  // XXX: In practice this rule isn't very useful, as it only matches invocations of
  // `assertThat(E)`. In most cases a more specific overload of `assertThat` is invoked, in which
  // case this rule won't match. Look into a more robust approach.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsExactly<S, E extends S> {
    @BeforeTemplate
    ObjectAssert<S> before(Iterable<S> actual, E expected) {
      return assertThat(Iterables.getOnlyElement(actual)).isEqualTo(expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    IterableAssert<S> after(Iterable<S> actual, E expected) {
      return assertThat(actual).containsExactly(expected);
    }
  }
}
