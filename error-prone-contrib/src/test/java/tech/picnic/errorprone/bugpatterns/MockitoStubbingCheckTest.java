package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class MockitoStubbingCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(MockitoStubbingCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(MockitoStubbingCheck.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static org.mockito.ArgumentMatchers.eq;",
            "import static org.mockito.ArgumentMatchers.notNull;",
            "import static org.mockito.Mockito.doAnswer;",
            "import static org.mockito.Mockito.mock;",
            "",
            "import java.util.function.BiConsumer;",
            "import java.util.function.Consumer;",
            "import org.mockito.ArgumentMatchers;",
            "",
            "class A {",
            "  void m() {",
            "    Runnable runnable = mock(Runnable.class);",
            "    doAnswer(inv -> null).when(runnable).run();",
            "",
            "    Consumer<String> consumer = mock(Consumer.class);",
            "    doAnswer(inv -> null).when(consumer).accept(\"foo\");",
            "    doAnswer(inv -> null).when(consumer).accept(notNull());",
            "    // BUG: Diagnostic contains:",
            "    doAnswer(inv -> null).when(consumer).accept(ArgumentMatchers.eq(\"foo\"));",
            "    // BUG: Diagnostic contains:",
            "    doAnswer(inv -> null).when(consumer).accept(eq(toString()));",
            "",
            "    BiConsumer<Integer, String> biConsumer = mock(BiConsumer.class);",
            "    doAnswer(inv -> null).when(biConsumer).accept(0, \"foo\");",
            "    doAnswer(inv -> null).when(biConsumer).accept(eq(0), notNull());",
            "    doAnswer(inv -> null).when(biConsumer).accept(notNull(), eq(\"foo\"));",
            "    doAnswer(inv -> null)",
            "        .when(biConsumer)",
            "        // BUG: Diagnostic contains:",
            "        .accept(ArgumentMatchers.eq(0), ArgumentMatchers.eq(\"foo\"));",
            "    // BUG: Diagnostic contains:",
            "    doAnswer(inv -> null).when(biConsumer).accept(eq(hashCode()), eq(toString()));",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "import static org.mockito.ArgumentMatchers.eq;",
            "import static org.mockito.Mockito.doAnswer;",
            "import static org.mockito.Mockito.mock;",
            "",
            "import java.util.function.BiConsumer;",
            "import java.util.function.Consumer;",
            "import org.mockito.ArgumentMatchers;",
            "",
            "class A {",
            "  void m() {",
            "    Consumer<String> consumer = mock(Consumer.class);",
            "    doAnswer(inv -> null).when(consumer).accept(ArgumentMatchers.eq(\"foo\"));",
            "    doAnswer(inv -> null).when(consumer).accept(eq(toString()));",
            "",
            "    BiConsumer<Integer, String> biConsumer = mock(BiConsumer.class);",
            "    doAnswer(inv -> null)",
            "        .when(biConsumer)",
            "        .accept(ArgumentMatchers.eq(0), ArgumentMatchers.eq(\"foo\"));",
            "    doAnswer(inv -> null).when(biConsumer).accept(eq(hashCode()), eq(toString()));",
            "  }",
            "}")
        .addOutputLines(
            "out/A.java",
            "import static org.mockito.ArgumentMatchers.eq;",
            "import static org.mockito.Mockito.doAnswer;",
            "import static org.mockito.Mockito.mock;",
            "",
            "import java.util.function.BiConsumer;",
            "import java.util.function.Consumer;",
            "import org.mockito.ArgumentMatchers;",
            "",
            "class A {",
            "  void m() {",
            "    Consumer<String> consumer = mock(Consumer.class);",
            "    doAnswer(inv -> null).when(consumer).accept(\"foo\");",
            "    doAnswer(inv -> null).when(consumer).accept(toString());",
            "",
            "    BiConsumer<Integer, String> biConsumer = mock(BiConsumer.class);",
            "    doAnswer(inv -> null).when(biConsumer).accept(0, \"foo\");",
            "    doAnswer(inv -> null).when(biConsumer).accept(hashCode(), toString());",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
