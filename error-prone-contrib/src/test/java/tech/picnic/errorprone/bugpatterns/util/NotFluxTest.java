package tech.picnic.errorprone.bugpatterns.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

final class NotFluxTest {
  @Test
  void signature() {
    NotFlux<?> notFlux = new NotFlux<>();
    assertThat(notFlux.toIterable()).isNotNull();
    assertThat(notFlux.toStream()).isNotNull();
  }
}
