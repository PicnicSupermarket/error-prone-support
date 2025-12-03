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
        NullNode.getInstance().get(1).asOptional(),
        NullNode.getInstance().path(2).asOptional(),
        Optional.of(NullNode.getInstance().get(3)),
        Optional.ofNullable(NullNode.getInstance().get(4)));
  }

  ImmutableSet<Optional<JsonNode>> testJsonNodeOptionalString() {
    return ImmutableSet.of(
        NullNode.getInstance().get("foo").asOptional(),
        NullNode.getInstance().path("bar").asOptional(),
        Optional.of(NullNode.getInstance().get("baz")),
        Optional.ofNullable(NullNode.getInstance().get("qux")));
  }

  ImmutableSet<JsonNode> testObjectMapperValueToTree() throws IOException {
    return ImmutableSet.of(
        new ObjectMapper().readTree(new ObjectMapper().writeValueAsBytes("foo")),
        new ObjectMapper(new JsonFactory())
            .readTree(new ObjectMapper(new JsonFactory()).writeValueAsString("bar")));
  }

  ImmutableSet<Number> testObjectMapperConvertValueWithClass() throws IOException {
    return ImmutableSet.of(
        new ObjectMapper().readValue(new ObjectMapper().writeValueAsBytes("1"), Integer.class),
        new ObjectMapper(new JsonFactory())
            .readValue(
                new ObjectMapper(new JsonFactory()).writeValueAsString("2.0"), Double.class));
  }

  ImmutableSet<Number> testObjectMapperConvertValueWithJavaType() throws IOException {
    return ImmutableSet.of(
        new ObjectMapper()
            .readValue(
                new ObjectMapper().writeValueAsBytes("1"),
                SimpleType.constructUnsafe(Integer.class)),
        new ObjectMapper(new JsonFactory())
            .readValue(
                new ObjectMapper(new JsonFactory()).writeValueAsString("2.0"),
                SimpleType.constructUnsafe(Double.class)));
  }

  ImmutableSet<Number> testObjectMapperConvertValueWithTypeReference() throws IOException {
    return ImmutableSet.of(
        new ObjectMapper()
            .readValue(new ObjectMapper().writeValueAsBytes("1"), new TypeReference<Integer>() {}),
        new ObjectMapper(new JsonFactory())
            .readValue(
                new ObjectMapper(new JsonFactory()).writeValueAsString("2.0"),
                new TypeReference<Double>() {}));
  }
}
