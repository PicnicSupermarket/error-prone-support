package tech.picnic.errorprone.refaster.test;

import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

abstract class MyAbstractTest {
  @TestFactory
  Stream<DynamicTest> dynamicTests() {
    MatchInWrongMethodRules.class.getDeclaredClasses();

    return Stream.of(
        DynamicTest.dynamicTest("A " + getClass(), () -> {}),
        DynamicTest.dynamicTest("A", () -> {}));
  }
}
