package tech.picnic.errorprone.refasterrules;

import java.util.Random;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class RandomRulesTest implements RefasterRuleCollectionTestCase {

  int testRandomNextInt() {
    return (int) (new Random().nextDouble() * 10);
  }

  int testRandomNextIntWithRounding() {
    return (int) Math.round(new Random().nextDouble() * 10);
  }
}
