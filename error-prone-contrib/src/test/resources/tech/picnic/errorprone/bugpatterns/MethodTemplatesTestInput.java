package tech.picnic.errorprone.bugpatterns;

import java.util.stream.Collectors;
import java.util.stream.Stream;

final class MethodTemplateTest implements RefasterTemplateTestCase {
  String testObjectReturn() {
    String var = "Hello!";
    return var;
  }

  byte testByteReturn() {
    byte var = Byte.MAX_VALUE;
    return var;
  }

  char testCharacterReturn() {
    char var = Character.MAX_VALUE;
    return var;
  }

  short testShortReturn() {
    short var = Short.MAX_VALUE;
    return var;
  }

  int testIntegerReturn() {
    int var = Integer.MAX_VALUE;
    return var;
  }

  long testLongReturn() {
    long var = 349827359L;
    return var;
  }

  float testFloatReturn() {
    float var = 4324.347284F;
    return var;
  }

  double testDoubleReturn() {
    double var = 3492.34284D;
    return var;
  }

  boolean testBooleanReturn() {
    boolean var = true;
    return var;
  }

  String testStuffAboveDoesntMatter() {
    System.out.println("Hi");
    String var = "Hello!";
    return var;
  }

  String testStuffBelowDoesMatter() {
    String var = "Hello!";
    System.out.println("Hi");
    return var;
  }

  String testChainedReturn() {
    String var =
        Stream.of("I", "like", "error-prone", ":-)").collect(Collectors.joining(" ", "", "!"));
    return var;
  }
}
