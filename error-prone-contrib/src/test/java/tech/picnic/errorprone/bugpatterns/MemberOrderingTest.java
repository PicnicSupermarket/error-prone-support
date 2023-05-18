package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.containsPattern;
import static com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers.SECOND;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class MemberOrderingTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(MemberOrdering.class, getClass())
        .expectErrorMessage(
            "MemberOrdering",
            and(
                containsPattern("SuppressWarnings"),
                containsPattern(
                    "Members, constructors and methods should follow standard ordering.")))
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic matches: MemberOrdering",
            "class A {",
            "  char a = 'a';",
            "  private static String FOO = \"foo\";",
            "  static int ONE = 1;",
            "",
            "  void m2() {}",
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
        .addSourceLines(
            "B.java",
            "class B {",
            "  private static String FOO = \"foo\";",
            "  static int ONE = 1;",
            "  private static String BAR = \"bar\";",
            "",
            "  static int TWO = 2;",
            "",
            "  char a = 'a';",
            "",
            "  char b = 'b';",
            "",
            "  public B() {}",
            "",
            "  void m1() {",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "  void m2() {}",
            "",
            "  class Inner {}",
            "",
            "  static class StaticInner {}",
            "}")
        .doTest();
  }

  @Test
  void replacementFirstSuggestedFix() {
    BugCheckerRefactoringTestHelper.newInstance(MemberOrdering.class, getClass())
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
            "@SuppressWarnings(\"MemberOrdering\")",
            "class A {",
            "  private static final int X = 1;",
            "  char a = 'a';",
            "  private static String FOO = \"foo\";",
            "  static int ONE = 1;",
            "",
            "  void m2() {}",
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
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementSecondSuggestedFix() {
    BugCheckerRefactoringTestHelper.newInstance(MemberOrdering.class, getClass())
        .setFixChooser(SECOND)
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
            "  private static String BAR = \"bar\";",
            "",
            "  static int TWO = 2;",
            "",
            "  char a = 'a';",
            "",
            "  char b = 'b';",
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
  }

  @Test
  void replacementSecondSuggestedFixConsidersDefaultConstructor() {
    BugCheckerRefactoringTestHelper.newInstance(MemberOrdering.class, getClass())
        .setFixChooser(SECOND)
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
            "  private static final String foo = \"foo\";",
            "",
            "  static int one = 1;",
            "",
            "  char c = 'c';",
            "",
            "  void m1() {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementSecondSuggestedFixConsidersComments() {
    BugCheckerRefactoringTestHelper.newInstance(MemberOrdering.class, getClass())
        .setFixChooser(SECOND)
        .addInputLines(
            "A.java",
            "class A {",
            "  // `m1()` comment.",
            "  void m1() {",
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
            "  // foo",
            "  /** Instantiates a new {@link A} instance. */",
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

  @SuppressWarnings("ErrorProneTestHelperSourceFormat")
  @Test
  void replacementSecondSuggestedFixDoesNotModifyWhitespace() {
    BugCheckerRefactoringTestHelper.newInstance(MemberOrdering.class, getClass())
        .setFixChooser(SECOND)
        .addInputLines(
            "A.java",
            "",
            "",
            "class A {",
            "",
            "",
            "  // `m1()` comment.",
            "  void m1() {",
            "    // Print line 'foo' to stdout.",
            "    System.out.println(\"foo\");",
            "  }",
            "  public  A  ()  {  }",
            "",
            "",
            "}")
        .addOutputLines(
            "A.java",
            "",
            "",
            "class A {",
            "",
            "",
            "",
            "  public  A  ()  {  }",
            "  // `m1()` comment.",
            "  void m1() {",
            "    // Print line 'foo' to stdout.",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "",
            "}")
        .doTest();
  }

  // todo: Actually verify that whitespace is preserved.
  @SuppressWarnings("ErrorProneTestHelperSourceFormat")
  @Test
  void xxx() {
    BugCheckerRefactoringTestHelper.newInstance(MemberOrdering.class, getClass())
        .setFixChooser(SECOND)
        .addInputLines(
            "A.java",
            "",
            "",
            "class A {",
            "",
            "",
            "  // `m1()` comment.",
            "  void m1() {",
            "    // Print line 'foo' to stdout.",
            "    System.out.println(\"foo\");",
            "  }",
            "  public  A  ()  {  }",
            "",
            "",
            "}")
        .addOutputLines(
            "A.java",
            "",
            "",
            "class A {",
            "",
            "  ",
            "     ",
            "  \t  \t",
            "     ",
            "  ",
            "",
            "  public  A                    ()  {  }",
            "  // `m1()` comment.",
            "  void m1",
            "         ()",
            "  {",
            "    // Print line 'foo' to stdout.",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  // todo: Test if second replacement considers annotations.
}
