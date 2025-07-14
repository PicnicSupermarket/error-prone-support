package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.security.SecureRandom;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class RandomGeneratorRulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<Integer> testRandomGeneratorNextInt() {
    return ImmutableSet.of(
        (int) new Random().nextDouble(1),
        (int) (new SplittableRandom().nextDouble() * 2),
        (int) Math.round(new SecureRandom().nextDouble() * 3));
  }

  ImmutableSet<Long> testRandomGeneratorNextLong() {
    return ImmutableSet.of(
        (long) new Random().nextDouble((double) 1L),
        Math.round(new SplittableRandom().nextDouble((double) 2L)),
        (long) new SecureRandom().nextDouble(3L),
        Math.round(ThreadLocalRandom.current().nextDouble(4L)),
        (long) (new Random(0).nextDouble() * 5L),
        Math.round(new SplittableRandom(0).nextDouble() * 6L));
  }
}
