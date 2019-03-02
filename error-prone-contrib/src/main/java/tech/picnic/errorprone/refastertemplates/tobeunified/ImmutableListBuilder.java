package tech.picnic.errorprone.refastertemplates.tobeunified;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

final class ImmutableListBuilder<T> {
  @BeforeTemplate
  ImmutableList.Builder<T> before() {
    return new ImmutableList.Builder<>();
  }

  @AfterTemplate
  ImmutableList.Builder<T> after() {
    return ImmutableList.builder();
  }
}
