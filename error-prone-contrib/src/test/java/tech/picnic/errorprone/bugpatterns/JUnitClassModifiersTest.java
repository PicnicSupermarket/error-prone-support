package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class JUnitClassModifiersTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(JUnitClassModifiers.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(JUnitClassModifiers.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import org.junit.jupiter.api.Test;",
            "",
            "// BUG: Diagnostic contains:",
            "class A {",
            "  @Test",
            "  void foo() {}",
            "}")
        .addSourceLines(
            "B.java",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "",
            "// BUG: Diagnostic contains:",
            "class B {",
            "  @ParameterizedTest",
            "  void foo() {}",
            "}")
        .addSourceLines(
            "C.java",
            "import org.junit.jupiter.api.Test;",
            "",
            "// BUG: Diagnostic contains:",
            "public class C {",
            "  @Test",
            "  void foo() {}",
            "}")
        .addSourceLines(
            "D.java",
            "import org.junit.jupiter.api.Nested;",
            "import org.junit.jupiter.api.Test;",
            "",
            "class D {",
            "  @Nested",
            "  // BUG: Diagnostic contains:",
            "  class Nested1 {",
            "    @Test",
            "    void foo() {}",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  static class Nested2 {",
            "    @Test",
            "    void bar() {}",
            "  }",
            "}")
        .addSourceLines(
            "E.java",
            "import org.junit.jupiter.api.Test;",
            "",
            "final class E {",
            "  @Test",
            "  void foo() {}",
            "}")
        .addSourceLines(
            "F.java",
            "import org.junit.jupiter.api.Test;",
            "import org.springframework.context.annotation.Configuration;",
            "",
            "@Configuration",
            "public class F {",
            "  @Test",
            "  void foo() {}",
            "}")
        .addSourceLines(
            "G.java",
            "import org.junit.jupiter.api.Test;",
            "import org.springframework.boot.test.context.TestConfiguration;",
            "",
            "@TestConfiguration",
            "public class G {",
            "  @Test",
            "  void foo() {}",
            "}")
        .addSourceLines(
            "H.java",
            "import org.junit.jupiter.api.Test;",
            "",
            "public abstract class H {",
            "  @Test",
            "  abstract void foo();",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import org.junit.jupiter.api.Test;",
            "",
            "public class A {",
            "  @Test",
            "  void foo() {}",
            "",
            "  private static class B {",
            "    @Test",
            "    void bar() {}",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import org.junit.jupiter.api.Test;",
            "",
            "final class A {",
            "  @Test",
            "  void foo() {}",
            "",
            "  static final class B {",
            "    @Test",
            "    void bar() {}",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
