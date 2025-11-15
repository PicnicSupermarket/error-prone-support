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

  ImmutableSet<JsonNode> testObjectMapperValueToTree() {
    return ImmutableSet.of(
        new ObjectMapper().valueToTree("foo"), JsonMapper.shared().valueToTree("bar"));
  }

  ImmutableSet<Number> testObjectMapperConvertValueWithClass() {
    return ImmutableSet.of(
        new ObjectMapper().convertValue("1", Integer.class),
        JsonMapper.shared().convertValue("2.0", Double.class));
  }

  ImmutableSet<Number> testObjectMapperConvertValueWithJavaType() {
    return ImmutableSet.of(
        new ObjectMapper().convertValue("1", SimpleType.constructUnsafe(Integer.class)),
        JsonMapper.shared().convertValue("2.0", SimpleType.constructUnsafe(Double.class)));
  }

  ImmutableSet<Number> testObjectMapperConvertValueWithTypeReference() {
    return ImmutableSet.of(
        new ObjectMapper().convertValue("1", new TypeReference<Integer>() {}),
        JsonMapper.shared().convertValue("2.0", new TypeReference<Double>() {}));
  }
}
