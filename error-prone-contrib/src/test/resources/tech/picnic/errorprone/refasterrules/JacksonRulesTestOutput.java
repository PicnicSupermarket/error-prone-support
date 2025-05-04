package tech.picnic.errorprone.refasterrules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class JacksonRulesTest implements RefasterRuleCollectionTestCase {
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
}
