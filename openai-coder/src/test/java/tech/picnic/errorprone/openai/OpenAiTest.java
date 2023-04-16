package tech.picnic.errorprone.openai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static tech.picnic.errorprone.openai.OpenAi.OPENAI_TOKEN_VARIABLE;

import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance(Lifecycle.PER_CLASS)
@EnabledIfSystemProperty(named = OPENAI_TOKEN_VARIABLE, matches = ".*")
final class OpenAiTest {
  private final OpenAi openAi = OpenAi.create();

  @AfterAll
  void tearDown() {
    openAi.close();
  }

  private static Stream<Arguments> requestEditTestCases() {
    return Stream.of(
        arguments(
            """
            class C extends List<String> {}
            """,
            "Add the missing java.util.List import.",
            """
            import java.util.List;

            class C extends List<String> {}
            """));
  }

  @ParameterizedTest
  @MethodSource("requestEditTestCases")
  void requestEdit(String input, String instruction, String expected) {
    assertThat(openAi.requestEdit(input, instruction)).isEqualTo(expected);
  }
}
