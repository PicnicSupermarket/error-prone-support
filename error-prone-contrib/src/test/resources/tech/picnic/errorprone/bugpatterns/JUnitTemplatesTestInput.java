package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.params.provider.Arguments;

final class JUnitTemplatesTest implements RefasterTemplateTestCase {
  ImmutableSet<Arguments> testArguments() {
    return ImmutableSet.of(
        Arguments.of("foo"), Arguments.of(1, "foo", 2, "bar"), Arguments.of(new Object()));
  }
}
