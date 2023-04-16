package tech.picnic.errorprone.openai;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.theokanning.openai.edit.EditRequest;
import com.theokanning.openai.service.OpenAiService;

public class OpenAi implements AutoCloseable {
  // XXX: Rename.
  @VisibleForTesting static final String OPENAI_TOKEN_VARIABLE = "openapi_token";

  private final OpenAiService openAiService;

  private OpenAi(OpenAiService openAiService) {
    this.openAiService = openAiService;
  }

  static OpenAi create() {
    String openApiToken = System.getenv(OPENAI_TOKEN_VARIABLE);
    checkState(openApiToken != null, "Environment variable '%s' not set", OPENAI_TOKEN_VARIABLE);
    return create(openApiToken);
  }

  static OpenAi create(String openAiToken) {
    return new OpenAi(new OpenAiService(openAiToken));
  }

  // XXX: Support multiple alternatives?
  // XXX: Improve error handling.
  String requestEdit(String input, String instruction) {
    return openAiService
        .createEdit(
            EditRequest.builder()
                .input(input)
                .model("code-davinci-edit-001")
                .instruction(instruction)
                .temperature(0.0)
                .n(1)
                .build())
        .getChoices()
        .get(0)
        .getText();
  }

  @Override
  public void close() {
    openAiService.shutdownExecutor();
  }
}
