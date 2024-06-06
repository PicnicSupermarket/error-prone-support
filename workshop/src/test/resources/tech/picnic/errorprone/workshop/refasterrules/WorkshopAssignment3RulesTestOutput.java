package tech.picnic.errorprone.workshop.refasterrules;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.concurrent.ThreadLocalRandom;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class WorkshopAssignment3RulesTest implements RefasterRuleCollectionTestCase {
  void testCheckArgumentWithoutMessage() {
    checkArgument("foo".isEmpty());
    checkArgument(ThreadLocalRandom.current().nextBoolean());
  }

  void testCheckArgumentWithMessage() {
    checkArgument("foo".isEmpty(), "The string is not empty");
    checkArgument(
        ThreadLocalRandom.current().nextBoolean(),
        "The rule should be able rewrite all kinds of messages ;).");
  }
}
