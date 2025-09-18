package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractIntegerAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJCollectionRulesTest implements RefasterRuleCollectionTestCase {
  AbstractIntegerAssert<?> testAssertThatCollectionHasSize() {
    return assertThat(ImmutableSet.of(1, 2, 3).size()).isEqualTo(3);
  }

  AbstractIntegerAssert<?> testAssertThatCollectionHasSizeWithList() {
    return assertThat(ImmutableList.of("a", "b").size()).isEqualTo(2);
  }
}