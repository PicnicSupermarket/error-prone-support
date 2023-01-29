package tech.picnic.errorprone.bugpatterns.util;

import org.junit.jupiter.api.Test;

final class NotFluxTest {
  @SuppressWarnings("UnusedVariable")
  @Test
  void signature() {
    NotFlux<?> notFlux = new NotFlux<>();
    var unusedIterable = notFlux.toIterable();
    var unusedStream = notFlux.toStream();
  }
}
