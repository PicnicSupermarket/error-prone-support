package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Collection;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractCollectionAssert;
import org.assertj.core.api.AbstractLongAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;

/** Refaster rules related to AssertJ assertions over {@link Stream}s. */
@OnlineDocumentation
final class AssertJStreamRules {
  private AssertJStreamRules() {}

  /** Prefer {@code assertThat(stream).isEmpty()} over less efficient alternatives. */
  static final class AssertThatIsEmpty<T, S> {
    @BeforeTemplate
    void before(Stream<T> actual) {
      assertThat(actual.findAny()).isEmpty();
    }

    @BeforeTemplate
    void before2(Stream<T> actual) {
      assertThat(actual.findFirst()).isEmpty();
    }

    @BeforeTemplate
    void before3(Stream<T> actual) {
      assertThat(actual.toArray()).isEmpty();
    }

    @BeforeTemplate
    void before4(Stream<T> actual, IntFunction<S[]> function) {
      assertThat(actual.toArray(function)).isEmpty();
    }

    @BeforeTemplate
    void before5(Stream<T> actual) {
      assertThat(actual.toList()).isEmpty();
    }

    // XXX: This template assumes that `collector` doesn't completely discard certain values.
    @BeforeTemplate
    void before6(Stream<T> actual, Collector<T, ?, ? extends Iterable<S>> collector) {
      assertThat(actual.collect(collector)).isEmpty();
    }

    // XXX: This template assumes that `collector` doesn't completely discard certain values.
    @BeforeTemplate
    void before7(Stream<T> actual, Collector<T, ?, ? extends Collection<S>> collector) {
      assertThat(actual.collect(collector)).isEmpty();
    }

    // XXX: This template assumes that `collector` doesn't completely discard certain values.
    @BeforeTemplate
    void before8(Stream<T> actual, Collector<T, ?, ? extends List<S>> collector) {
      assertThat(actual.collect(collector)).isEmpty();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Stream<T> actual) {
      assertThat(actual).isEmpty();
    }
  }

  /** Prefer {@code assertThat(stream).isNotEmpty()} over less efficient alternatives. */
  static final class AssertThatIsNotEmpty<T, S> {
    @BeforeTemplate
    void before(Stream<T> actual) {
      assertThat(actual.count()).isNotEqualTo(0);
    }

    @BeforeTemplate
    void before2(Stream<T> actual) {
      assertThat(actual.findAny()).isPresent();
    }

    @BeforeTemplate
    void before3(Stream<T> actual) {
      assertThat(actual.findFirst()).isPresent();
    }

    @BeforeTemplate
    void before4(Stream<T> actual) {
      assertThat(actual.toArray()).isNotEmpty();
    }

    @BeforeTemplate
    void before5(Stream<T> actual, IntFunction<S[]> function) {
      assertThat(actual.toArray(function)).isNotEmpty();
    }

    @BeforeTemplate
    void before6(Stream<T> actual) {
      assertThat(actual.toList()).isNotEmpty();
    }

    // XXX: This template assumes that `collector` doesn't completely discard certain values.
    @BeforeTemplate
    void before7(Stream<T> actual, Collector<T, ?, ? extends Iterable<S>> collector) {
      assertThat(actual.collect(collector)).isNotEmpty();
    }

    // XXX: This template assumes that `collector` doesn't completely discard certain values.
    @BeforeTemplate
    void before8(Stream<T> actual, Collector<T, ?, ? extends Collection<S>> collector) {
      assertThat(actual.collect(collector)).isNotEmpty();
    }

    // XXX: This template assumes that `collector` doesn't completely discard certain values.
    @BeforeTemplate
    void before9(Stream<T> actual, Collector<T, ?, ? extends List<S>> collector) {
      assertThat(actual.collect(collector)).isNotEmpty();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Stream<T> actual) {
      assertThat(actual).isNotEmpty();
    }
  }

  /** Prefer {@code assertThat(stream).hasSize(size)} over more contrived alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatHasSize<T> {
    @BeforeTemplate
    AbstractLongAssert<?> before(Stream<T> actual, int expected) {
      return assertThat(actual.count()).isEqualTo(expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<T> after(Stream<T> actual, int expected) {
      return assertThat(actual).hasSize(expected);
    }
  }

  /** Prefer {@code assertThat(stream).filteredOn(predicate)} over more contrived alternatives. */
  static final class AssertThatFilteredOn<S, T extends S> {
    @BeforeTemplate
    ListAssert<T> before(Stream<T> actual, Predicate<S> predicate) {
      return assertThat(actual.filter(predicate));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<T> after(Stream<T> actual, Predicate<S> predicate) {
      return assertThat(actual).filteredOn(predicate);
    }
  }

  /** Prefer {@code assertThat(stream).noneMatch(predicate)} over more contrived alternatives. */
  static final class AssertThatNoneMatch<S, T extends S> {
    @BeforeTemplate
    void before(Stream<T> actual, Predicate<S> predicate) {
      assertThat(actual).filteredOn(predicate).isEmpty();
    }

    @BeforeTemplate
    void before2(Stream<T> actual, Predicate<S> predicate) {
      Refaster.anyOf(
          assertThat(actual.anyMatch(predicate)).isFalse(),
          assertThat(actual.noneMatch(predicate)).isTrue());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Stream<T> actual, Predicate<S> predicate) {
      assertThat(actual).noneMatch(predicate);
    }
  }

  /** Prefer {@code assertThat(stream).anyMatch(predicate)} over more contrived alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatAnyMatch<S, T extends S> {
    @BeforeTemplate
    ListAssert<T> before(Stream<T> actual, Predicate<S> predicate) {
      return assertThat(actual).filteredOn(predicate).isNotEmpty();
    }

    @BeforeTemplate
    AbstractBooleanAssert<?> before2(Stream<T> actual, Predicate<S> predicate) {
      return Refaster.anyOf(
          assertThat(actual.anyMatch(predicate)).isTrue(),
          assertThat(actual.noneMatch(predicate)).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<T> after(Stream<T> actual, Predicate<S> predicate) {
      return assertThat(actual).anyMatch(predicate);
    }
  }

  /** Prefer {@code assertThat(collection)} over more contrived alternatives. */
  // XXX: Consider moving this rule to a new `AssertJCollectionRules` class.
  @PossibleSourceIncompatibility
  static final class AssertThat<T> {
    @BeforeTemplate
    ListAssert<T> before(Collection<T> actual) {
      return assertThat(actual.stream());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> after(
        Collection<T> actual) {
      return assertThat(actual);
    }
  }
}
