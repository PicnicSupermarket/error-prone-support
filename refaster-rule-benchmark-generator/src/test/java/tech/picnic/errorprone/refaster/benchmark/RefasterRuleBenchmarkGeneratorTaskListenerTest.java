package tech.picnic.errorprone.refaster.benchmark;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// XXX: Add relevant tests also present in `DocumentationGeneratorTaskListenerTest`.
// XXX: Test package placement.
final class RefasterRuleBenchmarkGeneratorTaskListenerTest {
  // XXX: Rename!
  @Test
  void xxx(@TempDir Path outputDirectory) {
    Compilation.compileWithRefasterRuleBenchmarkGenerator(
        outputDirectory,
        "TestCheckerWithoutAnnotation.java",
        """
          import com.google.errorprone.refaster.annotation.AfterTemplate;
          import com.google.errorprone.refaster.annotation.BeforeTemplate;
          import java.util.Optional;
          import reactor.core.publisher.Mono;
          import tech.picnic.errorprone.refaster.annotation.Benchmarked;

          @Benchmarked
          final class MonoFromOptionalSwitchIfEmpty<T> {
            @BeforeTemplate
            Mono<T> before(Optional<T> optional, Mono<T> mono) {
              return optional.map(Mono::just).orElse(mono);
            }

            @AfterTemplate
            Mono<T> after(Optional<T> optional, Mono<T> mono) {
              return Mono.justOrEmpty(optional).switchIfEmpty(mono);
            }
          }
          """);

    assertThat(outputDirectory.toAbsolutePath()).isEmptyDirectory();
  }
}
