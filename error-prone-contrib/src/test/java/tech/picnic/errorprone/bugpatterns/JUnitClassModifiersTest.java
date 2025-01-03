package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class JUnitClassModifiersTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(JUnitClassModifiers.class, getClass())
        .addSourceLines(
            "Container.java",
            """
            import org.junit.jupiter.api.Test;
            import org.junit.jupiter.params.ParameterizedTest;
            import org.springframework.boot.test.context.TestConfiguration;
            import org.springframework.context.annotation.Configuration;

            class Container {
              final class FinalAndPackagePrivate {
                @Test
                void foo() {}
              }

              final class FinalAndPackagePrivateWithCustomTestMethod {
                @ParameterizedTest
                void foo() {}
              }

              public abstract class Abstract {
                @Test
                void foo() {}
              }

              @Configuration
              class WithConfigurationAnnotation {
                @Test
                void foo() {}
              }

              @TestConfiguration
              class WithConfigurationMetaAnnotation {
                @Test
                void foo() {}
              }

              // BUG: Diagnostic contains:
              private final class Private {
                @Test
                void foo() {}
              }

              // BUG: Diagnostic contains:
              protected final class Protected {
                @Test
                void foo() {}
              }

              // BUG: Diagnostic contains:
              public final class Public {
                @Test
                void foo() {}
              }

              // BUG: Diagnostic contains:
              class NonFinal {
                @Test
                void foo() {}
              }

              // BUG: Diagnostic contains:
              class NonFinalWithCustomTestMethod {
                @ParameterizedTest
                void foo() {}
              }

              @Configuration
              // BUG: Diagnostic contains:
              public class PublicWithConfigurationAnnotation {
                @Test
                void foo() {}
              }

              @TestConfiguration
              // BUG: Diagnostic contains:
              protected class ProtectedWithConfigurationMetaAnnotation {
                @Test
                void foo() {}
              }
            }
            """)
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(JUnitClassModifiers.class, getClass())
        .addInputLines(
            "A.java",
            """
            import org.junit.jupiter.api.Test;
            import org.springframework.context.annotation.Configuration;

            public class A {
              @Test
              void foo() {}

              @Configuration
              private static class B {
                @Test
                void bar() {}
              }
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import org.junit.jupiter.api.Test;
            import org.springframework.context.annotation.Configuration;

            final class A {
              @Test
              void foo() {}

              @Configuration
              static class B {
                @Test
                void bar() {}
              }
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
