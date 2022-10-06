package tech.picnic.errorprone.refasterrules.input;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.AbstractMapAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJMapRulesTest implements RefasterRuleCollectionTestCase {
  AbstractMapAssert<?, ?, Integer, Integer>
      testAbstractMapAssertContainsExactlyInAnyOrderEntriesOf() {
    return assertThat(ImmutableMap.of(1, 2, 3, 4)).isEqualTo(ImmutableMap.of(1, 2, 3, 4));
  }

  AbstractMapAssert<?, ?, Integer, Integer> testAbstractMapAssertContainsExactlyEntriesOf() {
    return assertThat(ImmutableMap.of(1, 2))
        .containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(1, 2));
  }
}
