package tech.picnic.errorprone.workshop.refasterrules;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import java.util.stream.Stream;

/** Refaster rules for the fifth assignment of the workshop. */
final class WorkshopAssignment5Rules {
  private WorkshopAssignment5Rules() {}

  abstract static class StreamDoAllMatch<T> {
    @Placeholder
    abstract boolean test(@MayOptionallyUse T element);

    @BeforeTemplate
    boolean before(Stream<T> stream) {
      return stream.noneMatch(v -> !test(v));
    }

    @AfterTemplate
    boolean after(Stream<T> stream) {
      return stream.allMatch(v -> test(v));
    }
  }
}
