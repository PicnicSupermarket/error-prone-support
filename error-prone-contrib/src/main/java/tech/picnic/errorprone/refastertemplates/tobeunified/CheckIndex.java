package tech.picnic.errorprone.refastertemplates.tobeunified;

import com.google.common.base.Preconditions;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Objects;

/** Prefer {@link Objects#checkIndex(int, int)} over the Guava alternative. */
final class CheckIndex {
  @BeforeTemplate
  int before(int index, int size) {
    return Preconditions.checkElementIndex(index, size);
  }

  @AfterTemplate
  int after(int index, int size) {
    return Objects.checkIndex(index, size);
  }
}
