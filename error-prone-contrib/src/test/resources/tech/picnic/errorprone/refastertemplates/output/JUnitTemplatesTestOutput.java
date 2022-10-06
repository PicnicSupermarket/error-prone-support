package tech.picnic.errorprone.refastertemplates.output;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.params.provider.Arguments;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class JUnitTemplatesTest implements RefasterTemplateTestCase {
  ImmutableSet<Arguments> testArgumentsEnumeration() {
    return ImmutableSet.of(
        arguments("foo"), arguments(1, "foo", 2, "bar"), arguments(new Object()));
  }
}
