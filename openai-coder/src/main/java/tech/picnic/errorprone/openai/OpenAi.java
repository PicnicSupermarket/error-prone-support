package tech.picnic.errorprone.openai;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.edit.EditRequest;
import com.theokanning.openai.service.OpenAiService;
import java.time.Duration;

/** A class that exposes a number of high-level OpenAI-based operations. */
// XXX: Make final.
// XXX: Implement exponential backoff.
public final class OpenAi implements AutoCloseable {
  // XXX: Rename.
  @VisibleForTesting static final String OPENAI_TOKEN_VARIABLE = "openapi_token";
  // XXX: Make configurable?
  private static final Duration OPENAI_API_TIMEOUT = Duration.ofSeconds(30);

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
    return new OpenAi(new OpenAiService(openAiToken, OPENAI_API_TIMEOUT));
  }

  // XXX: Support multiple alternatives?
  // XXX: Improve error handling (catch `OpenAiHttpException`, expose its message and the backing
  // HTTP error code).
  String requestEdit(String input, String instruction) {
    return openAiService
        .createEdit(
            EditRequest.builder()
                .input(input)
                .model("code-davinci-edit-001")
                .instruction(instruction)
                .temperature(0.0)
                .build())
        .getChoices()
        .get(0)
        .getText();
  }

  // XXX: Improve error handling, including checking the finish reason.
  String requestChatCompletion(String instruction) {
    return openAiService
        .createChatCompletion(
            ChatCompletionRequest.builder()
                .messages(
                    ImmutableList.of(
                        new ChatMessage("system", "You are an expert Java developer"),
                        new ChatMessage("user", instruction)))
                .model("gpt-3.5-turbo")
                .temperature(0.0)
                .build())
        .getChoices()
        .get(0)
        .getMessage()
        .getContent();
  }

  @Override
  public void close() {
    openAiService.shutdownExecutor();
  }
}
