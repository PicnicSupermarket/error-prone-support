package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class ErrorProneTestHelperSourceFormatCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(ErrorProneTestHelperSourceFormatCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(
          ErrorProneTestHelperSourceFormatCheck.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
            "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
            "import com.google.errorprone.CompilationTestHelper;",
            "import tech.picnic.errorprone.bugpatterns.EmptyMethodCheck;",
            "",
            "class A {",
            "  private final CompilationTestHelper compilationTestHelper =",
            "      CompilationTestHelper.newInstance(EmptyMethodCheck.class, getClass());",
            "  private final BugCheckerRefactoringTestHelper refactoringTestHelper =",
            "      BugCheckerRefactoringTestHelper.newInstance(EmptyMethodCheck.class, getClass());",
            "",
            "  void m() {",
            "    compilationTestHelper",
            "        // BUG: Diagnostic contains: No source code provided",
            "        .addSourceLines(\"A.java\")",
            "        // BUG: Diagnostic contains: Source code is malformed:",
            "        .addSourceLines(\"A.java\", \"class A {\")",
            "        // Well-formed code, so not flagged.",
            "        .addSourceLines(\"A.java\", \"class A {}\")",
            "        // Malformed code, but not compile-time constant, so not flagged.",
            "        .addSourceLines(\"A.java\", \"class A {\" + getClass())",
            "        // BUG: Diagnostic contains: Test code should follow the Google Java style",
            "        .addSourceLines(\"A.java\", \"class A { }\")",
            "        .doTest();",
            "",
            "    refactoringTestHelper",
            "        // BUG: Diagnostic contains: Test code should follow the Google Java style",
            "        .addInputLines(\"in/A.java\", \"class A { }\")",
            "        // BUG: Diagnostic contains: Test code should follow the Google Java style",
            "        .addOutputLines(\"out/A.java\", \"class A { }\")",
            "        // BUG: Diagnostic contains: Test code should follow the Google Java style",
            "        .addInputLines(\"in/B.java\", \"import java.util.Map;\", \"\", \"class B {}\")",
            "        // Unused import, but in an output file, so not flagged.",
            "        .addOutputLines(\"out/B.java\", \"import java.util.Map;\", \"\", \"class B {}\")",
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
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
            "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
            "import com.google.errorprone.CompilationTestHelper;",
            "import tech.picnic.errorprone.bugpatterns.EmptyMethodCheck;",
            "",
            "class A {",
            "  private final CompilationTestHelper compilationTestHelper =",
            "      CompilationTestHelper.newInstance(EmptyMethodCheck.class, getClass());",
            "  private final BugCheckerRefactoringTestHelper refactoringTestHelper =",
            "      BugCheckerRefactoringTestHelper.newInstance(EmptyMethodCheck.class, getClass());",
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
            "            \"in/A.java\",",
            "            \"import java.util.Map;\",",
            "            \"import java.util.Collection;\",",
            "            \"import java.util.List;\",",
            "            \"\",",
            "            \"interface A extends List<A>, Map<A,A> { }\")",
            "        .addOutputLines(",
            "            \"out/A.java\",",
            "            \"import java.util.Map;\",",
            "            \"import java.util.Collection;\",",
            "            \"import java.util.List;\",",
            "            \"\",",
            "            \"interface A extends List<A>, Map<A,A> { }\")",
            "        .doTest(TestMode.TEXT_MATCH);",
            "  }",
            "}")
        .addOutputLines(
            "out/A.java",
            "import com.google.errorprone.BugCheckerRefactoringTestHelper;",
            "import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;",
            "import com.google.errorprone.CompilationTestHelper;",
            "import tech.picnic.errorprone.bugpatterns.EmptyMethodCheck;",
            "",
            "class A {",
            "  private final CompilationTestHelper compilationTestHelper =",
            "      CompilationTestHelper.newInstance(EmptyMethodCheck.class, getClass());",
            "  private final BugCheckerRefactoringTestHelper refactoringTestHelper =",
            "      BugCheckerRefactoringTestHelper.newInstance(EmptyMethodCheck.class, getClass());",
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
            "            \"in/A.java\",",
            "            \"import java.util.List;\",",
            "            \"import java.util.Map;\",",
            "            \"\",",
            "            \"interface A extends List<A>, Map<A, A> {}\")",
            "        .addOutputLines(",
            "            \"out/A.java\",",
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
