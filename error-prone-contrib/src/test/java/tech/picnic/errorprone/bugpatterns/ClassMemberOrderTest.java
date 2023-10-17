package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class ClassMemberOrderTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(ClassMemberOrder.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  class Empty {}",
            "",
            "  class SingleField {",
            "    private int field;",
            "  }",
            "",
            "  class FieldAndMethod {",
            "    private int field;",
            "",
            "    void foo() {}",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  class MethodAndField {",
            "    void foo() {}",
            "",
            "    private int field;",
            "  }",
            "",
            "  class AllSorted {",
            "    private static String FOO = \"foo\";",
            "    static String BAR = \"bar\";",
            "    public static final int ONE = 1;",
            "    protected static final int TWO = 2;",
            "",
            "    private char a = 'a';",
            "    public char b = 'b';",
            "",
            "    static {",
            "      FOO = \"foo2\";",
            "    }",
            "",
            "    static {",
            "      BAR = \"bar2\";",
            "    }",
            "",
            "    {",
            "      a = 'c';",
            "    }",
            "",
            "    {",
            "      b = 'd';",
            "    }",
            "",
            "    public AllSorted() {}",
            "",
            "    AllSorted(int param) {}",
            "",
            "    int m1() {",
            "      return 42;",
            "    }",
            "",
            "    public void m2() {}",
            "",
            "    class Nested {}",
            "",
            "    static class StaticNested {}",
            "  }",
            "}")
        .doTest();
  }

  // XXX: Also test with an interface!
  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(ClassMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "  private static final int X = 1;",
            "  char a = 'a';",
            "  private static String FOO = \"foo\";",
            "  static int ONE = 1;",
            "",
            "  void m2() {}",
            "",
            "  {",
            "  }",
            "",
            "  public A() {}",
            "",
            "  private static String BAR = \"bar\";",
            "  char b = 'b';",
            "",
            "  void m1() {",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "  static int TWO = 2;",
            "",
            "  class Inner {}",
            "",
            "  static class StaticInner {}",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "  private static final int X = 1;",
            "  private static String FOO = \"foo\";",
            "  static int ONE = 1;",
            "",
            "  private static String BAR = \"bar\";",
            "",
            "  static int TWO = 2;",
            "  char a = 'a';",
            "  char b = 'b';",
            "",
            "  {",
            "  }",
            "",
            "  public A() {}",
            "",
            "  void m2() {}",
            "",
            "  void m1() {",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "  class Inner {}",
            "",
            "  static class StaticInner {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);

    // XXX: Merge.
    BugCheckerRefactoringTestHelper.newInstance(ClassMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "  void m1() {}",
            "",
            "  char c = 'c';",
            "",
            "  private static final String foo = \"foo\";",
            "",
            "  static int one = 1;",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "",
            "  private static final String foo = \"foo\";",
            "",
            "  static int one = 1;",
            "",
            "  char c = 'c';",
            "",
            "  void m1() {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);

    // XXX: Merge.
    BugCheckerRefactoringTestHelper.newInstance(ClassMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "  // detached comment from method",
            "  ;",
            "  void method1() {}",
            "",
            "  // first comment prior to method",
            "  // second comment prior to method",
            "  void method2() {",
            "    // Print line 'foo' to stdout.",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "  // foo",
            "  /** Instantiates a new {@link A} instance. */",
            "  public A() {}",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "",
            "  // foo",
            "  /** Instantiates a new {@link A} instance. */",
            "  public A() {}",
            "  // detached comment from method",
            "  ;",
            "",
            "  void method1() {}",
            "",
            "  // first comment prior to method",
            "  // second comment prior to method",
            "  void method2() {",
            "    // Print line 'foo' to stdout.",
            "    System.out.println(\"foo\");",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);

    // XXX: Merge.
    BugCheckerRefactoringTestHelper.newInstance(ClassMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "  @SuppressWarnings(\"foo\")",
            "  void m1() {}",
            "",
            "  @SuppressWarnings(\"bar\")",
            "  A() {}",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "",
            "  @SuppressWarnings(\"bar\")",
            "  A() {}",
            "",
            "  @SuppressWarnings(\"foo\")",
            "  void m1() {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);

    // XXX: Merge.
    BugCheckerRefactoringTestHelper.newInstance(ClassMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "",
            "  // `m1()` comment.",
            "  void m1() {",
            "    // Print line 'foo' to stdout.",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "  public A() {}",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "",
            "  public A() {}",
            "",
            "  // `m1()` comment.",
            "  void m1() {",
            "    // Print line 'foo' to stdout.",
            "    System.out.println(\"foo\");",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);

    // XXX: Merge.
    BugCheckerRefactoringTestHelper.newInstance(ClassMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "",
            "  // `m1()` comment.",
            "  void m1() {",
            "    // Print line 'foo' to stdout.",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "  public A() {}",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "",
            "  public A() {}",
            "",
            "  // `m1()` comment.",
            "  void m1() {",
            "    // Print line 'foo' to stdout.",
            "    System.out.println(\"foo\");",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
