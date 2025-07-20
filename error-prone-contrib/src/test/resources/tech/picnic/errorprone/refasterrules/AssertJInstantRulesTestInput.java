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
    return assertThat(Instant.now().isAfter(Instant.EPOCH)).isTrue();
  }

  AbstractAssert<?, ?> testAbstractInstantAssertIsNotAfter() {
    return assertThat(Instant.EPOCH.isAfter(Instant.now())).isFalse();
  }

  AbstractAssert<?, ?> testAbstractInstantAssertIsBefore() {
    return assertThat(Instant.EPOCH.isBefore(Instant.now())).isTrue();
  }

  AbstractAssert<?, ?> testAbstractInstantAssertIsNotBefore() {
    return assertThat(Instant.now().isBefore(Instant.EPOCH)).isFalse();
  }

  AbstractAssert<?, ?> testAbstractInstantAssertIsBetween() {
    return assertThat(Instant.ofEpochSecond(100))
        .isAfterOrEqualTo(Instant.EPOCH)
        .isBeforeOrEqualTo(Instant.ofEpochSecond(200));
  }

  AbstractAssert<?, ?> testAbstractInstantAssertIsStrictlyBetween() {
    return assertThat(Instant.ofEpochSecond(100))
        .isAfter(Instant.EPOCH)
        .isBefore(Instant.ofEpochSecond(200));
  }
}
