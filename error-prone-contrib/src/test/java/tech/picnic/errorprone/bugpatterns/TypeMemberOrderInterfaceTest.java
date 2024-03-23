package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class TypeMemberOrderInterfaceTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .expectErrorMessage(
            "TypeMemberOrder",
            message -> message.contains("Type members should be ordered in a canonical order"))
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic matches: TypeMemberOrder",
            "interface A {",
            "  static class InnerClass {}",
            "",
            "  static interface InnerInterface {}",
            "",
            "  static enum InnerEnum {}",
            "",
            "  void bar();",
            "",
            "  static final int foo = 1;",
            "}")
        .addSourceLines(
            "B.java",
            "interface B {",
            "  static final int foo = 1;",
            "",
            "  void bar();",
            "",
            "  static class InnerClass {}",
            "",
            "  static interface InnerInterface {}",
            "",
            "  static enum InnerEnum {}",
            "}")
        .addSourceLines(
            "C.java",
            "interface C {",
            "  @SuppressWarnings({\"foo\", \"all\", \"bar\"})",
            "  void bar();",
            "",
            "  static final int foo = 1;",
            "}")
        .addSourceLines(
            "D.java",
            "interface D {",
            "  @SuppressWarnings(\"TypeMemberOrder\")",
            "  void bar();",
            "",
            "  static final int foo = 1;",
            "}")
        .addSourceLines("E.java", "interface E {}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "interface A {",
            "  static class InnerClass {}",
            "",
            "  static interface InnerInterface {}",
            "",
            "  static enum InnerEnum {}",
            "",
            "  void bar();",
            "",
            "  static final int foo = 1;",
            "}")
        .addOutputLines(
            "A.java",
            "interface A {",
            "",
            "  static final int foo = 1;",
            "",
            "  void bar();",
            "",
            "  static class InnerClass {}",
            "",
            "  static interface InnerInterface {}",
            "",
            "  static enum InnerEnum {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementDefaultMethods() {
    BugCheckerRefactoringTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "interface A {",
            "  class InnerClass {}",
            "",
            "  void foo();",
            "",
            "  default void bar() {}",
            "",
            "  void baz();",
            "",
            "  static final int QUX = 1;",
            "}")
        .addOutputLines(
            "A.java",
            "interface A {",
            "",
            "  static final int QUX = 1;",
            "",
            "  void foo();",
            "",
            "  default void bar() {}",
            "",
            "  void baz();",
            "",
            "  class InnerClass {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementUnmovableMembers() {
    BugCheckerRefactoringTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "interface A {",
            "  @SuppressWarnings(\"TypeMemberOrder\")",
            "  static class InnerClass {}",
            "",
            "  static interface InnerInterface {}",
            "",
            "  static enum InnerEnum {}",
            "",
            "  void bar();",
            "",
            "  @SuppressWarnings(\"TypeMemberOrder\")",
            "  static final int baz = 1;",
            "",
            "  static final int foo = 1;",
            "}")
        .addOutputLines(
            "A.java",
            "interface A {",
            "  @SuppressWarnings(\"TypeMemberOrder\")",
            "  static class InnerClass {}",
            "",
            "  static final int foo = 1;",
            "",
            "  void bar();",
            "",
            "  static interface InnerInterface {}",
            "",
            "  @SuppressWarnings(\"TypeMemberOrder\")",
            "  static final int baz = 1;",
            "",
            "  static enum InnerEnum {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementDanglingComments() {
    BugCheckerRefactoringTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "interface A {",
            "  // `bar` method's dangling comment.",
            "  ;",
            "  // `bar` method's comment",
            "  void bar();",
            "",
            "  // `foo` method's comment",
            "  static final int foo = 1;",
            "  // trailing comment",
            "}")
        .addOutputLines(
            "A.java",
            "interface A {",
            "",
            "  // `foo` method's comment",
            "  static final int foo = 1;",
            "  // `bar` method's dangling comment.",
            "  ;",
            "",
            "  // `bar` method's comment",
            "  void bar();",
            "  // trailing comment",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
