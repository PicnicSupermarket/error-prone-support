package tech.picnic.errorprone.bugpatterns;

import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class MethodMatcherFactoryTest {
  /** A {@link BugChecker} which flags method invocations matched by {@link #TEST_MATCHER}. */
  @BugPattern(
    name = "MatchedMethodsFlagger",
    severity = SeverityLevel.SUGGESTION,
    summary = "Flags methods matched by the test matcher."
  )
  public static final class MatchedMethodsFlagger extends BugChecker
      implements MethodInvocationTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
      if (TEST_MATCHER.matches(tree, state)) {
        return buildDescription(tree).build();
      }

      return Description.NO_MATCH;
    }
  }

  private static final Matcher<ExpressionTree> TEST_MATCHER =
      new MethodMatcherFactory()
          .create(
              ImmutableList.of(
                  "com.example.A#m1()",
                  "com.example.A#m2(java.lang.String)",
                  "com.example.sub.B#m3(int,int)"));

  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(MatchedMethodsFlagger.class, getClass());

  @Test
  public void testCreateWithMalformedSignatures() {
    MethodMatcherFactory factory = new MethodMatcherFactory();
    assertThrows(IllegalArgumentException.class, () -> factory.create(ImmutableList.of("foo.bar")));
    assertThrows(
        IllegalArgumentException.class, () -> factory.create(ImmutableList.of("foo.bar#baz")));
    assertThrows(
        IllegalArgumentException.class,
        () -> factory.create(ImmutableList.of("a", "foo.bar#baz()")));
    assertThrows(
        IllegalArgumentException.class,
        () -> factory.create(ImmutableList.of("foo.bar#baz()", "a")));
  }

  @Test
  public void testMatcher() {
    compilationTestHelper
        .addSourceLines(
            "com/example/A.java",
            "package com.example;",
            "",
            "public class A {",
            "  public void m1() {}",
            "  public void m1(String s) {}",
            "  public void m1(int i, int j) {}",
            "  public void m2() {}",
            "  public void m2(String s) {}",
            "  public void m2(int i, int j) {}",
            "  public void m3() {}",
            "  public void m3(String s) {}",
            "  public void m3(int i, int j) {}",
            "}")
        .addSourceLines(
            "com/example/B.java",
            "package com.example;",
            "",
            "public class B {",
            "  public void m1() {}",
            "  public void m1(String s) {}",
            "  public void m1(int i, int j) {}",
            "  public void m2() {}",
            "  public void m2(String s) {}",
            "  public void m2(int i, int j) {}",
            "  public void m3() {}",
            "  public void m3(String s) {}",
            "  public void m3(int i, int j) {}",
            "}")
        .addSourceLines(
            "com/example/sub/A.java",
            "package com.example.sub;",
            "",
            "public class A {",
            "  public static void m1() {}",
            "  public static void m1(String s) {}",
            "  public static void m1(int i, int j) {}",
            "  public static void m2() {}",
            "  public static void m2(String s) {}",
            "  public static void m2(int i, int j) {}",
            "  public static void m3() {}",
            "  public static void m3(String s) {}",
            "  public static void m3(int i, int j) {}",
            "}")
        .addSourceLines(
            "com/example/sub/B.java",
            "package com.example.sub;",
            "",
            "public class B {",
            "  public static void m1() {}",
            "  public static void m1(String s) {}",
            "  public static void m1(int i, int j) {}",
            "  public static void m2() {}",
            "  public static void m2(String s) {}",
            "  public static void m2(int i, int j) {}",
            "  public static void m3() {}",
            "  public static void m3(String s) {}",
            "  public static void m3(int i, int j) {}",
            "}")
        .addSourceLines(
            "External.java",
            "import com.example.A;",
            "import com.example.sub.B;",
            "",
            "public class External {",
            "  void invocations() {",
            "    // BUG: Diagnostic contains:",
            "    new A().m1();",
            "    new A().m1(\"\");",
            "    new A().m1(0, 0);",
            "    new A().m2();",
            "    // BUG: Diagnostic contains:",
            "    new A().m2(\"\");",
            "    new A().m2(0, 0);",
            "    new A().m3();",
            "    new A().m3(\"\");",
            "    new A().m3(0, 0);",
            "    B.m1();",
            "    B.m1(\"\");",
            "    B.m1(0, 0);",
            "    B.m2();",
            "    B.m2(\"\");",
            "    B.m2(0, 0);",
            "    B.m3();",
            "    B.m3(\"\");",
            "    // BUG: Diagnostic contains:",
            "    B.m3(0, 0);",
            "  }",
            "}")
        .addSourceLines(
            "ExternalWithDifferentPackages.java",
            "import com.example.B;",
            "import com.example.sub.A;",
            "",
            "public class ExternalWithDifferentPackages {",
            "  void invocations() {",
            "    A.m1();",
            "    A.m1(\"\");",
            "    A.m1(0, 0);",
            "    A.m2();",
            "    A.m2(\"\");",
            "    A.m2(0, 0);",
            "    A.m3();",
            "    A.m3(\"\");",
            "    A.m3(0, 0);",
            "    new B().m1();",
            "    new B().m1(\"\");",
            "    new B().m1(0, 0);",
            "    new B().m2();",
            "    new B().m2(\"\");",
            "    new B().m2(0, 0);",
            "    new B().m3();",
            "    new B().m3(\"\");",
            "    new B().m3(0, 0);",
            "  }",
            "}")
        .doTest();
  }
}
