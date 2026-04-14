package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Optional;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Refaster rules related to Jackson 3.x expressions and statements. */
@OnlineDocumentation
final class Jackson3Rules {
  private Jackson3Rules() {}

  /** Prefer {@link JsonNode#optional(int)} over more contrived alternatives. */
  static final class JsonNodeOptionalInt {
    @BeforeTemplate
    Optional<JsonNode> before(JsonNode jsonNode, int index) {
      return Refaster.anyOf(
          jsonNode.get(index).asOptional(),
          jsonNode.path(index).asOptional(),
          Optional.of(jsonNode.get(index)),
          Optional.ofNullable(jsonNode.get(index)));
    }

    @AfterTemplate
    Optional<JsonNode> after(JsonNode jsonNode, int index) {
      return jsonNode.optional(index);
    }
  }

  /** Prefer {@link JsonNode#optional(String)} over more contrived alternatives. */
  static final class JsonNodeOptionalString {
    @BeforeTemplate
    Optional<JsonNode> before(JsonNode jsonNode, String propertyName) {
      return Refaster.anyOf(
          jsonNode.get(propertyName).asOptional(),
          jsonNode.path(propertyName).asOptional(),
          Optional.of(jsonNode.get(propertyName)),
          Optional.ofNullable(jsonNode.get(propertyName)));
    }

    @AfterTemplate
    Optional<JsonNode> after(JsonNode jsonNode, String propertyName) {
      return jsonNode.optional(propertyName);
    }
  }

  /** Prefer {@link ObjectMapper#valueToTree(Object)} over less efficient alternatives. */
  static final class ObjectMapperValueToTree {
    @BeforeTemplate
    JsonNode before(ObjectMapper objectMapper, Object fromValue) {
      return Refaster.anyOf(
          objectMapper.readTree(objectMapper.writeValueAsBytes(fromValue)),
          objectMapper.readTree(objectMapper.writeValueAsString(fromValue)));
    }

    @AfterTemplate
    JsonNode after(ObjectMapper objectMapper, Object fromValue) {
      return objectMapper.valueToTree(fromValue);
    }
  }

  /** Prefer {@link ObjectMapper#convertValue(Object, Class)} over less efficient alternatives. */
  static final class ObjectMapperConvertValueClass<T> {
    @BeforeTemplate
    T before(ObjectMapper objectMapper, Object fromValue, Class<T> toValueType) {
      return Refaster.anyOf(
          objectMapper.readValue(objectMapper.writeValueAsBytes(fromValue), toValueType),
          objectMapper.readValue(objectMapper.writeValueAsString(fromValue), toValueType));
    }

    @AfterTemplate
    T after(ObjectMapper objectMapper, Object fromValue, Class<T> toValueType) {
      return objectMapper.convertValue(fromValue, toValueType);
    }
  }

  /**
   * Prefer {@link ObjectMapper#convertValue(Object, JavaType)} over less efficient alternatives.
   */
  static final class ObjectMapperConvertValueJavaType<T> {
    @BeforeTemplate
    T before(ObjectMapper objectMapper, Object fromValue, JavaType toValueType) {
      return Refaster.anyOf(
          objectMapper.readValue(objectMapper.writeValueAsBytes(fromValue), toValueType),
          objectMapper.readValue(objectMapper.writeValueAsString(fromValue), toValueType));
    }

    @AfterTemplate
    T after(ObjectMapper objectMapper, Object fromValue, JavaType toValueType) {
      return objectMapper.convertValue(fromValue, toValueType);
    }
  }

  /**
   * Prefer {@link ObjectMapper#convertValue(Object, TypeReference)} over less efficient
   * alternatives.
   */
  static final class ObjectMapperConvertValueTypeReference<T> {
    @BeforeTemplate
    T before(ObjectMapper objectMapper, Object fromValue, TypeReference<T> toValueTypeRef) {
      return Refaster.anyOf(
          objectMapper.readValue(objectMapper.writeValueAsBytes(fromValue), toValueTypeRef),
          objectMapper.readValue(objectMapper.writeValueAsString(fromValue), toValueTypeRef));
    }

    @AfterTemplate
    T after(ObjectMapper objectMapper, Object fromValue, TypeReference<T> toValueTypeRef) {
      return objectMapper.convertValue(fromValue, toValueTypeRef);
    }
  }
}
