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
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

@OnlineDocumentation
final class AssertJStreamRules {
  private AssertJStreamRules() {}

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
    void before7(Stream<T> stream, Collector<? super T, ?, ? extends Iterable<S>> collector) {
      assertThat(stream.collect(collector)).isNotEmpty();
    }

    // XXX: This template assumes that `collector` doesn't completely discard certain values.
    @BeforeTemplate
    void before8(Stream<T> stream, Collector<? super T, ?, ? extends Collection<S>> collector) {
      assertThat(stream.collect(collector)).isNotEmpty();
    }

    // XXX: This template assumes that `collector` doesn't completely discard certain values.
    @BeforeTemplate
    void before9(Stream<T> stream, Collector<? super T, ?, ? extends List<S>> collector) {
      assertThat(stream.collect(collector)).isNotEmpty();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Stream<T> stream) {
      assertThat(stream).isNotEmpty();
    }
  }

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

  static final class AssertThatFilteredOn<T> {
    @BeforeTemplate
    ListAssert<T> before(Stream<T> stream, Predicate<? super T> predicate) {
      return assertThat(stream.filter(predicate));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<T> after(Stream<T> stream, Predicate<? super T> predicate) {
      return assertThat(stream).filteredOn(predicate);
    }
  }

  static final class AssertThatNoneMatch<T> {
    @BeforeTemplate
    void before(Stream<T> stream, Predicate<? super T> predicate) {
      assertThat(stream).filteredOn(predicate).isEmpty();
    }

    @BeforeTemplate
    void before2(Stream<T> stream, Predicate<? super T> predicate) {
      Refaster.anyOf(
          assertThat(stream.anyMatch(predicate)).isFalse(),
          assertThat(stream.noneMatch(predicate)).isTrue());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Stream<T> stream, Predicate<? super T> predicate) {
      assertThat(stream).noneMatch(predicate);
    }
  }

  static final class AssertThatAnyMatch<T> {
    @BeforeTemplate
    ListAssert<T> before(Stream<T> stream, Predicate<? super T> predicate) {
      return assertThat(stream).filteredOn(predicate).isNotEmpty();
    }

    @BeforeTemplate
    AbstractBooleanAssert<?> before2(Stream<T> stream, Predicate<? super T> predicate) {
      return Refaster.anyOf(
          assertThat(stream.anyMatch(predicate)).isTrue(),
          assertThat(stream.noneMatch(predicate)).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<T> after(Stream<T> stream, Predicate<? super T> predicate) {
      return assertThat(stream).anyMatch(predicate);
    }
  }

  // XXX: Consider moving this rule to a new `AssertJCollectionRules` class.
  static final class AssertThatCollection<T> {
    @BeforeTemplate
    ListAssert<T> before(Collection<T> collection) {
      return assertThat(collection.stream());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractCollectionAssert<?, ?, T, ?> after(Collection<T> collection) {
      return assertThat(collection);
    }
  }
}
