package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.params.provider.Arguments;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class JUnitTemplatesTest implements RefasterTemplateTestCase {
  ImmutableSet<Arguments> testArgumentsEnumeration() {
    return ImmutableSet.of(
        Arguments.of("foo"), Arguments.of(1, "foo", 2, "bar"), Arguments.of(new Object()));
  }
}
