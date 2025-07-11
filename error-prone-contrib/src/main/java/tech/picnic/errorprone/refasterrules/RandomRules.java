package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Random;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link Random}. */
@OnlineDocumentation
final class RandomRules {
  private RandomRules() {}

  /** Prefer {@link Random#nextInt(int)} over casting a scaled random double to int. */
  static final class RandomNextInt {
    @BeforeTemplate
    int nextDouble(Random random, int bound) {
      return (int) (random.nextDouble() * bound);
    }

    @BeforeTemplate
    int nextDoubleWithRound(Random random, int bound) {
      return (int) Math.round(random.nextDouble() * bound);
    }

    @AfterTemplate
    int after(Random random, int bound) {
      return random.nextInt(bound);
    }
  }
}
