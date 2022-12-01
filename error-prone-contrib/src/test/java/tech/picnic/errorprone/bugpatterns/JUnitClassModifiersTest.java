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
            "Container.java",
            "import org.junit.jupiter.api.Test;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "import org.springframework.boot.test.context.TestConfiguration;",
            "import org.springframework.context.annotation.Configuration;",
            "",
            "class Container {",
            "  final class FinalAndPackagePrivate {",
            "    @Test",
            "    void foo() {}",
            "  }",
            "",
            "  public abstract class Abstract {",
            "    @Test",
            "    void foo() {}",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  class NonFinal {",
            "    @Test",
            "    void foo() {}",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  class NonFinalWithCustomTestMethod {",
            "    @ParameterizedTest",
            "    void foo() {}",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  public final class Public {",
            "    @Test",
            "    void foo() {}",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  private final class Private {",
            "    @Test",
            "    void foo() {}",
            "  }",
            "",
            "  @Configuration",
            "  // BUG: Diagnostic contains:",
            "  public class WithConfigurationAnnotation {",
            "    @Test",
            "    void foo() {}",
            "  }",
            "",
            "  @TestConfiguration",
            "  class WithConfigurationMetaAnnotation {",
            "    @Test",
            "    void foo() {}",
            "  }",
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
