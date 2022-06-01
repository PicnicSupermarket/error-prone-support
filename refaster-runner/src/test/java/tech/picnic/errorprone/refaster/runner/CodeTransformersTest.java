package tech.picnic.errorprone.refaster.runner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

final class CodeTransformersTest {
  /**
   * Verifies that {@link CodeTransformers#getAllCodeTransformers()} finds at least one code
   * transformer on the classpath.
   *
   * <p>It is expected that the template collection from this package, {@link FooTemplates}, is one
   * of these transformers.
   */
  @Test
  void loadAllCodeTransformers() {
    assertThat(CodeTransformers.getAllCodeTransformers().keySet())
        .contains("FooTemplates$SimpleTemplate");
  }
}
