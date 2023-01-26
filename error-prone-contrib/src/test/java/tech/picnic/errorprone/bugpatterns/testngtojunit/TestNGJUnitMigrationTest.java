package tech.picnic.errorprone.bugpatterns.testngtojunit;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class TestNGJUnitMigrationTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(TestNGJUnitMigration.class, getClass())
        .addSourceLines(
            "A.java",
            "import org.testng.annotations.DataProvider;",
            "import org.testng.annotations.Test;",
            "",
            "// BUG: Diagnostic contains:",
            "@Test",
            "public class A {",
            "  public void classLevelAnnotation() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Test(description = \"bar\")",
            "  public void methodAnnotation() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Test",
            "  public static class B {",
            "    public void nestedClass() {}",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Test(dataProvider = \"dataProviderTestCases\")",
            "  public void dataProvider(int foo) {}",
            "",
            "  @DataProvider",
            "  private static Object[][] dataProviderTestCases() {",
            "    return new Object[][] {{1}, {2}};",
            "  }",
            "",
            "  @DataProvider",
            "  private static Object[][] unusedDataProvider() {",
            "    return new Object[][] {{1}, {2}};",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(TestNGJUnitMigration.class, getClass())
        .addInputLines(
            "A.java",
            "import org.testng.annotations.DataProvider;",
            "import org.testng.annotations.Test;",
            "",
            "@Test",
            "class A {",
            "  public void foo() {}",
            "",
            "  @Test(priority = 1, description = \"unit\")",
            "  public void bar() {}",
            "",
            "  @Test(dataProvider = \"bazNumbers\")",
            "  public void baz(String string, int number) {}",
            "",
            "  @DataProvider",
            "  private Object[][] bazNumbers() {",
            "    int[] values = new int[] {1, 2};",
            "    return new Object[][] {",
            "      {\"1\", values[0]},",
            "      {\"2\", values[1]}",
            "    };",
            "  }",
            "",
            "  @Test(expectedExceptions = RuntimeException.class)",
            "  public void qux() {",
            "    throw new RuntimeException(\"foo\");",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "",
            "import java.util.stream.Stream;",
            "import org.junit.jupiter.api.DisplayName;",
            "import org.junit.jupiter.api.MethodOrderer;",
            "import org.junit.jupiter.api.Order;",
            "import org.junit.jupiter.api.Test;",
            "import org.junit.jupiter.api.TestMethodOrder;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "import org.junit.jupiter.params.provider.Arguments;",
            "import org.junit.jupiter.params.provider.MethodSource;",
            "",
            "@TestMethodOrder(MethodOrderer.OrderAnnotation.class)",
            "class A {",
            "  @Test",
            "  public void foo() {}",
            "",
            "  @Order(1)",
            "  @DisplayName(\"unit\")",
            "  @Test",
            "  public void bar() {}",
            "",
            "  @ParameterizedTest",
            "  @MethodSource(\"bazNumbers\")",
            "  public void baz(String string, int number) {}",
            "",
            "  private static Stream<Arguments> bazNumbers() {",
            "    int[] values = new int[] {1, 2};",
            "    return Stream.of(arguments(\"1\", values[0]), arguments(\"2\", values[1]));",
            "  }",
            "",
            "  @Test",
            "  public void qux() {",
            "    org.junit.jupiter.api.Assertions.assertThrows(",
            "        RuntimeException.class,",
            "        () -> {",
            "          throw new RuntimeException(\"foo\");",
            "        });",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
