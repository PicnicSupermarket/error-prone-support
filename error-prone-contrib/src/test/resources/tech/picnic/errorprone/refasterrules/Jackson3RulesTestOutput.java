package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;

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
}
