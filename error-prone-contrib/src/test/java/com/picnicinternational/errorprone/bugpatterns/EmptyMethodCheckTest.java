package com.picnicinternational.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class EmptyMethodCheckTest {
    private final CompilationTestHelper testHelper =
            CompilationTestHelper.newInstance(EmptyMethodCheck.class, getClass());

    @Test
    public void testNegative() {
        testHelper
                .addSourceLines(
                        "A.java",
                        "class A {",
                        "  Object m() {",
                        "    return null;",
                        "  }",
                        "",
                        "  void m2() {",
                        "    System.out.println(42);",
                        "  }",
                        "",
                        "  interface F {",
                        "    void fun();",
                        "  }",
                        "}")
                .doTest();
    }

    @Test
    public void testPositive() {
        testHelper
                .addSourceLines(
                        "A.java",
                        "class A {",
                        "  // BUG: Diagnostic contains:",
                        "  void m() {}",
                        "",
                        "  // BUG: Diagnostic contains:",
                        "  static void m2() {}",
                        "}")
                .doTest();
    }
}
