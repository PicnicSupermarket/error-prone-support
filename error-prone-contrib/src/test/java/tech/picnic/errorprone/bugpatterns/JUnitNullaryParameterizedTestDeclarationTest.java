package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class JUnitNullaryParameterizedTestDeclarationTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(JUnitNullaryParameterizedTestDeclaration.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import org.junit.jupiter.api.Test;
            import org.junit.jupiter.params.ParameterizedTest;
            import org.junit.jupiter.params.provider.ValueSource;

            class A {
              void nonTest() {}

              @Test
              void nonParameterizedTest() {}

              @ParameterizedTest
              @ValueSource(ints = {0, 1})
              void goodParameterizedTest(int someInt) {}

              @ParameterizedTest
              @ValueSource(ints = {0, 1})
              // BUG: Diagnostic contains:
              void nullaryParameterizedTest() {}
            }
            """)
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(
            JUnitNullaryParameterizedTestDeclaration.class, getClass())
        .addInputLines(
            "A.java",
            """
            import org.junit.jupiter.params.ParameterizedTest;
            import org.junit.jupiter.params.provider.ArgumentsProvider;
            import org.junit.jupiter.params.provider.ArgumentsSource;
            import org.junit.jupiter.params.provider.ArgumentsSources;
            import org.junit.jupiter.params.provider.MethodSource;
            import org.junit.jupiter.params.provider.ValueSource;

            class A {
              @ParameterizedTest
              void withoutArgumentSource() {}

              @ParameterizedTest
              @ArgumentsSource(ArgumentsProvider.class)
              void withCustomArgumentSource() {}

              @ParameterizedTest
              @ArgumentsSources({
                @ArgumentsSource(ArgumentsProvider.class),
                @ArgumentsSource(ArgumentsProvider.class)
              })
              void withCustomerArgumentSources() {}

              /** Foo. */
              @ParameterizedTest
              @ValueSource(ints = {0, 1})
              void withValueSourceAndJavadoc() {}

              @ParameterizedTest
              @MethodSource("nonexistentMethod")
              @SuppressWarnings("foo")
              void withMethodSourceAndUnrelatedAnnotation() {}

              @org.junit.jupiter.params.ParameterizedTest
              @ArgumentsSource(ArgumentsProvider.class)
              @ValueSource(ints = {0, 1})
              @MethodSource("nonexistentMethod")
              void withMultipleArgumentSourcesAndFullyQualifiedImport() {}

              class NestedWithTestAnnotationFirst {
                @ParameterizedTest
                @ValueSource(ints = {0, 1})
                void withValueSource() {}
              }

              class NestedWithTestAnnotationSecond {
                @ValueSource(ints = {0, 1})
                @ParameterizedTest
                void withValueSource() {}
              }
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import org.junit.jupiter.api.Test;
            import org.junit.jupiter.params.ParameterizedTest;
            import org.junit.jupiter.params.provider.ArgumentsProvider;
            import org.junit.jupiter.params.provider.ArgumentsSource;
            import org.junit.jupiter.params.provider.ArgumentsSources;
            import org.junit.jupiter.params.provider.MethodSource;
            import org.junit.jupiter.params.provider.ValueSource;

            class A {
              @Test
              void withoutArgumentSource() {}

              @Test
              void withCustomArgumentSource() {}

              @Test
              void withCustomerArgumentSources() {}

              /** Foo. */
              @Test
              void withValueSourceAndJavadoc() {}

              @Test
              @SuppressWarnings("foo")
              void withMethodSourceAndUnrelatedAnnotation() {}

              @Test
              void withMultipleArgumentSourcesAndFullyQualifiedImport() {}

              class NestedWithTestAnnotationFirst {
                @Test
                void withValueSource() {}
              }

              class NestedWithTestAnnotationSecond {
                @Test
                void withValueSource() {}
              }
            }
            """)
        .addInputLines(
            "B.java",
            """
            import org.junit.jupiter.params.ParameterizedTest;

            class B {
              @ParameterizedTest
              void scopeInWhichIdentifierTestIsAlreadyDeclared() {}

              class Test {}
            }
            """)
        .addOutputLines(
            "B.java",
            """
            import org.junit.jupiter.params.ParameterizedTest;

            class B {
              @org.junit.jupiter.api.Test
              void scopeInWhichIdentifierTestIsAlreadyDeclared() {}

              class Test {}
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
