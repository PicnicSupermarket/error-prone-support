package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class TypeMemberOrderEnumTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains:",
            "enum A {",
            "  FOO;",
            "",
            "  class InnerClass {}",
            "",
            "  interface InnerInterface {}",
            "",
            "  enum InnerEnum {}",
            "",
            "  void qux() {}",
            "",
            "  A() {}",
            "",
            "  {",
            "    System.out.println(\"bar\");",
            "  }",
            "",
            "  static {",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "  final int baz = 2;",
            "  static final int BAR = 1;",
            "}")
        .addSourceLines(
            "B.java",
            "enum B {",
            "  FOO;",
            "  static final int BAR = 1;",
            "  final int baz = 2;",
            "",
            "  static {",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "  {",
            "    System.out.println(\"bar\");",
            "  }",
            "",
            "  B() {}",
            "",
            "  void qux() {}",
            "",
            "  class InnerClass {}",
            "",
            "  interface InnerInterface {}",
            "",
            "  enum InnerEnum {}",
            "}")
        .addSourceLines(
            "C.java",
            "enum C {",
            "  FOO;",
            "",
            "  @SuppressWarnings({\"foo\", \"all\", \"bar\"})",
            "  void bar() {}",
            "",
            "  C() {}",
            "}")
        .addSourceLines(
            "D.java",
            "enum D {",
            "  FOO;",
            "",
            "  @SuppressWarnings({\"TypeMemberOrder\"})",
            "  void bar() {}",
            "",
            "  D() {}",
            "}")
        .addSourceLines("E.java", "enum E {}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "enum A {",
            "  FOO;",
            "",
            "  class InnerClass {}",
            "",
            "  interface InnerInterface {}",
            "",
            "  enum InnerEnum {}",
            "",
            "  void qux() {}",
            "",
            "  A() {}",
            "",
            "  {",
            "    System.out.println(\"bar\");",
            "  }",
            "",
            "  static {",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "  final int baz = 2;",
            "  static final int BAR = 1;",
            "}")
        .addOutputLines(
            "A.java",
            "enum A {",
            "  FOO;",
            "  static final int BAR = 1;",
            "",
            "  final int baz = 2;",
            "",
            "  static {",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "  {",
            "    System.out.println(\"bar\");",
            "  }",
            "",
            "  A() {}",
            "",
            "  void qux() {}",
            "",
            "  class InnerClass {}",
            "",
            "  interface InnerInterface {}",
            "",
            "  enum InnerEnum {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementUnmovableMembers() {
    BugCheckerRefactoringTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "enum A {",
            "  FOO;",
            "",
            "  @SuppressWarnings(\"TypeMemberOrder\")",
            "  class InnerClass {}",
            "",
            "  interface InnerInterface {}",
            "",
            "  enum InnerEnum {}",
            "",
            "  void qux() {}",
            "",
            "  A() {}",
            "",
            "  {",
            "    System.out.println(\"bar\");",
            "  }",
            "",
            "  static {",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "  @SuppressWarnings(\"TypeMemberOrder\")",
            "  final int baz = 2;",
            "",
            "  static final int BAR = 1;",
            "}")
        .addOutputLines(
            "A.java",
            "enum A {",
            "  FOO;",
            "",
            "  @SuppressWarnings(\"TypeMemberOrder\")",
            "  class InnerClass {}",
            "",
            "  static final int BAR = 1;",
            "",
            "  static {",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "  {",
            "    System.out.println(\"bar\");",
            "  }",
            "",
            "  A() {}",
            "",
            "  void qux() {}",
            "",
            "  interface InnerInterface {}",
            "",
            "  @SuppressWarnings(\"TypeMemberOrder\")",
            "  final int baz = 2;",
            "",
            "  enum InnerEnum {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementHandlesGeneratedDefaultConstructor() {
    BugCheckerRefactoringTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "enum A {",
            "  FOO;",
            "",
            "  class InnerClass {}",
            "",
            "  interface InnerInterface {}",
            "",
            "  enum InnerEnum {}",
            "",
            "  void qux() {}",
            "",
            "  {",
            "    System.out.println(\"bar\");",
            "  }",
            "",
            "  static {",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "  final int baz = 2;",
            "  static final int BAR = 1;",
            "}")
        .addOutputLines(
            "A.java",
            "enum A {",
            "  FOO;",
            "  static final int BAR = 1;",
            "",
            "  final int baz = 2;",
            "",
            "  static {",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "  {",
            "    System.out.println(\"bar\");",
            "  }",
            "",
            "  void qux() {}",
            "",
            "  class InnerClass {}",
            "",
            "  interface InnerInterface {}",
            "",
            "  enum InnerEnum {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementDanglingComments() {
    BugCheckerRefactoringTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "enum A {",
            "  /** FOO's JavaDoc */",
            "  FOO",
            "/* Dangling comment trailing enumerations. */ ;",
            "",
            "  /* `quz` method's dangling comment */",
            "  ;",
            "",
            "  /* `quz` method's comment */",
            "  void qux() {}",
            "",
            "  // `baz` method's comment",
            "  final int baz = 2;",
            "",
            "  static final int BAR = 1;",
            "  // trailing comment",
            "}")
        .addOutputLines(
            "A.java",
            "enum A {",
            "  /** FOO's JavaDoc */",
            "  FOO",
            "/* Dangling comment trailing enumerations. */ ;",
            "",
            "  static final int BAR = 1;",
            "",
            "  // `baz` method's comment",
            "  final int baz = 2;",
            "",
            "  /* `quz` method's dangling comment */",
            "  ;",
            "",
            "  /* `quz` method's comment */",
            "  void qux() {}",
            "  // trailing comment",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
