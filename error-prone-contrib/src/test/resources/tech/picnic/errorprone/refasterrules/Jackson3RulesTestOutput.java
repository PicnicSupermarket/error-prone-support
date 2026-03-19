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

  ImmutableSet<Number> testObjectMapperConvertValueClass() {
    return ImmutableSet.of(
        new ObjectMapper().convertValue("foo", Integer.class),
        JsonMapper.shared().convertValue("bar", Double.class));
  }

  ImmutableSet<Number> testObjectMapperConvertValueJavaType() {
    return ImmutableSet.of(
        new ObjectMapper().convertValue("foo", SimpleType.constructUnsafe(Integer.class)),
        JsonMapper.shared().convertValue("bar", SimpleType.constructUnsafe(Double.class)));
  }

  ImmutableSet<Number> testObjectMapperConvertValueTypeReference() {
    return ImmutableSet.of(
        new ObjectMapper().convertValue("foo", new TypeReference<Integer>() {}),
        JsonMapper.shared().convertValue("bar", new TypeReference<Double>() {}));
  }
}
