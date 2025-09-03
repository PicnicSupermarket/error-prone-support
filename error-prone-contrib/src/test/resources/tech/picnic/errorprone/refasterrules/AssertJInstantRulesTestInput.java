package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractInstantAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJInstantRulesTest implements RefasterRuleCollectionTestCase {
  AbstractAssert<?, ?> testAssertThatIsAfter() {
    return assertThat(Instant.MIN.isAfter(Instant.MAX)).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsBeforeOrEqualTo() {
    return assertThat(Instant.MIN.isAfter(Instant.MAX)).isFalse();
  }

  AbstractAssert<?, ?> testAssertThatIsBefore() {
    return assertThat(Instant.MIN.isBefore(Instant.MAX)).isTrue();
  }

  AbstractAssert<?, ?> testAssertThatIsAfterOrEqualTo() {
    return assertThat(Instant.MIN.isBefore(Instant.MAX)).isFalse();
  }

  ImmutableSet<AbstractInstantAssert<?>> testAssertThatIsBetween() {
    return ImmutableSet.of(
        assertThat(Instant.EPOCH).isAfterOrEqualTo(Instant.MIN).isBeforeOrEqualTo(Instant.MAX),
        assertThat(Instant.ofEpochMilli(0))
            .isBeforeOrEqualTo(Instant.ofEpochMilli(1))
            .isAfterOrEqualTo(Instant.ofEpochMilli(2)));
  }

  ImmutableSet<AbstractInstantAssert<?>> testAssertThatIsStrictlyBetween() {
    return ImmutableSet.of(
        assertThat(Instant.EPOCH).isAfter(Instant.MIN).isBefore(Instant.MAX),
        assertThat(Instant.ofEpochMilli(0))
            .isBefore(Instant.ofEpochMilli(1))
            .isAfter(Instant.ofEpochMilli(2)));
  }
}
