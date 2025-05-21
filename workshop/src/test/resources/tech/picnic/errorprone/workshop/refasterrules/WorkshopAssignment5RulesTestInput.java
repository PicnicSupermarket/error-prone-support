package tech.picnic.errorprone.workshop.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class WorkshopAssignment5RulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<String> testOptionalOrElseIfItDoesntRequireComputation() {
    // The first expression should be rewritten to `Optional.of("foo").orElse("bar")` but the second
    // should stay `Optional.of("baz").orElseGet(() -> toString())`.
    return ImmutableSet.of(
        Optional.of("foo").orElseGet(() -> "bar"), Optional.of("baz").orElseGet(() -> toString()));
  }
}
