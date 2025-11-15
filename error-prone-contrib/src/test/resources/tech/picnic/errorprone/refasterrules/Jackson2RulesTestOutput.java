package tech.picnic.errorprone.refasterrules;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Optional;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class Jackson2RulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<Optional<JsonNode>> testJsonNodeOptionalInt() {
    return ImmutableSet.of(
        NullNode.getInstance().optional(1),
        NullNode.getInstance().optional(2),
        NullNode.getInstance().optional(3),
        NullNode.getInstance().optional(4));
  }

  ImmutableSet<Optional<JsonNode>> testJsonNodeOptionalString() {
    return ImmutableSet.of(
        NullNode.getInstance().optional("foo"),
        NullNode.getInstance().optional("bar"),
        NullNode.getInstance().optional("baz"),
        NullNode.getInstance().optional("qux"));
  }

  ImmutableSet<JsonNode> testObjectMapperValueToTree() throws IOException {
    return ImmutableSet.of(
        new ObjectMapper().valueToTree("foo"),
        new ObjectMapper(new JsonFactory()).valueToTree("bar"));
  }

  ImmutableSet<Number> testObjectMapperConvertValueWithClass() throws IOException {
    return ImmutableSet.of(
        new ObjectMapper().convertValue("1", Integer.class),
        new ObjectMapper(new JsonFactory()).convertValue("2.0", Double.class));
  }

  ImmutableSet<Number> testObjectMapperConvertValueWithJavaType() throws IOException {
    return ImmutableSet.of(
        new ObjectMapper().convertValue("1", SimpleType.constructUnsafe(Integer.class)),
        new ObjectMapper(new JsonFactory())
            .convertValue("2.0", SimpleType.constructUnsafe(Double.class)));
  }

  ImmutableSet<Number> testObjectMapperConvertValueWithTypeReference() throws IOException {
    return ImmutableSet.of(
        new ObjectMapper().convertValue("1", new TypeReference<Integer>() {}),
        new ObjectMapper(new JsonFactory()).convertValue("2.0", new TypeReference<Double>() {}));
  }
}
