package tech.picnic.errorprone.workshop.refasterrules;

import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class WorkshopAssignment5RulesTest implements RefasterRuleCollectionTestCase {
  boolean testStreamDoAllMatch() {
    boolean example = Stream.of("foo").noneMatch(s -> !s.isBlank());
    return Stream.of("bar").noneMatch(b -> !b.startsWith("b"));
  }
}
