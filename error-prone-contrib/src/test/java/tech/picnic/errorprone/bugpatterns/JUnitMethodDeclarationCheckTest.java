package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class JUnitMethodDeclarationCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(JUnitMethodDeclarationCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(JUnitMethodDeclarationCheck.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import org.junit.jupiter.api.AfterAll;",
            "import org.junit.jupiter.api.AfterEach;",
            "import org.junit.jupiter.api.BeforeAll;",
            "import org.junit.jupiter.api.BeforeEach;",
            "import org.junit.jupiter.api.Test;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "",
            "class A {",
            "  @BeforeAll void setUp1() {}",
            "  // BUG: Diagnostic contains:",
            "  @BeforeAll public void setUp2() {}",
            "  // BUG: Diagnostic contains:",
            "  @BeforeAll protected void setUp3() {}",
            "  // BUG: Diagnostic contains:",
            "  @BeforeAll private void setUp4() {}",
            "",
            "  @BeforeEach void setup5() {}",
            "  // BUG: Diagnostic contains:",
            "  @BeforeEach public void setUp6() {}",
            "  // BUG: Diagnostic contains:",
            "  @BeforeEach protected void setUp7() {}",
            "  // BUG: Diagnostic contains:",
            "  @BeforeEach private void setUp8() {}",
            "",
            "  @AfterEach void tearDown1() {}",
            "  // BUG: Diagnostic contains:",
            "  @AfterEach public void tearDown2() {}",
            "  // BUG: Diagnostic contains:",
            "  @AfterEach protected void tearDown3() {}",
            "  // BUG: Diagnostic contains:",
            "  @AfterEach private void tearDown4() {}",
            "",
            "  @AfterAll void tearDown5() {}",
            "  // BUG: Diagnostic contains:",
            "  @AfterAll public void tearDown6() {}",
            "  // BUG: Diagnostic contains:",
            "  @AfterAll protected void tearDown7() {}",
            "  // BUG: Diagnostic contains:",
            "  @AfterAll private void tearDown8() {}",
            "",
            "  @Test void method1() {}",
            "  // BUG: Diagnostic contains:",
            "  @Test void testMethod2() {}",
            "  // BUG: Diagnostic contains:",
            "  @Test public void method3() {}",
            "  // BUG: Diagnostic contains:",
            "  @Test protected void method4() {}",
            "  // BUG: Diagnostic contains:",
            "  @Test private void method5() {}",
            "",
            "  @ParameterizedTest void method6() {}",
            "  // BUG: Diagnostic contains:",
            "  @ParameterizedTest void testMethod7() {}",
            "  // BUG: Diagnostic contains:",
            "  @ParameterizedTest public void method8() {}",
            "  // BUG: Diagnostic contains:",
            "  @ParameterizedTest protected void method9() {}",
            "  // BUG: Diagnostic contains:",
            "  @ParameterizedTest private void method10() {}",
            "",
            "  @BeforeEach @BeforeAll @AfterEach @AfterAll void testNonTestMethod1() {}",
            "  public void testNonTestMethod2() {}",
            "  protected void testNonTestMethod3() {}",
            "  private void testNonTestMethod4() {}",
            "  @Test void test5() {}",
            "}")
        .addSourceLines(
            "B.java",
            "import org.junit.jupiter.api.AfterAll;",
            "import org.junit.jupiter.api.AfterEach;",
            "import org.junit.jupiter.api.BeforeAll;",
            "import org.junit.jupiter.api.BeforeEach;",
            "import org.junit.jupiter.api.Test;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "",
            "class B extends A {",
            "  @Override @BeforeAll void setUp1() {}",
            "  @Override @BeforeAll public void setUp2() {}",
            "  @Override @BeforeAll protected void setUp3() {}",
            "",
            "  @Override @BeforeEach void setup5() {}",
            "  @Override @BeforeEach public void setUp6() {}",
            "  @Override @BeforeEach protected void setUp7() {}",
            "",
            "  @Override @AfterEach void tearDown1() {}",
            "  @Override @AfterEach public void tearDown2() {}",
            "  @Override @AfterEach protected void tearDown3() {}",
            "",
            "  @Override @AfterAll void tearDown5() {}",
            "  @Override @AfterAll public void tearDown6() {}",
            "  @Override @AfterAll protected void tearDown7() {}",
            "",
            "  @Override @Test void method1() {}",
            "  @Override @Test void testMethod2() {}",
            "  @Override @Test public void method3() {}",
            "  @Override @Test protected void method4() {}",
            "",
            "  @Override @ParameterizedTest void method6() {}",
            "  @Override @ParameterizedTest void testMethod7() {}",
            "  @Override @ParameterizedTest public void method8() {}",
            "  @Override @ParameterizedTest protected void method9() {}",
            "",
            "  @Override @BeforeEach @BeforeAll @AfterEach @AfterAll void testNonTestMethod1() {}",
            "  @Override public void testNonTestMethod2() {}",
            "  @Override protected void testNonTestMethod3() {}",
            "  @Override @Test void test5() {}",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "import org.junit.jupiter.api.AfterAll;",
            "import org.junit.jupiter.api.AfterEach;",
            "import org.junit.jupiter.api.BeforeAll;",
            "import org.junit.jupiter.api.BeforeEach;",
            "import org.junit.jupiter.api.RepeatedTest;",
            "import org.junit.jupiter.api.Test;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "",
            "class A {",
            "  @BeforeAll public void setUp1() {}",
            "  @BeforeEach protected void setUp2() {}",
            "  @AfterEach private void setUp3() {}",
            "  @AfterAll private void setUp4() {}",
            "",
            "  @Test void testFoo() {}",
            "  @ParameterizedTest void testBar() {}",
            "",
            "  @Test public void baz() {}",
            "  @RepeatedTest(2) private void qux() {}",
            "  @ParameterizedTest protected void quux() {}",
            "}")
        .addOutputLines(
            "out/A.java",
            "import org.junit.jupiter.api.AfterAll;",
            "import org.junit.jupiter.api.AfterEach;",
            "import org.junit.jupiter.api.BeforeAll;",
            "import org.junit.jupiter.api.BeforeEach;",
            "import org.junit.jupiter.api.RepeatedTest;",
            "import org.junit.jupiter.api.Test;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "",
            "class A {",
            "  @BeforeAll void setUp1() {}",
            "  @BeforeEach void setUp2() {}",
            "  @AfterEach void setUp3() {}",
            "  @AfterAll void setUp4() {}",
            "",
            "  @Test void foo() {}",
            "  @ParameterizedTest void bar() {}",
            "",
            "  @Test void baz() {}",
            "  @RepeatedTest(2) void qux() {}",
            "  @ParameterizedTest void quux() {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void methodAlreadyExistsInClass() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import org.junit.jupiter.api.Test;",
            "",
            "class A {",
            "  @Test void testFoo() {}",
            "  void foo() {}",
            "",
            "  @Test void testBar() {}",
            "  private void bar() {}",
            "",
            "  @Test void testFooDifferent() {}",
            "  @Test void testBarDifferent() {}",
            "}")
        .addOutputLines(
            "A.java",
            "import org.junit.jupiter.api.Test;",
            "",
            "class A {",
            "  @Test void testFoo() {}",
            "  void foo() {}",
            "",
            "  @Test void testBar() {}",
            "  private void bar() {}",
            "",
            "  @Test void fooDifferent() {}",
            "  @Test void barDifferent() {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void methodAlreadyInStaticImports() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import static com.google.common.collect.ImmutableSet.toImmutableSet;",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "",
            "import org.junit.jupiter.api.Test;",
            "import com.google.common.collect.ImmutableSet;",
            "",
            "class A {",
            "  @Test",
            "  void testArguments() {",
            "    arguments(1, 2, 3);",
            "  }",
            "",
            "  @Test",
            "  void testToImmutableSet() {",
            "    ImmutableSet.of(1).stream().filter(i -> i > 1).collect(toImmutableSet());",
            "  }",
            "",
            "  @Test",
            "  void testArgumentsDifferentName() {}",
            "",
            "  @Test",
            "  void testToImmutableSetDifferentName() {}",
            "}")
        .addOutputLines(
            "A.java",
            "import static com.google.common.collect.ImmutableSet.toImmutableSet;",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "",
            "import org.junit.jupiter.api.Test;",
            "import com.google.common.collect.ImmutableSet;",
            "",
            "class A {",
            "  @Test",
            "  void testArguments() {",
            "    arguments(1, 2, 3);",
            "  }",
            "",
            "  @Test",
            "  void testToImmutableSet() {",
            "    ImmutableSet.of(1).stream().filter(i -> i > 1).collect(toImmutableSet());",
            "  }",
            "",
            "  @Test",
            "  void argumentsDifferentName() {}",
            "",
            "  @Test",
            "  void toImmutableSetDifferentName() {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void methodHasJavaKeyword() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import org.junit.jupiter.api.Test;",
            "",
            "class A {",
            "  @Test",
            "  void testClass() {}",
            "",
            "  @Test",
            "  void testClazz() {}",
            "",
            "  @Test",
            "  void testThrow() {}",
            "",
            "  @Test",
            "  void testThrowww() {}",
            "}")
        .addOutputLines(
            "A.java",
            "import org.junit.jupiter.api.Test;",
            "",
            "class A {",
            "  @Test",
            "  void testClass() {}",
            "",
            "  @Test",
            "  void clazz() {}",
            "",
            "  @Test",
            "  void testThrow() {}",
            "",
            "  @Test",
            "  void throwww() {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
