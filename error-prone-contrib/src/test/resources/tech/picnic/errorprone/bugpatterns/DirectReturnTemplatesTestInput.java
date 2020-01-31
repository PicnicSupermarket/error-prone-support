package tech.picnic.errorprone.bugpatterns;

import static java.util.stream.Collectors.joining;

import java.util.stream.Stream;

final class DirectReturnTemplatesTest implements RefasterTemplateTestCase {
  boolean testDirectlyReturnBooleanVariable() {
    boolean var = true;
    return var;
  }

  byte testDirectlyReturnByteVariable() {
    byte var = Byte.MAX_VALUE;
    return var;
  }

  char testDirectlyReturnCharVariable() {
    char var = Character.MAX_VALUE;
    return var;
  }

  short testDirectlyReturnShortVariable() {
    short var = Short.MAX_VALUE;
    return var;
  }

  int testDirectlyReturnIntVariable() {
    int var = Integer.MAX_VALUE;
    return var;
  }

  long testDirectlyReturnLongVariable() {
    long var = Long.MAX_VALUE;
    return var;
  }

  float testDirectlyReturnFloatVariable() {
    float var = Float.MAX_VALUE;
    return var;
  }

  double testDirectlyReturnDoubleVariable() {
    double var = Double.MAX_VALUE;
    return var;
  }

  String testDirectlyReturnObjectVariable() {
    String var = "foo";
    return var;
  }

  String testDirectlyReturnObjectVariableWithPrecedingStatement() {
    String unrelated = "foo";
    String var = Stream.of("bar", "baz").collect(joining(" "));
    return var;
  }

  String testDirectlyReturnObjectVariableWithInterveningStatement() {
    String var = "foo";
    String unrelated = "bar";
    return var;
  }
}
