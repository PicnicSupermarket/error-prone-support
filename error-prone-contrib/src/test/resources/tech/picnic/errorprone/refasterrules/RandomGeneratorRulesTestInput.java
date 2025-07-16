package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.security.SecureRandom;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class RandomGeneratorRulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<Double> testRandomGeneratorNextDouble() {
    return ImmutableSet.of(
        new Random().nextDouble() * 1,
        new SplittableRandom().nextDouble() * 2L,
        new SecureRandom().nextDouble() * 3.0);
  }

  ImmutableSet<Integer> testRandomGeneratorNextInt() {
    return ImmutableSet.of(
        (int) new Random().nextDouble(1), (int) Math.round(new SplittableRandom().nextDouble(2)));
  }

  ImmutableSet<Long> testRandomGeneratorNextLong() {
    return ImmutableSet.of(
        (long) new Random().nextDouble((double) 1L),
        Math.round(new SplittableRandom().nextDouble((double) 2L)),
        (long) new SecureRandom().nextDouble(3L),
        Math.round(ThreadLocalRandom.current().nextDouble(4L)));
  }
}
