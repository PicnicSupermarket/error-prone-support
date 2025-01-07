package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJIteratorRulesTest implements RefasterRuleCollectionTestCase {
  AbstractAssert<?, ?> testAssertThatHasNext() {
    return assertThat(ImmutableSet.of().iterator()).hasNext();
  }

  AbstractAssert<?, ?> testAssertThatIsExhausted() {
    return assertThat(ImmutableSet.of().iterator()).isExhausted();
  }
}
