package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJInstantRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Instant.class);
  }

  AbstractAssert<?, ?> testAbstractInstantAssertIsAfter() {
    return assertThat(Instant.now()).isAfter(Instant.EPOCH);
  }

  AbstractAssert<?, ?> testAbstractInstantAssertIsNotAfter() {
    return assertThat(Instant.EPOCH).isBeforeOrEqualTo(Instant.now());
  }

  AbstractAssert<?, ?> testAbstractInstantAssertIsBefore() {
    return assertThat(Instant.EPOCH).isBefore(Instant.now());
  }

  AbstractAssert<?, ?> testAbstractInstantAssertIsNotBefore() {
    return assertThat(Instant.now()).isAfterOrEqualTo(Instant.EPOCH);
  }

  AbstractAssert<?, ?> testAbstractInstantAssertIsBeforeOrEqualTo() {
    return assertThat(Instant.EPOCH).isBeforeOrEqualTo(Instant.now());
  }

  AbstractAssert<?, ?> testAbstractInstantAssertIsAfterOrEqualTo() {
    return assertThat(Instant.now()).isAfterOrEqualTo(Instant.EPOCH);
  }

  AbstractAssert<?, ?> testAbstractInstantAssertIsBetween() {
    return assertThat(Instant.ofEpochSecond(100))
        .isBetween(Instant.EPOCH, Instant.ofEpochSecond(200));
  }

  AbstractAssert<?, ?> testAbstractInstantAssertIsStrictlyBetween() {
    return assertThat(Instant.ofEpochSecond(100))
        .isStrictlyBetween(Instant.EPOCH, Instant.ofEpochSecond(200));
  }
}