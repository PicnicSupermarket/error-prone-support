package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractCollectionAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJCollectionRulesTest implements RefasterRuleCollectionTestCase {
  AbstractCollectionAssert<?, ?, ?, ?> testAssertThatCollectionHasSize() {
    return assertThat(ImmutableSet.of(1, 2, 3)).hasSize(3);
  }

  AbstractCollectionAssert<?, ?, ?, ?> testAssertThatCollectionHasSizeWithList() {
    return assertThat(ImmutableList.of("a", "b")).hasSize(2);
  }
}