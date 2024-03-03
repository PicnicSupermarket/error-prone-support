package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class TypeMemberOrderUnhandledKindsTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addSourceLines(
            "A.java",
            "@interface A {",
            "  class InnerClass_1 {}",
            "",
            "  int foo();",
            "",
            "  int bar = 0;",
            "",
            "  class InnerClass_2 {}",
            "}")
        .addSourceLines(
            "B.java",
            "record B(int foo, int bar) {",
            "  void baz() {}",
            "",
            "  static final int QUX = 1;",
            "",
            "  void quux() {}",
            "}")
        .doTest();
  }
}
