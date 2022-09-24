package tech.picnic.errorprone.refaster.runner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

final class CodeTransformersTest {
  /**
   * Verifies that {@link CodeTransformers#getAllCodeTransformers()} finds the code transformers
   * compiled from {@link FooTemplates} on the classpath.
   */
  @Test
  void getAllCodeTransformers() {
    assertThat(CodeTransformers.getAllCodeTransformers().keySet())
        .containsExactlyInAnyOrder(
            "FooTemplates$StringOfSizeZeroTemplate",
            "FooTemplates$StringOfSizeZeroVerboseTemplate",
            "FooTemplates$StringOfSizeOneTemplate",
            "FooTemplates$ExtraGrouping$StringOfSizeTwoTemplate",
            "FooTemplates$ExtraGrouping$StringOfSizeThreeTemplate");
  }
}
