package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class TestNGParameterizedTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(TestNGParameterized.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(TestNGParameterized.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "",
            "import java.util.stream.Stream;",
            "import org.junit.jupiter.params.provider.Arguments;",
            "import org.testng.annotations.DataProvider;",
            "import org.testng.annotations.Test;",
            "",
            "class A {",
            "  // BUG: Diagnostic contains:",
            "  @Test(dataProvider = \"fooNumbers\")",
            "  public void foo(String first, int second) {}",
            "",
            "  @DataProvider",
            "  private Object[][] fooNumbers() {",
            "    return new Object[][] {",
            "      {\"1\", 1},",
            "      {\"2\", 2}",
            "    };",
            "  }",
            "",
            "  @SuppressWarnings(\"UnusedMethod\")",
            "  private static final Stream<Arguments> fooNumbersJunit() {",
            "    return Stream.of(arguments(\"1\", 1), arguments(\"2\", 2));",
            "  }",
            "",
            "  @Test(dataProvider = \"barNumbers\")",
            "  public void bar(String first, int second) {}",
            "",
            "  @DataProvider",
            "  private Object[][] barNumbers() {",
            "    return new Object[][] {",
            "      {\"1\", 1},",
            "      {\"2\", 2}",
            "    };",
            "  }",
            "",
            "  @Test(dataProvider = \"fooNumbers\", description = \"foo\")",
            "  public void secondFoo() {}",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "",
            "import java.util.stream.Stream;",
            "import org.junit.jupiter.params.provider.Arguments;",
            "import org.testng.annotations.DataProvider;",
            "import org.testng.annotations.Test;",
            "",
            "class A {",
            "",
            "  @Test(dataProvider = \"fooNumbers\")",
            "  public void foo(String string, int number) {}",
            "",
            "  @DataProvider",
            "  private Object[][] fooNumbers() {",
            "    int[] values = new int[] {1, 2};",
            "    return new Object[][] {",
            "      {\"1\", values[0]},",
            "      {\"2\", values[1]}",
            "    };",
            "  }",
            "",
            "  @SuppressWarnings(\"UnusedMethod\")",
            "  private static Stream<Arguments> fooNumbersJunit() {",
            "    int[] values = new int[] {1, 2};",
            "    return Stream.of(arguments(\"1\", values[0]), arguments(\"2\", values[1]));",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "",
            "import java.util.stream.Stream;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "import org.junit.jupiter.params.provider.Arguments;",
            "import org.junit.jupiter.params.provider.MethodSource;",
            "import org.testng.annotations.DataProvider;",
            "import org.testng.annotations.Test;",
            "",
            "class A {",
            "",
            "  @ParameterizedTest",
            "  @MethodSource(\"fooNumbers\")",
            "  public void foo(String string, int number) {}",
            "",
            "  private static Stream<Arguments> fooNumbers() {",
            "    int[] values = new int[] {1, 2};",
            "    return Stream.of(arguments(\"1\", values[0]), arguments(\"2\", values[1]));",
            "  }",
            "}")
        .doTest(TEXT_MATCH);
  }
}
