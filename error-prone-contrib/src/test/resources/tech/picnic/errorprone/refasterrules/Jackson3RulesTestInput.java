package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.NullNode;

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

  JsonNode testObjectMapperValueToTree() {
    return new ObjectMapper().readTree(new ObjectMapper().writeValueAsString("foo"));
  }
}
