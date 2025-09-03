package tech.picnic.errorprone.refasterrules;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Optional;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to Jackson expressions and statements. */
@OnlineDocumentation
final class JacksonRules {
  private JacksonRules() {}

  /** Prefer {@link JsonNode#optional(int)} over more contrived alternatives. */
  static final class JsonNodeOptionalInt {
    @BeforeTemplate
    Optional<JsonNode> before(JsonNode node, int index) {
      return Refaster.anyOf(
          node.get(index).asOptional(),
          node.path(index).asOptional(),
          Optional.of(node.get(index)),
          Optional.ofNullable(node.get(index)));
    }

    @AfterTemplate
    Optional<JsonNode> after(JsonNode node, int index) {
      return node.optional(index);
    }
  }

  /** Prefer {@link JsonNode#optional(String)} over more contrived alternatives. */
  static final class JsonNodeOptionalString {
    @BeforeTemplate
    Optional<JsonNode> before(JsonNode node, String fieldName) {
      return Refaster.anyOf(
          node.get(fieldName).asOptional(),
          node.path(fieldName).asOptional(),
          Optional.of(node.get(fieldName)),
          Optional.ofNullable(node.get(fieldName)));
    }

    @AfterTemplate
    Optional<JsonNode> after(JsonNode node, String fieldName) {
      return node.optional(fieldName);
    }
  }
}
