package tech.picnic.errorprone.guidelines.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class ErrorProneRuntimeClasspathTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(ErrorProneRuntimeClasspath.class, getClass())
        .expectErrorMessage(
            "USE_CLASS_REFERENCE",
            m ->
                m.contains(
                    "This type will be on the runtime classpath; use `Class#getCanonicalName()` instead"))
        .expectErrorMessage(
            "USE_STRING_LITERAL",
            m ->
                m.contains(
                    "This type may not be on the runtime classpath; use a string literal instead"))
        .addSourceLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
            "import com.google.errorprone.BugPattern;",
            "import com.google.errorprone.CompilationTestHelper;",
            "import org.junit.jupiter.api.Test;",
            "",
            "class A {",
            "  @SuppressWarnings(\"java.lang.String\")",
            "  void m(Object o) {",
            "    m(null);",
            "    m(0);",
            "    m(getClass().getName());",
            "    m(getClass().getCanonicalName());",
            "    m(\"\");",
            "    m(\"foo\");",
            "    m(\"java.util.\");",
            "",
            "    m(\"org.junit.jupiter.api.Test\");",
            "    m(\"org.junit.jupiter.api.Test.toString\");",
            "    m(\"com.google.errorprone.CompilationTestHelper\");",
            "    m(\"com.google.errorprone.CompilationTestHelper.toString\");",
            "    m(\"com.google.errorprone.BugCheckerRefactoringTestHelper.ExpectOutput\");",
            "    m(\"com.google.errorprone.BugCheckerRefactoringTestHelper.ExpectOutput.toString\");",
            "    m(\"com.google.errorprone.NonExistent\");",
            "    m(\"com.google.common.NonExistent.toString\");",
            "    m(\"java.lang.NonExistent\");",
            "    m(\"com.google.common.collect.ImmutableEnumSet\");",
            "    // BUG: Diagnostic matches: USE_CLASS_REFERENCE",
            "    m(\"com.google.errorprone.BugPattern\");",
            "    // BUG: Diagnostic matches: USE_CLASS_REFERENCE",
            "    m(\"com.google.errorprone.util.ErrorProneToken\");",
            "    // BUG: Diagnostic matches: USE_CLASS_REFERENCE",
            "    m(\"com.google.common.collect.ImmutableList\");",
            "    // BUG: Diagnostic matches: USE_CLASS_REFERENCE",
            "    m(\"java.lang.String\");",
            "    // BUG: Diagnostic matches: USE_CLASS_REFERENCE",
            "    m(\"java.lang.String.toString\");",
            "",
            "    m(BugPattern.class.getCanonicalName());",
            "    m(ImmutableList.class.getCanonicalName());",
            "    m(String.class.getCanonicalName());",
            "    m(void.class.getCanonicalName());",
            "    m(boolean.class.getCanonicalName());",
            "    m(byte.class.getCanonicalName());",
            "    m(char.class.getCanonicalName());",
            "    m(short.class.getCanonicalName());",
            "    m(int.class.getCanonicalName());",
            "    m(long.class.getCanonicalName());",
            "    m(float.class.getCanonicalName());",
            "    m(double.class.getCanonicalName());",
            "    m(java.lang.Iterable.class.getCanonicalName());",
            "    m(CompilationTestHelper.class.toString());",
            "    // BUG: Diagnostic matches: USE_STRING_LITERAL",
            "    m(CompilationTestHelper.class.getCanonicalName());",
            "    // BUG: Diagnostic matches: USE_STRING_LITERAL",
            "    m(BugCheckerRefactoringTestHelper.ExpectOutput.class.getCanonicalName());",
            "    // BUG: Diagnostic matches: USE_STRING_LITERAL",
            "    m(Test.class.getCanonicalName());",
            "    // BUG: Diagnostic matches: USE_STRING_LITERAL",
            "    m(org.junit.jupiter.api.Nested.class.getCanonicalName());",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(ErrorProneRuntimeClasspath.class, getClass())
        .addInputLines(
            "A.java",
            "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
            "import com.google.errorprone.CompilationTestHelper;",
            "import org.junit.jupiter.api.Test;",
            "",
            "class A {",
            "  void m(Object o) {",
            "    m(\"com.google.errorprone.BugPattern\");",
            "    m(\"com.google.errorprone.util.ErrorProneToken\");",
            "    m(\"com.google.common.collect.ImmutableList\");",
            "    m(\"java.lang.String\");",
            "    m(\"java.lang.String.toString\");",
            "",
            "    m(CompilationTestHelper.class.getCanonicalName());",
            "    m(BugCheckerRefactoringTestHelper.ExpectOutput.class.getCanonicalName());",
            "    m(Test.class.getCanonicalName());",
            "    m(org.junit.jupiter.api.Nested.class.getCanonicalName());",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
            "import com.google.errorprone.BugPattern;",
            "import com.google.errorprone.CompilationTestHelper;",
            "import com.google.errorprone.util.ErrorProneToken;",
            "import org.junit.jupiter.api.Test;",
            "",
            "class A {",
            "  void m(Object o) {",
            "    m(BugPattern.class.getCanonicalName());",
            "    m(ErrorProneToken.class.getCanonicalName());",
            "    m(ImmutableList.class.getCanonicalName());",
            "    m(String.class.getCanonicalName());",
            "    m(String.class.getCanonicalName() + \".toString\");",
            "",
            "    m(\"com.google.errorprone.CompilationTestHelper\");",
            "    m(\"com.google.errorprone.BugCheckerRefactoringTestHelper.ExpectOutput\");",
            "    m(\"org.junit.jupiter.api.Test\");",
            "    m(\"org.junit.jupiter.api.Nested\");",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
