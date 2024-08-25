package tech.picnic.errorprone.refasterrules.output;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.params.provider.Arguments;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class JUnitRulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<Arguments> testArgumentsEnumeration() {
    return ImmutableSet.of(
        arguments("foo"), arguments(1, "foo", 2, "bar"), arguments(new Object()));
  }
}
