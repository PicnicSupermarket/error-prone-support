package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import java.util.function.Supplier;
import reactor.core.publisher.Mono;

final class ReactorTemplatesTest implements RefasterTemplateTestCase {
  Mono<Void> testMonoDeferredError() {
    return Mono.error(() -> new IllegalStateException());
  }

  ImmutableSet<Mono<Void>> testMonoErrorSupplier() {
    return ImmutableSet.of(
        Mono.error(((Supplier<RuntimeException>) null)),
        Mono.error(((Supplier<RuntimeException>) null)));
  }
}
