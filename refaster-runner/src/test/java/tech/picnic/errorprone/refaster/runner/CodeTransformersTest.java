package tech.picnic.errorprone.refaster.runner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

final class CodeTransformersTest {
  /**
   * Verifies that {@link CodeTransformers#getAllCodeTransformers()} finds the code transformer
   * compiled from {@link FooRules} on the classpath.
   */
  @Test
  void getAllCodeTransformers() {
    assertThat(CodeTransformers.getAllCodeTransformers().keySet())
        .containsExactly("FooRules$SimpleRule");
  }
}
