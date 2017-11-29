package com.picnicinternational.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class AutowiredConstructorCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(AutowiredConstructorCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(new AutowiredConstructorCheck(), getClass());

  @Test
  public void testIdentification() {
    compilationTestHelper
        .addSourceLines(
            "Container.java",
            "import org.springframework.beans.factory.annotation.Autowired;",
            "",
            "interface Container {",
            "  class A {",
            "    A() {}",
            "  }",
            "",
            "  class B {",
            "    @Autowired void setProperty(Object o) {}",
            "  }",
            "",
            "  class C {",
            "    // BUG: Diagnostic contains:",
            "    @Autowired C() {}",
            "  }",
            "",
            "  class D {",
            "    // BUG: Diagnostic contains:",
            "    @Autowired D(String x) {}",
            "  }",
            "",
            "  class E {",
            "    @Autowired E() {}",
            "    E(String x) {}",
            "  }",
            "",
            "  class F {",
            "    F() {}",
            "    @Autowired F(String x) {}",
            "  }",
            "",
            "  class G {",
            "    @Autowired private Object o;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void testReplacement() throws IOException {
    refactoringTestHelper
        .addInputLines(
            "in/Container.java",
            "import org.springframework.beans.factory.annotation.Autowired;",
            "",
            "interface Container {",
            "  class A {",
            "    @Autowired A() {}",
            "  }",
            "",
            "  class B {",
            "    @Autowired B(String x) {}",
            "  }",
            "}")
        .addOutputLines(
            "out/Container.java",
            "import org.springframework.beans.factory.annotation.Autowired;",
            "",
            "interface Container {",
            "  class A {",
            "    A() {}",
            "  }",
            "",
            "  class B {",
            "    B(String x) {}",
            "  }",
            "}")
        .doTest();
  }
}
