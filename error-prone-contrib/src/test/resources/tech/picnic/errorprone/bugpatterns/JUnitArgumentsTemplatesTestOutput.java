package tech.picnic.errorprone.bugpatterns;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

final class JUnitTemplatesTest implements RefasterTemplateTestCase {
  Arguments testSingle() {
    return arguments("foo");
  }

  Arguments testMultiple() {
    return arguments(1, "foo", 2, "bar");
  }

  Stream<Arguments> testSingleInStream() {
    return Stream.of(arguments("foo"));
  }

  Stream<Arguments> testMultipleInStream() {
    return Stream.of(arguments(1, "foo", 2, "bar"));
  }
}
