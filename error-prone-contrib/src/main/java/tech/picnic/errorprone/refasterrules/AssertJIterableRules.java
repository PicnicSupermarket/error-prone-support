package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Iterables;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Collection;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.ObjectAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

@OnlineDocumentation
final class AssertJIterableRules {
  private AssertJIterableRules() {}

  static final class AssertThatIterableIsEmpty<E> {
    @BeforeTemplate
    void before(Iterable<E> iterable) {
      assertThat(iterable.iterator()).isExhausted();
    }

    @BeforeTemplate
    void before2(Iterable<E> iterable) {
        assertThat(iterable).hasSize(0);
    }

    @BeforeTemplate
    void before3(Collection<E> iterable) {
      assertThat(iterable.isEmpty()).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Collection<E> iterable) {
      assertThat(iterable).isEmpty();
    }
  }

  static final class AssertThatIterableIsNotEmpty<E> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Iterable<E> iterable) {
      return assertThat(iterable.iterator()).hasNext();
    }

    @BeforeTemplate
    AbstractAssert<?, ?> before(Collection<E> iterable) {
      return assertThat(iterable.isEmpty()).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> iterable) {
      return assertThat(iterable).isNotEmpty();
    }
  }

  static final class AssertThatIterableSize<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(Iterable<E> iterable) {
      return assertThat(Iterables.size(iterable));
    }

    @BeforeTemplate
    AbstractIntegerAssert<?> before(Collection<E> iterable) {
      return assertThat(iterable.size());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractIntegerAssert<?> after(Iterable<E> iterable) {
      return assertThat(iterable).size();
    }
  }

  // XXX: In practice this rule isn't very useful, as it only matches invocations of
  // `assertThat(E)`. In most cases a more specific overload of `assertThat` is invoked, in which
  // case this rule won't match. Look into a more robust approach.
  static final class AssertThatIterableHasOneElementEqualTo<S, E extends S> {
    @BeforeTemplate
    ObjectAssert<S> before(Iterable<S> iterable, E element) {
      return assertThat(Iterables.getOnlyElement(iterable)).isEqualTo(element);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    IterableAssert<S> after(Iterable<S> iterable, E element) {
      return assertThat(iterable).containsExactly(element);
    }
  }
}
