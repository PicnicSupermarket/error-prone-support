package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/** Refaster templates related to expressions dealing with {@link ImmutableList}s. */
final class ImmutableListTemplates {
  private ImmutableListTemplates() {}

  /** Prefer {@link ImmutableList#builder()} over the associated constructor. */
  // XXX: This drops generic type information, sometimes leading to non-compilable code. Anything
  // we can do about that?
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

  /** Don't unnecessarily copy an {@link ImmutableList}. */
  static final class ImmutableListCopyOfImmutableList<T> {
    @BeforeTemplate
    ImmutableList<T> before(ImmutableList<T> list) {
      return Refaster.anyOf(ImmutableList.copyOf(list), list.asList());
    }

    @AfterTemplate
    ImmutableList<T> after(ImmutableList<T> list) {
      return list;
    }
  }
}
