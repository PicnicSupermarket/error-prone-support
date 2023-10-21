package tech.picnic.errorprone.workshop.refasterrules;

import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class WorkshopAssignment5RulesTest implements RefasterRuleCollectionTestCase {
  boolean testStreamDoAllMatch() {
    boolean example = Stream.of("foo").allMatch(s -> s.isBlank());
    return Stream.of("bar").allMatch(b -> b.startsWith("b"));
  }
}
