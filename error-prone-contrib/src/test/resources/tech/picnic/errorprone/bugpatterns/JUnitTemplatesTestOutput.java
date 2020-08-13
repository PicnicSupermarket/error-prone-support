package tech.picnic.errorprone.bugpatterns;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.params.provider.Arguments;

final class JUnitTemplatesTest implements RefasterTemplateTestCase {
  ImmutableSet<Arguments> testArgumentsEnumeration() {
    return ImmutableSet.of(
        arguments("foo"), arguments(1, "foo", 2, "bar"), arguments(new Object()));
  }
}
