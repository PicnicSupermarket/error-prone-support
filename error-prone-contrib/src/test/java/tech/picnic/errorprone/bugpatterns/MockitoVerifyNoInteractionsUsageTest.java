package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class MockitoVerifyNoInteractionsUsageTest {
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(
          MockitoVerifyNoInteractionsUsage.class, getClass());

  @Test
  void identification() {
    CompilationTestHelper.newInstance(MockitoVerifyNoInteractionsUsage.class, getClass())
        .addSourceLines(
            "A.java",
            "import static org.mockito.Mockito.mock;",
            "import static org.mockito.Mockito.verifyNoInteractions;",
            "",
            "import org.junit.jupiter.api.Test;",
            "",
            "class A {",
            "  @Test",
            "  void a() {}",
            "  @Test",
            "  void b() {",
            "    Runnable runnable = mock(Runnable.class);",
            "    verifyNoInteractions(runnable);",
            "  }",
            "  @Test",
            "  void c() {",
            "    Runnable runnable = mock(Runnable.class);",
            "    Runnable runnable1 = mock(Runnable.class);",
            "    verifyNoInteractions(runnable, runnable1);",
            "  }",
            "  @Test",
            "  // BUG: Diagnostic contains:",
            "  void d() {",
            "    Runnable runnable1 = mock(Runnable.class);",
            "    Runnable runnable2 = mock(Runnable.class);",
            "    verifyNoInteractions(runnable1);",
            "    verifyNoInteractions(runnable2);",
            "  }",
            "  @Test",
            "  // BUG: Diagnostic contains:",
            "  void e() {",
            "    Runnable runnable1 = mock(Runnable.class);",
            "    verifyNoInteractions(runnable1);",
            "    Runnable runnable2 = mock(Runnable.class);",
            "    verifyNoInteractions(runnable2);",
            "  }",
            "  @Test",
            "  // BUG: Diagnostic contains:",
            "  void f() {",
            "    Runnable runnable1 = mock(Runnable.class);",
            "    verifyNoInteractions(runnable1);",
            "    Runnable runnable2 = mock(Runnable.class);",
            "    Runnable runnable3 = mock(Runnable.class);",
            "    verifyNoInteractions(runnable2, runnable3);",
            "    Runnable runnable4 = mock(Runnable.class);",
            "    Runnable runnable5 = mock(Runnable.class);",
            "    verifyNoInteractions(runnable4);",
            "    verifyNoInteractions(runnable5);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replaceSequentialCalls() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import static org.mockito.Mockito.mock;",
            "import static org.mockito.Mockito.verifyNoInteractions;",
            "",
            "import org.junit.jupiter.api.Test;",
            "",
            "class A {",
            "  @Test",
            "  void m() {",
            "    Runnable runnable1 = mock(Runnable.class);",
            "    Runnable runnable2 = mock(Runnable.class);",
            "    verifyNoInteractions(runnable1);",
            "    verifyNoInteractions(runnable2);",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static org.mockito.Mockito.mock;",
            "import static org.mockito.Mockito.verifyNoInteractions;",
            "",
            "import org.junit.jupiter.api.Test;",
            "",
            "class A {",
            "  @Test",
            "  void m() {",
            "    Runnable runnable1 = mock(Runnable.class);",
            "    Runnable runnable2 = mock(Runnable.class);",
            "",
            "    verifyNoInteractions(runnable1, runnable2);",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replaceNonSequentialCalls() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import static org.mockito.Mockito.mock;",
            "import static org.mockito.Mockito.verifyNoInteractions;",
            "",
            "import org.junit.jupiter.api.Test;",
            "",
            "class A {",
            "  @Test",
            "  void m() {",
            "    Runnable runnable1 = mock(Runnable.class);",
            "    verifyNoInteractions(runnable1);",
            "    Runnable runnable2 = mock(Runnable.class);",
            "    verifyNoInteractions(runnable2);",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static org.mockito.Mockito.mock;",
            "import static org.mockito.Mockito.verifyNoInteractions;",
            "",
            "import org.junit.jupiter.api.Test;",
            "",
            "class A {",
            "  @Test",
            "  void m() {",
            "    Runnable runnable1 = mock(Runnable.class);",
            "",
            "    Runnable runnable2 = mock(Runnable.class);",
            "    verifyNoInteractions(runnable1, runnable2);",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replaceManyCalls() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import static org.mockito.Mockito.mock;",
            "import static org.mockito.Mockito.verifyNoInteractions;",
            "",
            "import org.junit.jupiter.api.Test;",
            "",
            "class A {",
            "  @Test",
            "  void m() {",
            "    Runnable runnable1 = mock(Runnable.class);",
            "    verifyNoInteractions(runnable1);",
            "    Runnable runnable2 = mock(Runnable.class);",
            "    Runnable runnable3 = mock(Runnable.class);",
            "    verifyNoInteractions(runnable2, runnable3);",
            "    Runnable runnable4 = mock(Runnable.class);",
            "    Runnable runnable5 = mock(Runnable.class);",
            "    verifyNoInteractions(runnable4);",
            "    verifyNoInteractions(runnable5);",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static org.mockito.Mockito.mock;",
            "import static org.mockito.Mockito.verifyNoInteractions;",
            "",
            "import org.junit.jupiter.api.Test;",
            "",
            "class A {",
            "  @Test",
            "  void m() {",
            "    Runnable runnable1 = mock(Runnable.class);",
            "",
            "    Runnable runnable2 = mock(Runnable.class);",
            "    Runnable runnable3 = mock(Runnable.class);",
            "",
            "    Runnable runnable4 = mock(Runnable.class);",
            "    Runnable runnable5 = mock(Runnable.class);",
            "",
            "    verifyNoInteractions(runnable1, runnable2, runnable3, runnable4, runnable5);",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
