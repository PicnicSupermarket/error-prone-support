package tech.picnic.errorprone.experimental.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class TypeMemberOrderTest {
  @Test
  void identificationClass() {
    CompilationTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains:",
            "class A {",
            "  private final int bar = 2;",
            "  private static final int foo = 1;",
            "}")
        .addSourceLines(
            "B.java",
            "class B {",
            "  private static final int foo = 1;",
            "  private final int bar = 2;",
            "}")
        .doTest();
  }

  @Test
  void identificationInterface() {
    CompilationTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains:",
            "interface A {",
            "  void bar();",
            "",
            "  static final int foo = 1;",
            "}")
        .addSourceLines(
            "B.java", "interface B {", "  static final int foo = 1;", "", "  void bar();", "}")
        .doTest();
  }

  @Test
  void identificationEnum() {
    CompilationTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains:",
            "enum A {",
            "  FOO;",
            "  final int baz = 2;",
            "  static final int BAR = 1;",
            "}")
        .addSourceLines(
            "B.java",
            "enum B {",
            "  FOO;",
            "  static final int BAR = 1;",
            "  final int baz = 2;",
            "}")
        .doTest();
  }

  @Test
  void identificationAnnotation() {
    CompilationTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addSourceLines(
            "A.java", "@interface A {", "  final int baz = 2;", "  static final int BAR = 1;", "}")
        .doTest();
  }

  @Test
  void identificationRecord() {
    CompilationTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addSourceLines(
            "A.java", "record A() {", "  void baz() {}", "", "  static final int BAR = 1;", "}")
        .doTest();
  }

  @Test
  // TODO: Extend case with @Nested
  void replacementClass() {
    BugCheckerRefactoringTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "  // `InnerClass` comment",
            "  class InnerClass {}",
            "",
            "  // dangling comment 1",
            "  ;",
            "",
            "  interface InnerInterface {}",
            "",
            "  enum InnerEnum {}",
            "",
            "  void baz() {",
            "    System.out.println(\"hello, world!\");",
            "  }",
            "",
            "  A() {}",
            "",
            "  {",
            "    System.out.println(\"hello, world!\");",
            "  }",
            "",
            "  static {",
            "    System.out.println(\"hello, world!\");",
            "  }",
            "",
            "  final int bar = 2;",
            "",
            "  static final int foo = 1;",
            "",
            "  // dangling comment 2",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "",
            "  static final int foo = 1;",
            "",
            "  final int bar = 2;",
            "",
            "  static {",
            "    System.out.println(\"hello, world!\");",
            "  }",
            "",
            "  {",
            "    System.out.println(\"hello, world!\");",
            "  }",
            "",
            "  A() {}",
            "",
            "  void baz() {",
            "    System.out.println(\"hello, world!\");",
            "  }",
            "",
            "  // `InnerClass` comment",
            "  class InnerClass {}",
            "",
            "  // dangling comment 1",
            "  ;",
            "",
            "  interface InnerInterface {}",
            "",
            "  enum InnerEnum {}",
            "",
            "  // dangling comment 2",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementInterface() {
    BugCheckerRefactoringTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "interface A {",
            "  // `InnerClass` comment",
            "  class InnerClass {}",
            "",
            "  // dangling comment 1",
            "  ;",
            "",
            "  interface InnerInterface {}",
            "",
            "  enum InnerEnum {}",
            "",
            "  default void baz() {",
            "    System.out.println(\"hello, world!\");",
            "  }",
            "",
            "  void qux();",
            "",
            "  static final int foo = 1;",
            "",
            "  // dangling comment 2",
            "}")
        .addOutputLines(
            "A.java",
            "interface A {",
            "",
            "  static final int foo = 1;",
            "",
            "  default void baz() {",
            "    System.out.println(\"hello, world!\");",
            "  }",
            "",
            "  void qux();",
            "",
            "  // `InnerClass` comment",
            "  class InnerClass {}",
            "",
            "  // dangling comment 1",
            "  ;",
            "",
            "  interface InnerInterface {}",
            "",
            "  enum InnerEnum {}",
            "",
            "  // dangling comment 2",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementEnum() {
    BugCheckerRefactoringTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "enum A {",
            "  // E1 comment",
            "  E1,",
            "  E2,",
            "  // E3 comment",
            "  E3",
            "// dangling comment 1",
            ";",
            "",
            "  // `InnerClass` comment",
            "  class InnerClass {}",
            "",
            "  // dangling comment 2",
            "  ;",
            "",
            "  interface InnerInterface {}",
            "",
            "  enum InnerEnum {}",
            "",
            "  void baz() {",
            "    System.out.println(\"hello, world!\");",
            "  }",
            "",
            "  A() {}",
            "",
            "  {",
            "    System.out.println(\"hello, world!\");",
            "  }",
            "",
            "  static {",
            "    System.out.println(\"hello, world!\");",
            "  }",
            "",
            "  final int bar = 2;",
            "",
            "  static final int foo = 1;",
            "",
            "  // dangling comment 3",
            "}")
        .addOutputLines(
            "A.java",
            "enum A {",
            "  // E1 comment",
            "  E1,",
            "  E2,",
            "  // E3 comment",
            "  E3",
            "// dangling comment 1",
            ";",
            "",
            "  static final int foo = 1;",
            "",
            "  final int bar = 2;",
            "",
            "  static {",
            "    System.out.println(\"hello, world!\");",
            "  }",
            "",
            "  {",
            "    System.out.println(\"hello, world!\");",
            "  }",
            "",
            "  A() {}",
            "",
            "  void baz() {",
            "    System.out.println(\"hello, world!\");",
            "  }",
            "",
            "  // `InnerClass` comment",
            "  class InnerClass {}",
            "",
            "  // dangling comment 2",
            "  ;",
            "",
            "  interface InnerInterface {}",
            "",
            "  enum InnerEnum {}",
            "",
            "  // dangling comment 3",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementNestedClasses() {
    BugCheckerRefactoringTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "  class InnerClass {",
            "    int baz;",
            "",
            "    static int bar;",
            "  }",
            "",
            "  static int foo;",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "",
            "  static int foo;",
            "",
            "  class InnerClass {",
            "",
            "    static int bar;",
            "    int baz;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementAbstractMethods() {
    BugCheckerRefactoringTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "abstract class A {",
            "  static class InnerClass {}",
            "",
            "  void foo() {}",
            "",
            "  abstract void bar();",
            "",
            "  void baz() {}",
            "",
            "  A() {}",
            "}")
        .addOutputLines(
            "A.java",
            "abstract class A {",
            "",
            "  A() {}",
            "",
            "  void foo() {}",
            "",
            "  abstract void bar();",
            "",
            "  void baz() {}",
            "",
            "  static class InnerClass {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementHandlesGeneratedDefaultConstructor() {
    BugCheckerRefactoringTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "  int foo() {",
            "    return foo;",
            "  }",
            "",
            "  {",
            "    System.out.println(\"bar\");",
            "  }",
            "",
            "  static {",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "  private final int bar = 2;",
            "  private static final int foo = 1;",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "  private static final int foo = 1;",
            "",
            "  private final int bar = 2;",
            "",
            "  static {",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "  {",
            "    System.out.println(\"bar\");",
            "  }",
            "",
            "  int foo() {",
            "    return foo;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementDanglingComments() {
    BugCheckerRefactoringTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "  /* empty statement's dangling comment */",
            "  ;",
            "  /**",
            "   * Multiline JavaDoc",
            "   *",
            "   * <p>`foo` method's comment",
            "   */",
            "  int foo() {",
            "    return foo;",
            "  }",
            "",
            "  // initializer block's comment",
            "  {",
            "    System.out.println(\"bar\");",
            "  }",
            "",
            "  // static initializer block's comment",
            "  static {",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "  /* `bar` field's dangling comment */",
            "",
            "  private final int bar = 2;",
            "  // `foo` field's comment",
            "  private static final int foo = 1;",
            "  // trailing comment",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "  // `foo` field's comment",
            "  private static final int foo = 1;",
            "",
            "  /* `bar` field's dangling comment */",
            "",
            "  private final int bar = 2;",
            "",
            "  // static initializer block's comment",
            "  static {",
            "    System.out.println(\"foo\");",
            "  }",
            "",
            "  // initializer block's comment",
            "  {",
            "    System.out.println(\"bar\");",
            "  }",
            "  /* empty statement's dangling comment */",
            "  ;",
            "",
            "  /**",
            "   * Multiline JavaDoc",
            "   *",
            "   * <p>`foo` method's comment",
            "   */",
            "  int foo() {",
            "    return foo;",
            "  }",
            "  // trailing comment",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementComplexAnnotation() {
    BugCheckerRefactoringTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "",
            "  @interface AnnotationWithClassReferences {",
            "    Class<?>[] value() default {};",
            "  }",
            "",
            "  @AnnotationWithClassReferences({Object.class})",
            "  class InnerClassOneValue {",
            "    String bar;",
            "    private static final int foo = 1;",
            "  }",
            "",
            "  @AnnotationWithClassReferences(value = {Integer.class, String.class})",
            "  class InnerClassTwoValues {",
            "    String bar;",
            "    private static final int foo = 1;",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "",
            "  @interface AnnotationWithClassReferences {",
            "    Class<?>[] value() default {};",
            "  }",
            "",
            "  @AnnotationWithClassReferences({Object.class})",
            "  class InnerClassOneValue {",
            "    private static final int foo = 1;",
            "    String bar;",
            "  }",
            "",
            "  @AnnotationWithClassReferences(value = {Integer.class, String.class})",
            "  class InnerClassTwoValues {",
            "    private static final int foo = 1;",
            "    String bar;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementJUnitNestedAnnotation() {
    BugCheckerRefactoringTestHelper.newInstance(TypeMemberOrder.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "",
            "  void foo () {}",
            "",
            "  @org.junit.jupiter.api.Nested",
            "  class JUnitNested1 {}",
            "",
            "  void bar () {}",
            "",
            "  class NormalClass {}",
            "",
            "  @org.junit.jupiter.api.Nested",
            "  class JUnitNested2 {}",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "",
            "  void foo () {}",
            "",
            "  @org.junit.jupiter.api.Nested",
            "  class JUnitNested1 {}",
            "",
            "  void bar () {}",
            "",
            "  @org.junit.jupiter.api.Nested",
            "  class JUnitNested2 {}",
            "",
            "  class NormalClass {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
