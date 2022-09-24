package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class RefasterTemplateModifiersTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(RefasterTemplateModifiers.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(RefasterTemplateModifiers.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "",
            "final class A {",
            "  @BeforeTemplate",
            "  String before(String str) {",
            "    return str;",
            "  }",
            "",
            "  String nonRefasterMethod(String str) {",
            "    return str;",
            "  }",
            "",
            "  static final class Inner {",
            "    @BeforeTemplate",
            "    String before(String str) {",
            "      return str;",
            "    }",
            "  }",
            "}")
        .addSourceLines(
            "B.java",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "import com.google.errorprone.refaster.annotation.Placeholder;",
            "",
            "abstract class B<I, O> {",
            "  @Placeholder",
            "  abstract O someFunction(I input);",
            "",
            "  @BeforeTemplate",
            "  String before(I input) {",
            "    return String.valueOf(someFunction(input));",
            "  }",
            "",
            "  abstract static class Inner<I, O> {",
            "    @Placeholder",
            "    abstract O someFunction(I input);",
            "",
            "    @BeforeTemplate",
            "    String before(I input) {",
            "      return String.valueOf(someFunction(input));",
            "    }",
            "  }",
            "}")
        .addSourceLines(
            "C.java",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "",
            "// BUG: Diagnostic contains:",
            "class C {",
            "  @BeforeTemplate",
            "  // BUG: Diagnostic contains:",
            "  final String beforeFinal(String str) {",
            "    return str;",
            "  }",
            "",
            "  @BeforeTemplate",
            "  // BUG: Diagnostic contains:",
            "  private String beforePrivate(String str) {",
            "    return str;",
            "  }",
            "",
            "  @BeforeTemplate",
            "  // BUG: Diagnostic contains:",
            "  public String beforePublic(String str) {",
            "    return str;",
            "  }",
            "",
            "  @BeforeTemplate",
            "  // BUG: Diagnostic contains:",
            "  static String beforeStatic(String str) {",
            "    return str;",
            "  }",
            "",
            "  @BeforeTemplate",
            "  // BUG: Diagnostic contains:",
            "  synchronized String beforeSynchronized(String str) {",
            "    return str;",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  final class NonStaticInner {",
            "    @BeforeTemplate",
            "    String before(String str) {",
            "      return str;",
            "    }",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  static class NonFinalInner {",
            "    @BeforeTemplate",
            "    String before(String str) {",
            "      return str;",
            "    }",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  abstract static class AbstractInner {",
            "    @BeforeTemplate",
            "    String before(String str) {",
            "      return str;",
            "    }",
            "  }",
            "}")
        .addSourceLines(
            "D.java",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "",
            "// BUG: Diagnostic contains:",
            "abstract class D {",
            "  @BeforeTemplate",
            "  // BUG: Diagnostic contains:",
            "  protected String beforeProtected(String str) {",
            "    return str;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "",
            "class A {",
            "  @BeforeTemplate",
            "  private static String before(String str) {",
            "    return str;",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "",
            "final class A {",
            "  @BeforeTemplate",
            "  String before(String str) {",
            "    return str;",
            "  }",
            "}")
        .addInputLines(
            "B.java",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "import com.google.errorprone.refaster.annotation.Placeholder;",
            "",
            "final class B {",
            "  abstract class WithoutPlaceholder {",
            "    @BeforeTemplate",
            "    protected synchronized String before(String str) {",
            "      return str;",
            "    }",
            "  }",
            "",
            "  abstract class WithPlaceholder<I, O> {",
            "    @Placeholder",
            "    public abstract O someFunction(I input);",
            "",
            "    @BeforeTemplate",
            "    public final String before(I input) {",
            "      return String.valueOf(someFunction(input));",
            "    }",
            "  }",
            "}")
        .addOutputLines(
            "B.java",
            "import com.google.errorprone.refaster.annotation.BeforeTemplate;",
            "import com.google.errorprone.refaster.annotation.Placeholder;",
            "",
            "final class B {",
            "  static final class WithoutPlaceholder {",
            "    @BeforeTemplate",
            "    String before(String str) {",
            "      return str;",
            "    }",
            "  }",
            "",
            "  abstract static class WithPlaceholder<I, O> {",
            "    @Placeholder",
            "    abstract O someFunction(I input);",
            "",
            "    @BeforeTemplate",
            "    String before(I input) {",
            "      return String.valueOf(someFunction(input));",
            "    }",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
