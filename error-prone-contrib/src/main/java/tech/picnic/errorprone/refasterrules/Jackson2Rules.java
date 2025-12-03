package tech.picnic.errorprone.refasterrules;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.io.IOException;
import java.util.Optional;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to Jackson 2.x expressions and statements. */
@OnlineDocumentation
final class Jackson2Rules {
  private Jackson2Rules() {}

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

  /**
   * Prefer {@link ObjectMapper#valueToTree(Object)} over more contrived and less efficient
   * alternatives.
   */
  static final class ObjectMapperValueToTree {
    @BeforeTemplate
    JsonNode before(ObjectMapper objectMapper, Object object) throws IOException {
      return Refaster.anyOf(
          objectMapper.readTree(objectMapper.writeValueAsBytes(object)),
          objectMapper.readTree(objectMapper.writeValueAsString(object)));
    }

    @AfterTemplate
    JsonNode after(ObjectMapper objectMapper, Object object) {
      return objectMapper.valueToTree(object);
    }
  }

  /**
   * Prefer {@link ObjectMapper#convertValue(Object, Class)} over more contrived and less efficient
   * alternatives.
   */
  static final class ObjectMapperConvertValueWithClass<T> {
    @BeforeTemplate
    T before(ObjectMapper objectMapper, Object object, Class<T> valueType) throws IOException {
      return Refaster.anyOf(
          objectMapper.readValue(objectMapper.writeValueAsBytes(object), valueType),
          objectMapper.readValue(objectMapper.writeValueAsString(object), valueType));
    }

    @AfterTemplate
    T after(ObjectMapper objectMapper, Object object, Class<T> valueType) {
      return objectMapper.convertValue(object, valueType);
    }
  }

  /**
   * Prefer {@link ObjectMapper#convertValue(Object, JavaType)} over more contrived and less
   * efficient alternatives.
   */
  static final class ObjectMapperConvertValueWithJavaType<T> {
    @BeforeTemplate
    T before(ObjectMapper objectMapper, Object object, JavaType valueType) throws IOException {
      return Refaster.anyOf(
          objectMapper.readValue(objectMapper.writeValueAsBytes(object), valueType),
          objectMapper.readValue(objectMapper.writeValueAsString(object), valueType));
    }

    @AfterTemplate
    T after(ObjectMapper objectMapper, Object object, JavaType valueType) {
      return objectMapper.convertValue(object, valueType);
    }
  }

  /**
   * Prefer {@link ObjectMapper#convertValue(Object, TypeReference)} over more contrived and less
   * efficient alternatives.
   */
  static final class ObjectMapperConvertValueWithTypeReference<T> {
    @BeforeTemplate
    T before(ObjectMapper objectMapper, Object object, TypeReference<T> valueTypeRef)
        throws IOException {
      return Refaster.anyOf(
          objectMapper.readValue(objectMapper.writeValueAsBytes(object), valueTypeRef),
          objectMapper.readValue(objectMapper.writeValueAsString(object), valueTypeRef));
    }

    @AfterTemplate
    T after(ObjectMapper objectMapper, Object object, TypeReference<T> valueTypeRef) {
      return objectMapper.convertValue(object, valueTypeRef);
    }
  }
}
