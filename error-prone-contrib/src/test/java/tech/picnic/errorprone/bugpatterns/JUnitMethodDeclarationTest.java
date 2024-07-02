package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class JUnitMethodDeclarationTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(JUnitMethodDeclaration.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import static org.junit.jupiter.params.provider.Arguments.arguments;

            import org.junit.jupiter.api.AfterAll;
            import org.junit.jupiter.api.AfterEach;
            import org.junit.jupiter.api.BeforeAll;
            import org.junit.jupiter.api.BeforeEach;
            import org.junit.jupiter.api.Test;
            import org.junit.jupiter.params.ParameterizedTest;

            class A {
              {
                arguments();
              }

              @BeforeAll
              void setUp1() {}

              @BeforeAll
              // BUG: Diagnostic contains:
              public void setUp2() {}

              @BeforeAll
              // BUG: Diagnostic contains:
              protected void setUp3() {}

              @BeforeAll
              // BUG: Diagnostic contains:
              private void setUp4() {}

              @BeforeEach
              void setup5() {}

              @BeforeEach
              // BUG: Diagnostic contains:
              public void setUp6() {}

              @BeforeEach
              // BUG: Diagnostic contains:
              protected void setUp7() {}

              @BeforeEach
              // BUG: Diagnostic contains:
              private void setUp8() {}

              @AfterEach
              void tearDown1() {}

              @AfterEach
              // BUG: Diagnostic contains:
              public void tearDown2() {}

              @AfterEach
              // BUG: Diagnostic contains:
              protected void tearDown3() {}

              @AfterEach
              // BUG: Diagnostic contains:
              private void tearDown4() {}

              @AfterAll
              void tearDown5() {}

              @AfterAll
              // BUG: Diagnostic contains:
              public void tearDown6() {}

              @AfterAll
              // BUG: Diagnostic contains:
              protected void tearDown7() {}

              @AfterAll
              // BUG: Diagnostic contains:
              private void tearDown8() {}

              @Test
              void test() {}

              @Test
              void method1() {}

              @Test
              // BUG: Diagnostic contains:
              void testMethod2() {}

              @Test
              // BUG: Diagnostic contains:
              public void method3() {}

              @Test
              // BUG: Diagnostic contains:
              protected void method4() {}

              @Test
              // BUG: Diagnostic contains:
              private void method5() {}

              @ParameterizedTest
              void method6() {}

              @ParameterizedTest
              // BUG: Diagnostic contains:
              void testMethod7() {}

              @ParameterizedTest
              // BUG: Diagnostic contains:
              public void method8() {}

              @ParameterizedTest
              // BUG: Diagnostic contains:
              protected void method9() {}

              @ParameterizedTest
              // BUG: Diagnostic contains:
              private void method10() {}

              @BeforeEach
              @BeforeAll
              @AfterEach
              @AfterAll
              void testNonTestMethod1() {}

              public void testNonTestMethod2() {}

              protected void testNonTestMethod3() {}

              private void testNonTestMethod4() {}

              @Test
              void test5() {}

              @Test
              // BUG: Diagnostic contains: (but note that a method named `toString` is already defined in this
              // class or a supertype)
              void testToString() {}

              @Test
              // BUG: Diagnostic contains: (but note that a method named `overload` is already defined in this
              // class or a supertype)
              void testOverload() {}

              void overload() {}

              @Test
              // BUG: Diagnostic contains: (but note that `arguments` is already statically imported)
              void testArguments() {}

              @Test
              // BUG: Diagnostic contains: (but note that `public` is not a valid identifier)
              void testPublic() {}

              @Test
              // BUG: Diagnostic contains: (but note that `null` is not a valid identifier)
              void testNull() {}

              @Test
              // BUG: Diagnostic contains:
              void testRecord() {}

              @Test
              // BUG: Diagnostic contains:
              void testMethodThatIsOverriddenWithoutOverrideAnnotation() {}
            }
            """)
        .addSourceLines(
            "B.java",
            """
            import org.junit.jupiter.api.AfterAll;
            import org.junit.jupiter.api.AfterEach;
            import org.junit.jupiter.api.BeforeAll;
            import org.junit.jupiter.api.BeforeEach;
            import org.junit.jupiter.api.Test;
            import org.junit.jupiter.params.ParameterizedTest;

            class B extends A {
              @Override
              @BeforeAll
              void setUp1() {}

              @Override
              @BeforeAll
              public void setUp2() {}

              @Override
              @BeforeAll
              protected void setUp3() {}

              @Override
              @BeforeEach
              void setup5() {}

              @Override
              @BeforeEach
              public void setUp6() {}

              @Override
              @BeforeEach
              protected void setUp7() {}

              @Override
              @AfterEach
              void tearDown1() {}

              @Override
              @AfterEach
              public void tearDown2() {}

              @Override
              @AfterEach
              protected void tearDown3() {}

              @Override
              @AfterAll
              void tearDown5() {}

              @Override
              @AfterAll
              public void tearDown6() {}

              @Override
              @AfterAll
              protected void tearDown7() {}

              @Override
              @Test
              void test() {}

              @Override
              @Test
              void method1() {}

              @Override
              @Test
              void testMethod2() {}

              @Override
              @Test
              public void method3() {}

              @Override
              @Test
              protected void method4() {}

              @Override
              @ParameterizedTest
              void method6() {}

              @Override
              @ParameterizedTest
              void testMethod7() {}

              @Override
              @ParameterizedTest
              public void method8() {}

              @Override
              @ParameterizedTest
              protected void method9() {}

              @Override
              @BeforeEach
              @BeforeAll
              @AfterEach
              @AfterAll
              void testNonTestMethod1() {}

              @Override
              public void testNonTestMethod2() {}

              @Override
              protected void testNonTestMethod3() {}

              @Override
              @Test
              void test5() {}

              @Override
              @Test
              void testToString() {}

              @Override
              @Test
              void testOverload() {}

              @Override
              void overload() {}

              @Override
              @Test
              void testArguments() {}

              @Override
              @Test
              void testPublic() {}

              @Override
              @Test
              void testNull() {}

              @Override
              @Test
              void testRecord() {}

              @Test
              void testMethodThatIsOverriddenWithoutOverrideAnnotation() {}
            }
            """)
        .addSourceLines(
            "C.java",
            """
            import org.junit.jupiter.api.AfterAll;
            import org.junit.jupiter.api.BeforeAll;
            import org.junit.jupiter.api.Test;

            abstract class C {
              @BeforeAll
              public void setUp() {}

              @Test
              void testMethod1() {}

              @AfterAll
              // BUG: Diagnostic contains:
              private void tearDown() {}

              @Test
              // BUG: Diagnostic contains:
              final void testMethod2() {}
            }
            """)
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(JUnitMethodDeclaration.class, getClass())
        .addInputLines(
            "A.java",
            """
            import static org.junit.jupiter.params.provider.Arguments.arguments;

            import org.junit.jupiter.api.AfterAll;
            import org.junit.jupiter.api.AfterEach;
            import org.junit.jupiter.api.BeforeAll;
            import org.junit.jupiter.api.BeforeEach;
            import org.junit.jupiter.api.RepeatedTest;
            import org.junit.jupiter.api.Test;
            import org.junit.jupiter.params.ParameterizedTest;

            class A {
              {
                arguments();
              }

              @BeforeAll
              public void setUp1() {}

              @BeforeEach
              protected void setUp2() {}

              @AfterEach
              private void setUp3() {}

              @AfterAll
              private void setUp4() {}

              @Test
              void testFoo() {}

              @ParameterizedTest
              void testBar() {}

              @Test
              public void baz() {}

              @RepeatedTest(2)
              private void qux() {}

              @ParameterizedTest
              protected void quux() {}

              @Test
              public void testToString() {}

              @Test
              public void testOverload() {}

              void overload() {}

              @Test
              protected void testArguments() {}

              @Test
              private void testClass() {}

              @Test
              private void testTrue() {}
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import static org.junit.jupiter.params.provider.Arguments.arguments;

            import org.junit.jupiter.api.AfterAll;
            import org.junit.jupiter.api.AfterEach;
            import org.junit.jupiter.api.BeforeAll;
            import org.junit.jupiter.api.BeforeEach;
            import org.junit.jupiter.api.RepeatedTest;
            import org.junit.jupiter.api.Test;
            import org.junit.jupiter.params.ParameterizedTest;

            class A {
              {
                arguments();
              }

              @BeforeAll
              void setUp1() {}

              @BeforeEach
              void setUp2() {}

              @AfterEach
              void setUp3() {}

              @AfterAll
              void setUp4() {}

              @Test
              void foo() {}

              @ParameterizedTest
              void bar() {}

              @Test
              void baz() {}

              @RepeatedTest(2)
              void qux() {}

              @ParameterizedTest
              void quux() {}

              @Test
              void testToString() {}

              @Test
              void testOverload() {}

              void overload() {}

              @Test
              void testArguments() {}

              @Test
              void testClass() {}

              @Test
              void testTrue() {}
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
