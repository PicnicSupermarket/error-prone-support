package tech.picnic.errorprone.bugpatterns;

import static java.util.stream.Collectors.joining;

import java.util.stream.Stream;

final class DirectReturnTemplatesTest implements RefasterTemplateTestCase {
  boolean testDirectlyReturnBooleanVariable() {
    return true;
  }

  byte testDirectlyReturnByteVariable() {
    return Byte.MAX_VALUE;
  }

  char testDirectlyReturnCharVariable() {
    return Character.MAX_VALUE;
  }

  short testDirectlyReturnShortVariable() {
    return Short.MAX_VALUE;
  }

  int testDirectlyReturnIntVariable() {
    return Integer.MAX_VALUE;
  }

  long testDirectlyReturnLongVariable() {
    return Long.MAX_VALUE;
  }

  float testDirectlyReturnFloatVariable() {
    return Float.MAX_VALUE;
  }

  double testDirectlyReturnDoubleVariable() {
    return Double.MAX_VALUE;
  }

  String testDirectlyReturnObjectVariable() {
    return "foo";
  }

  String testDirectlyReturnObjectVariableWithPrecedingStatement() {
    String unrelated = "foo";
    return Stream.of("bar", "baz").collect(joining(" "));
  }

  String testDirectlyReturnObjectVariableWithInterveningStatement() {
    String var = "foo";
    String unrelated = "bar";
    return var;
  }
}
