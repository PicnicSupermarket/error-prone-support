package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

@OnlineDocumentation
final class AssertJStreamRules {
  private AssertJStreamRules() {}

  static final class AssertThatStreamFilteredOn<T> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Stream<T> stream, Predicate<? super T> predicate) {
      return assertThat(stream.filter(predicate));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractAssert<?, ?> after(Stream<T> stream, Predicate<? super T> predicate) {
      return assertThat(stream).filteredOn(predicate);
    }
  }

  static final class AssertThatStreamNoneMatch<T> {
    @BeforeTemplate
    void before(Stream<T> stream, Predicate<? super T> predicate) {
      assertThat(stream).filteredOn(predicate).isEmpty();
    }

    @BeforeTemplate
    void before2(Stream<T> stream, Predicate<? super T> predicate) {
      assertThat(stream.anyMatch(predicate)).isFalse();
    }

    @BeforeTemplate
    void before3(Stream<T> stream, Predicate<? super T> predicate) {
      assertThat(stream.noneMatch(predicate)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Stream<T> stream, Predicate<? super T> predicate) {
      assertThat(stream).noneMatch(predicate);
    }
  }

  static final class AssertThatStreamAnyMatch<T> {
    @BeforeTemplate
    void before(Stream<T> stream, Predicate<? super T> predicate) {
      assertThat(stream).filteredOn(predicate).isNotEmpty();
    }

    @BeforeTemplate
    void before2(Stream<T> stream, Predicate<? super T> predicate) {
      assertThat(stream.anyMatch(predicate)).isTrue();
    }

    @BeforeTemplate
    void before3(Stream<T> stream, Predicate<? super T> predicate) {
      assertThat(stream.noneMatch(predicate)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Stream<T> stream, Predicate<? super T> predicate) {
      assertThat(stream).anyMatch(predicate);
    }
  }

  static final class AssertThatCollectionStream<E> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Collection<E> collection) {
      return assertThat(collection.stream());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractAssert<?, ?> after(Collection<E> collection) {
      return assertThat(collection);
    }
  }
}
