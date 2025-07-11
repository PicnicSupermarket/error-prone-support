package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.util.Random;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class RandomRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of();
  }

  int testRandomNextInt() {
    return (int) (new Random().nextDouble() * 10);
  }

  int testRandomNextIntWithRounding() {
    return (int) Math.round(new Random().nextDouble() * 10);
  }
}