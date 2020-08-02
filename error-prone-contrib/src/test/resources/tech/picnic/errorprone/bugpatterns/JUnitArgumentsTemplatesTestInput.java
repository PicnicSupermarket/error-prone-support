package tech.picnic.errorprone.bugpatterns;

import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

final class JUnitTemplatesTest implements RefasterTemplateTestCase {
  Arguments testSingle() {
    return Arguments.of("foo");
  }

  Arguments testMultiple() {
    return Arguments.of(1, "foo", 2, "bar");
  }

  Stream<Arguments> testSingleInStream() {
    return Stream.of(Arguments.of("foo"));
  }

  Stream<Arguments> testMultipleInStream() {
    return Stream.of(Arguments.of(1, "foo", 2, "bar"));
  }
}
