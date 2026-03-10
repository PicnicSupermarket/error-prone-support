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
    void before(Stream<T> stream) {
      assertThat(stream.findAny()).isEmpty();
    }

    @BeforeTemplate
    void before2(Stream<T> stream) {
      assertThat(stream.findFirst()).isEmpty();
    }

    @BeforeTemplate
    void before3(Stream<T> stream) {
      assertThat(stream.toArray()).isEmpty();
    }

    @BeforeTemplate
    void before4(Stream<T> stream, IntFunction<S[]> generator) {
      assertThat(stream.toArray(generator)).isEmpty();
    }

    @BeforeTemplate
    void before5(Stream<T> stream) {
      assertThat(stream.toList()).isEmpty();
    }

    // XXX: This template assumes that `collector` doesn't completely discard certain values.
    @BeforeTemplate
    void before6(Stream<T> stream, Collector<T, ?, ? extends Iterable<S>> collector) {
      assertThat(stream.collect(collector)).isEmpty();
    }

    // XXX: This template assumes that `collector` doesn't completely discard certain values.
    @BeforeTemplate
    void before7(Stream<T> stream, Collector<T, ?, ? extends Collection<S>> collector) {
      assertThat(stream.collect(collector)).isEmpty();
    }

    // XXX: This template assumes that `collector` doesn't completely discard certain values.
    @BeforeTemplate
    void before8(Stream<T> stream, Collector<T, ?, ? extends List<S>> collector) {
      assertThat(stream.collect(collector)).isEmpty();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Stream<T> stream) {
      assertThat(stream).isEmpty();
    }
  }

  /** Prefer {@code assertThat(stream).isNotEmpty()} over less efficient alternatives. */
  static final class AssertThatIsNotEmpty<T, S> {
    @BeforeTemplate
    void before(Stream<T> stream) {
      assertThat(stream.count()).isNotEqualTo(0);
    }

    @BeforeTemplate
    void before2(Stream<T> stream) {
      assertThat(stream.findAny()).isPresent();
    }

    @BeforeTemplate
    void before3(Stream<T> stream) {
      assertThat(stream.findFirst()).isPresent();
    }

    @BeforeTemplate
    void before4(Stream<T> stream) {
      assertThat(stream.toArray()).isNotEmpty();
    }

    @BeforeTemplate
    void before5(Stream<T> stream, IntFunction<S[]> generator) {
      assertThat(stream.toArray(generator)).isNotEmpty();
    }

    @BeforeTemplate
    void before6(Stream<T> stream) {
      assertThat(stream.toList()).isNotEmpty();
    }

    // XXX: This template assumes that `collector` doesn't completely discard certain values.
    @BeforeTemplate
    void before7(Stream<T> stream, Collector<T, ?, ? extends Iterable<S>> collector) {
      assertThat(stream.collect(collector)).isNotEmpty();
    }

    // XXX: This template assumes that `collector` doesn't completely discard certain values.
    @BeforeTemplate
    void before8(Stream<T> stream, Collector<T, ?, ? extends Collection<S>> collector) {
      assertThat(stream.collect(collector)).isNotEmpty();
    }

    // XXX: This template assumes that `collector` doesn't completely discard certain values.
    @BeforeTemplate
    void before9(Stream<T> stream, Collector<T, ?, ? extends List<S>> collector) {
      assertThat(stream.collect(collector)).isNotEmpty();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Stream<T> stream) {
      assertThat(stream).isNotEmpty();
    }
  }

  /** Prefer {@code assertThat(stream).hasSize(size)} over more contrived alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatHasSize<T> {
    @BeforeTemplate
    AbstractLongAssert<?> before(Stream<T> stream, int size) {
      return assertThat(stream.count()).isEqualTo(size);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<T> after(Stream<T> stream, int size) {
      return assertThat(stream).hasSize(size);
    }
  }

  /** Prefer {@code assertThat(stream).filteredOn(predicate)} over more contrived alternatives. */
  static final class AssertThatFilteredOn<S, T extends S> {
    @BeforeTemplate
    ListAssert<T> before(Stream<T> stream, Predicate<S> predicate) {
      return assertThat(stream.filter(predicate));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<T> after(Stream<T> stream, Predicate<S> predicate) {
      return assertThat(stream).filteredOn(predicate);
    }
  }

  /** Prefer {@code assertThat(stream).noneMatch(predicate)} over more contrived alternatives. */
  static final class AssertThatNoneMatch<S, T extends S> {
    @BeforeTemplate
    void before(Stream<T> stream, Predicate<S> predicate) {
      assertThat(stream).filteredOn(predicate).isEmpty();
    }

    @BeforeTemplate
    void before2(Stream<T> stream, Predicate<S> predicate) {
      Refaster.anyOf(
          assertThat(stream.anyMatch(predicate)).isFalse(),
          assertThat(stream.noneMatch(predicate)).isTrue());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Stream<T> stream, Predicate<S> predicate) {
      assertThat(stream).noneMatch(predicate);
    }
  }

  /** Prefer {@code assertThat(stream).anyMatch(predicate)} over more contrived alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatAnyMatch<S, T extends S> {
    @BeforeTemplate
    ListAssert<T> before(Stream<T> stream, Predicate<S> predicate) {
      return assertThat(stream).filteredOn(predicate).isNotEmpty();
    }

    @BeforeTemplate
    AbstractBooleanAssert<?> before2(Stream<T> stream, Predicate<S> predicate) {
      return Refaster.anyOf(
          assertThat(stream.anyMatch(predicate)).isTrue(),
          assertThat(stream.noneMatch(predicate)).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<T> after(Stream<T> stream, Predicate<S> predicate) {
      return assertThat(stream).anyMatch(predicate);
    }
  }

  /** Prefer {@code assertThat(collection)} over more contrived alternatives. */
  // XXX: Consider moving this rule to a new `AssertJCollectionRules` class.
  @PossibleSourceIncompatibility
  static final class AssertThat<T> {
    @BeforeTemplate
    ListAssert<T> before(Collection<T> collection) {
      return assertThat(collection.stream());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> after(
        Collection<T> collection) {
      return assertThat(collection);
    }
  }
}
