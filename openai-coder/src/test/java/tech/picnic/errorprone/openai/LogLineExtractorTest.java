package tech.picnic.errorprone.openai;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableSet;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class LogLineExtractorTest {
  // XXX: Rename / generalize tests.
  private final LogLineExtractor logLineExtractor =
      LogLineExtractor.mavenErrorAndWarningExtractor();

  private static Stream<Arguments> extractTestCases() {
    /* { logs, expected } */
    return Stream.of(
        arguments("""
            Line without log level
            """, ImmutableSet.of()),
        arguments("""
            [INFO] Line with log level
            """, ImmutableSet.of()),
        arguments(
            """
            [WARNING] Line with warning
            """,
            ImmutableSet.of("Line with warning")),
        arguments(
            """
            [ERROR] Line with error
            """,
            ImmutableSet.of("Line with error")),
        arguments(
            """
            [INFO] Info 1
            [INFO] Info 2
            [WARNING] Warning 1
            [ERROR] Error 1
            [INFO] Info 3
            """,
            ImmutableSet.of("Warning 1", "Error 1")),
        arguments(
            """
            [INFO] Info 1
            Info line 1, continued
            [INFO] Info 2
            [WARNING] Warning 1
            Warning line 1, continued
            [ERROR] Error 1
            Error line 1, continued
            Error line 1, continued still
            [INFO] Info 3
            [WARNING] Warning 2
            """,
            ImmutableSet.of(
                """
                Warning 1
                Warning line 1, continued""",
                """
                Error 1
                Error line 1, continued
                Error line 1, continued still""",
                """
                Warning 2""")));
  }

  @ParameterizedTest
  @MethodSource("extractTestCases")
  void extract(String logs, ImmutableSet<String> expected) throws IOException {
    assertThat(logLineExtractor.extract(new ByteArrayInputStream(logs.getBytes(UTF_8))))
        .containsExactlyElementsOf(expected);
  }
}
