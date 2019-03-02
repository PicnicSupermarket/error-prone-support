package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/** Refaster templates related to expressions dealing with {@link ImmutableList}s. */
final class ImmutableListTemplates {
  private ImmutableListTemplates() {}

  static final class ImmutableListBuilder<T> {
    @BeforeTemplate
    ImmutableList.Builder<T> before() {
      return new ImmutableList.Builder<>();
    }

    @AfterTemplate
    ImmutableList.Builder<T> after() {
      return ImmutableList.builder();
    }
  }

  /** Don't call {@link ImmutableList#asList()}; it is a no-op. */
  static final class ImmutableListAsList<T> {
    @BeforeTemplate
    ImmutableList<T> before(ImmutableList<T> list) {
      return list.asList();
    }

    @AfterTemplate
    ImmutableList<T> after(ImmutableList<T> list) {
      return list;
    }
  }
}
