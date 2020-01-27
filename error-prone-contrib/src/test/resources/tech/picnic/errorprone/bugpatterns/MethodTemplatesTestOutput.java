package tech.picnic.errorprone.bugpatterns;

import java.util.stream.Collectors;
import java.util.stream.Stream;

final class MethodTemplateTest implements RefasterTemplateTestCase {
  String testObjectReturn() {
    return "Hello!";
  }

  byte testByteReturn() {
    return Byte.MAX_VALUE;
  }

  char testCharacterReturn() {
    return Character.MAX_VALUE;
  }

  short testShortReturn() {
    return Short.MAX_VALUE;
  }

  int testIntegerReturn() {
    return Integer.MAX_VALUE;
  }

  long testLongReturn() {
    return 349827359L;
  }

  float testFloatReturn() {
    return 4324.347284F;
  }

  double testDoubleReturn() {
    return 3492.34284D;
  }

  boolean testBooleanReturn() {
    return true;
  }

  String testStuffAboveDoesntMatter() {
    System.out.println("Hi");
    return "Hello!";
  }

  String testStuffBelowDoesMatter() {
    String var = "Hello!";
    System.out.println("Hi");
    return var;
  }

  String testChainedReturn() {
    return Stream.of("I", "like", "error-prone", ":-)").collect(Collectors.joining(" ", "", "!"));
  }
}
