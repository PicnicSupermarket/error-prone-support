package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class JunitMethodDeclarationCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(JunitMethodDeclarationCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(new JunitMethodDeclarationCheck(), getClass());

  @Test
  public void testIdentification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import org.junit.jupiter.api.Test;",
            "",
            "final class A {",
            "  @Test void foo() {}",
            "  @Test void method1() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Test void testMethod2() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Test public void method3() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Test protected void method4() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Test private void method5() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Test private void method6() { ",
            "    new Foo() {",
            "      @Override",
            "      public void bar() {}",
            "    };",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Test void testMethod7() { ",
            "    new Foo() {",
            "      @Override",
            "      public void bar() {}",
            "    };",
            "  }",
            "",
            "",
            "  @Test void method8() { ",
            "    new Foo() {",
            "      @Override",
            "      public void bar() {}",
            "    };",
            "  }",
            "",
            "}",
            "",
            "abstract class Foo { ",
            "  abstract public void bar();",
            "}")
        .addSourceLines(
            "B.java",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "",
            "final class B {",
            "  @ParameterizedTest void method1() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @ParameterizedTest void testMethod2() {}",
            "",
            "}")
        .doTest();
  }

  @Test
  public void testReplaceTestPrefix() {
    refactoringTestHelper
        .addInputLines(
            "in/Container.java",
            "import org.junit.jupiter.api.Test;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "",
            "interface Container {",
            "  class A {",
            "    @Test void test() {}",
            "    @Test void testMethod1() {}",
            "    @Test void test2() {}",
            "    @Test void method3() {}",
            "    void method4() {}",
            "  }",
            "",
            "  class B {",
            "    @ParameterizedTest void test() {}",
            "    @ParameterizedTest void testMethod1() {}",
            "    @ParameterizedTest void test2() {}",
            "    @ParameterizedTest void method3() {}",
            "    void method4() {}",
            "  }",
            "}")
        .addOutputLines(
            "out/Container.java",
            "import org.junit.jupiter.api.Test;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "",
            "interface Container {",
            "  class A {",
            "    @Test void test() {}",
            "    @Test void method1() {}",
            "    @Test void test2() {}",
            "    @Test void method3() {}",
            "    void method4() {}",
            "  }",
            "",
            "  class B {",
            "    @ParameterizedTest void test() {}",
            "    @ParameterizedTest void method1() {}",
            "    @ParameterizedTest void test2() {}",
            "    @ParameterizedTest void method3() {}",
            "    void method4() {}",
            "  }",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }

  @Test
  public void testDropModifiers() {
    refactoringTestHelper
        .addInputLines(
            "in/Container.java",
            "import org.junit.jupiter.api.AfterAll;",
            "import org.junit.jupiter.api.AfterEach;",
            "import org.junit.jupiter.api.BeforeAll;",
            "import org.junit.jupiter.api.BeforeEach;",
            "import org.junit.jupiter.api.Test;",
            "interface Container {",
            "  class A {",
            "    @BeforeEach public void setUp1() {}",
            "    @BeforeEach protected void setUp2() {}",
            "    @BeforeEach private void setUp3() {}",
            "    @BeforeEach void setUp4() {}",
            "    @BeforeAll public void setUp5() {}",
            "    @BeforeAll protected void setUp6() {}",
            "    @BeforeAll private void setUp7() {}",
            "    @BeforeAll void setUp8() {}",
            "    @AfterEach public void tearDown1() {}",
            "    @AfterEach protected void tearDown2() {}",
            "    @AfterEach private void tearDown3() {}",
            "    @AfterEach void tearDown4() {}",
            "    @AfterAll public void tearDown5() {}",
            "    @AfterAll protected void tearDown6() {}",
            "    @AfterAll private void tearDown7() {}",
            "    @AfterAll void tearDown8() {}",
            "    @Test void method1() {}",
            "    @Test public void method2() {}",
            "    @Test protected void method3() {}",
            "    @Test private void method4() {}",
            "    void method5() {}",
            "    public void method6() {}",
            "    protected void method7() {}",
            "    private void method8() {}",
            "  }",
            "}")
        .addOutputLines(
            "out/Container.java",
            "import org.junit.jupiter.api.AfterAll;",
            "import org.junit.jupiter.api.AfterEach;",
            "import org.junit.jupiter.api.BeforeAll;",
            "import org.junit.jupiter.api.BeforeEach;",
            "import org.junit.jupiter.api.Test;",
            "interface Container {",
            "  class A {",
            "    @BeforeEach void setUp1() {}",
            "    @BeforeEach void setUp2() {}",
            "    @BeforeEach void setUp3() {}",
            "    @BeforeEach void setUp4() {}",
            "    @BeforeAll void setUp5() {}",
            "    @BeforeAll void setUp6() {}",
            "    @BeforeAll void setUp7() {}",
            "    @BeforeAll void setUp8() {}",
            "    @AfterEach void tearDown1() {}",
            "    @AfterEach void tearDown2() {}",
            "    @AfterEach void tearDown3() {}",
            "    @AfterEach void tearDown4() {}",
            "    @AfterAll void tearDown5() {}",
            "    @AfterAll void tearDown6() {}",
            "    @AfterAll void tearDown7() {}",
            "    @AfterAll void tearDown8() {}",
            "    @Test void method1() {}",
            "    @Test void method2() {}",
            "    @Test void method3() {}",
            "    @Test void method4() {}",
            "    void method5() {}",
            "    public void method6() {}",
            "    protected void method7() {}",
            "    private void method8() {}",
            "  }",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }
}
