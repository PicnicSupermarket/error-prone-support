package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class AutowiredConstructorCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(AutowiredConstructorCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(AutowiredConstructorCheck.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "Container.java",
            "import com.google.errorprone.annotations.Immutable;",
            "import java.util.List;",
            "import org.springframework.beans.factory.annotation.Autowired;",
            "",
            "interface Container {",
            "  @Immutable",
            "  class A {",
            "    A() {}",
            "  }",
            "",
            "  class B {",
            "    @Autowired",
            "    void setProperty(Object o) {}",
            "  }",
            "",
            "  class C {",
            "    // BUG: Diagnostic contains:",
            "    @Autowired",
            "    C() {}",
            "  }",
            "",
            "  class D {",
            "    // BUG: Diagnostic contains:",
            "    @Autowired",
            "    D(String x) {}",
            "  }",
            "",
            "  class E {",
            "    @Autowired",
            "    E() {}",
            "",
            "    E(String x) {}",
            "  }",
            "",
            "  class F {",
            "    F() {}",
            "",
            "    @Autowired",
            "    F(String x) {}",
            "  }",
            "",
            "  class G {",
            "    @Autowired private Object o;",
            "  }",
            "",
            "  class H {",
            "    @SafeVarargs",
            "    H(List<String>... lists) {}",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "in/Container.java",
            "import org.springframework.beans.factory.annotation.Autowired;",
            "",
            "interface Container {",
            "  class A {",
            "    @Autowired",
            "    @Deprecated",
            "    A() {}",
            "  }",
            "",
            "  class B {",
            "    @Autowired",
            "    B(String x) {}",
            "  }",
            "}")
        .addOutputLines(
            "out/Container.java",
            "import org.springframework.beans.factory.annotation.Autowired;",
            "",
            "interface Container {",
            "  class A {",
            "",
            "    @Deprecated",
            "    A() {}",
            "  }",
            "",
            "  class B {",
            "",
            "    B(String x) {}",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
