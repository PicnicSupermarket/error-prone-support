package tech.picnic.errorprone.guidelines.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class ErrorProneTestHelperSourceFormatTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(ErrorProneTestHelperSourceFormat.class, getClass())
        .addSourceLines(
            "A.java",
            "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
            "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
            "import com.google.errorprone.CompilationTestHelper;",
            "import tech.picnic.errorprone.guidelines.bugpatterns.RefasterAnyOfUsage;",
            "",
            "class A {",
            "  private final CompilationTestHelper compilationTestHelper =",
            "      CompilationTestHelper.newInstance(RefasterAnyOfUsage.class, getClass());",
            "  private final BugCheckerRefactoringTestHelper refactoringTestHelper =",
            "      BugCheckerRefactoringTestHelper.newInstance(RefasterAnyOfUsage.class, getClass());",
            "",
            "  void m() {",
            "    compilationTestHelper",
            "        // BUG: Diagnostic contains: No source code provided",
            "        .addSourceLines(\"A.java\")",
            "        // BUG: Diagnostic contains: Source code is malformed:",
            "        .addSourceLines(\"B.java\", \"class B {\")",
            "        // Well-formed code, so not flagged.",
            "        .addSourceLines(\"C.java\", \"class C {}\")",
            "        // Malformed code, but not compile-time constant, so not flagged.",
            "        .addSourceLines(\"D.java\", \"class D {\" + getClass())",
            "        // BUG: Diagnostic contains: Test code should follow the Google Java style",
            "        .addSourceLines(\"E.java\", \"class E { }\")",
            "        .doTest();",
            "",
            "    refactoringTestHelper",
            "        // BUG: Diagnostic contains: Test code should follow the Google Java style",
            "        .addInputLines(\"A.java\", \"class A { }\")",
            "        // BUG: Diagnostic contains: Test code should follow the Google Java style",
            "        .addOutputLines(\"A.java\", \"class A { }\")",
            "        // BUG: Diagnostic contains: Test code should follow the Google Java style",
            "        .addInputLines(\"B.java\", \"import java.util.Map;\", \"\", \"class B {}\")",
            "        // Unused import, but in an output file, so not flagged.",
            "        .addOutputLines(\"B.java\", \"import java.util.Map;\", \"\", \"class B {}\")",
            "        .doTest(TestMode.TEXT_MATCH);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    /*
     * Verifies that import sorting and code formatting is performed unconditionally, while unused
     * imports are removed unless part of a `BugCheckerRefactoringTestHelper` expected output file.
     */
    BugCheckerRefactoringTestHelper.newInstance(ErrorProneTestHelperSourceFormat.class, getClass())
        .addInputLines(
            "A.java",
            "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
            "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
            "import com.google.errorprone.CompilationTestHelper;",
            "import tech.picnic.errorprone.guidelines.bugpatterns.RefasterAnyOfUsage;",
            "",
            "class A {",
            "  private final CompilationTestHelper compilationTestHelper =",
            "      CompilationTestHelper.newInstance(RefasterAnyOfUsage.class, getClass());",
            "  private final BugCheckerRefactoringTestHelper refactoringTestHelper =",
            "      BugCheckerRefactoringTestHelper.newInstance(RefasterAnyOfUsage.class, getClass());",
            "",
            "  void m() {",
            "    compilationTestHelper",
            "        .addSourceLines(",
            "            \"A.java\",",
            "            \"import java.util.Map;\",",
            "            \"import java.util.Collection;\",",
            "            \"import java.util.List;\",",
            "            \"\",",
            "            \"interface A extends List<A>, Map<A,A> { }\")",
            "        .doTest();",
            "",
            "    refactoringTestHelper",
            "        .addInputLines(",
            "            \"A.java\",",
            "            \"import java.util.Map;\",",
            "            \"import java.util.Collection;\",",
            "            \"import java.util.List;\",",
            "            \"\",",
            "            \"interface A extends List<A>, Map<A,A> { }\")",
            "        .addOutputLines(",
            "            \"A.java\",",
            "            \"import java.util.Map;\",",
            "            \"import java.util.Collection;\",",
            "            \"import java.util.List;\",",
            "            \"\",",
            "            \"interface A extends List<A>, Map<A,A> { }\")",
            "        .doTest(TestMode.TEXT_MATCH);",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
            "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
            "import com.google.errorprone.CompilationTestHelper;",
            "import tech.picnic.errorprone.guidelines.bugpatterns.RefasterAnyOfUsage;",
            "",
            "class A {",
            "  private final CompilationTestHelper compilationTestHelper =",
            "      CompilationTestHelper.newInstance(RefasterAnyOfUsage.class, getClass());",
            "  private final BugCheckerRefactoringTestHelper refactoringTestHelper =",
            "      BugCheckerRefactoringTestHelper.newInstance(RefasterAnyOfUsage.class, getClass());",
            "",
            "  void m() {",
            "    compilationTestHelper",
            "        .addSourceLines(",
            "            \"A.java\",",
            "            \"import java.util.List;\",",
            "            \"import java.util.Map;\",",
            "            \"\",",
            "            \"interface A extends List<A>, Map<A, A> {}\")",
            "        .doTest();",
            "",
            "    refactoringTestHelper",
            "        .addInputLines(",
            "            \"A.java\",",
            "            \"import java.util.List;\",",
            "            \"import java.util.Map;\",",
            "            \"\",",
            "            \"interface A extends List<A>, Map<A, A> {}\")",
            "        .addOutputLines(",
            "            \"A.java\",",
            "            \"import java.util.Collection;\",",
            "            \"import java.util.List;\",",
            "            \"import java.util.Map;\",",
            "            \"\",",
            "            \"interface A extends List<A>, Map<A, A> {}\")",
            "        .doTest(TestMode.TEXT_MATCH);",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
