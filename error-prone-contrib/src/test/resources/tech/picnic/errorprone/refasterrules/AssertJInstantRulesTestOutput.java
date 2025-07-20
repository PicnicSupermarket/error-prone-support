package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractInstantAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJInstantRulesTest implements RefasterRuleCollectionTestCase {
  AbstractAssert<?, ?> testAssertThatIsAfter() {
    return assertThat(Instant.MIN).isAfter(Instant.MAX);
  }

  AbstractAssert<?, ?> testAssertThatIsBeforeOrEqualTo() {
    return assertThat(Instant.MIN).isBeforeOrEqualTo(Instant.MAX);
  }

  AbstractAssert<?, ?> testAssertThatIsBefore() {
    return assertThat(Instant.MIN).isBefore(Instant.MAX);
  }

  AbstractAssert<?, ?> testAssertThatIsAfterOrEqualTo() {
    return assertThat(Instant.MIN).isAfterOrEqualTo(Instant.MAX);
  }

  ImmutableSet<AbstractInstantAssert<?>> testAssertThatIsBetween() {
    return ImmutableSet.of(
        assertThat(Instant.EPOCH).isBetween(Instant.MIN, Instant.MAX),
        assertThat(Instant.ofEpochMilli(0))
            .isBetween(Instant.ofEpochMilli(2), Instant.ofEpochMilli(1)));
  }

  ImmutableSet<AbstractInstantAssert<?>> testAssertThatIsStrictlyBetween() {
    return ImmutableSet.of(
        assertThat(Instant.EPOCH).isStrictlyBetween(Instant.MIN, Instant.MAX),
        assertThat(Instant.ofEpochMilli(0))
            .isStrictlyBetween(Instant.ofEpochMilli(2), Instant.ofEpochMilli(1)));
  }
}
