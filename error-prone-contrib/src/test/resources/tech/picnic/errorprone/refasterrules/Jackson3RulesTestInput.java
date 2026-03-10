package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.type.SimpleType;

final class Jackson3RulesTest implements RefasterRuleCollectionTestCase {
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

  ImmutableSet<JsonNode> testObjectMapperValueToTree() {
    return ImmutableSet.of(
        new ObjectMapper().readTree(new ObjectMapper().writeValueAsBytes("foo")),
        JsonMapper.shared().readTree(JsonMapper.shared().writeValueAsString("bar")));
  }

  ImmutableSet<Number> testObjectMapperConvertValueClass() {
    return ImmutableSet.of(
        new ObjectMapper().readValue(new ObjectMapper().writeValueAsBytes("foo"), Integer.class),
        JsonMapper.shared().readValue(JsonMapper.shared().writeValueAsString("bar"), Double.class));
  }

  ImmutableSet<Number> testObjectMapperConvertValueJavaType() {
    return ImmutableSet.of(
        new ObjectMapper()
            .readValue(
                new ObjectMapper().writeValueAsBytes("foo"),
                SimpleType.constructUnsafe(Integer.class)),
        JsonMapper.shared()
            .readValue(
                JsonMapper.shared().writeValueAsString("bar"),
                SimpleType.constructUnsafe(Double.class)));
  }

  ImmutableSet<Number> testObjectMapperConvertValueTypeReference() {
    return ImmutableSet.of(
        new ObjectMapper()
            .readValue(
                new ObjectMapper().writeValueAsBytes("foo"), new TypeReference<Integer>() {}),
        JsonMapper.shared()
            .readValue(
                JsonMapper.shared().writeValueAsString("bar"), new TypeReference<Double>() {}));
  }
}
