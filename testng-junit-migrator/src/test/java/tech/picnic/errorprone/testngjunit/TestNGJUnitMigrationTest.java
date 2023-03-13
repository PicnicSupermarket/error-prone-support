package tech.picnic.errorprone.testngjunit;

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
            "import java.util.stream.Stream;",
            "import org.testng.annotations.DataProvider;",
            "import org.testng.annotations.Test;",
            "",
            "// BUG: Diagnostic contains:",
            "@Test",
            "public class A {",
            "  public void classLevelAnnotation() {}",
            "",
            "  private void notATest() {}",
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
            "  // BUG: Diagnostic contains:",
            "  private static Object[][] dataProviderTestCases() {",
            "    return new Object[][] {{1}, {2}};",
            "  }",
            "",
            "  @DataProvider",
            "  private static Object[][] unusedDataProvider() {",
            "    return new Object[][] {{1}, {2}};",
            "  }",
            "",
            "  @Test(dataProvider = \"notMigratableDataProviderTestCases\")",
            "  public void notMigratableDataProvider(int foo) {}",
            "",
            "  @DataProvider",
            "  private static Object[][] notMigratableDataProviderTestCases() {",
            "    return Stream.of(1, 2, 3).map(i -> new Object[] {i}).toArray(Object[][]::new);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void identificationConservativeMode() {
    CompilationTestHelper.newInstance(TestNGJUnitMigration.class, getClass())
        .setArgs("-XepOpt:TestNGJUnitMigration:ConservativeMode=true")
        .addSourceLines(
            "A.java",
            "import java.util.stream.Stream;",
            "import org.testng.annotations.DataProvider;",
            "import org.testng.annotations.Test;",
            "",
            "@Test",
            "public class A {",
            "  public void classLevelAnnotation() {}",
            "",
            "  @Test(description = \"bar\")",
            "  public void methodAnnotation() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Test",
            "  public static class B {",
            "    public void nestedClass() {}",
            "  }",
            "",
            "  @Test(dataProvider = \"notMigratableDataProviderTestCases\")",
            "  public void notMigratableDataProvider(int foo) {}",
            "",
            "  @DataProvider",
            "  private static Object[][] notMigratableDataProviderTestCases() {",
            "    return Stream.of(1, 2, 3).map(i -> new Object[] {i}).toArray(Object[][]::new);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(TestNGJUnitMigration.class, getClass())
        .addInputLines(
            "A.java",
            "import org.testng.annotations.BeforeMethod;",
            "import org.testng.annotations.DataProvider;",
            "import org.testng.annotations.Test;",
            "",
            "@Test",
            "class A {",
            "  @BeforeMethod",
            "  private void setup() {}",
            "",
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
            "  public void singleExpectedException() {",
            "    throw new RuntimeException(\"foo\");",
            "  }",
            "",
            "  @Test(expectedExceptions = {IllegalArgumentException.class, RuntimeException.class})",
            "  public void multipleExpectedExceptions() {",
            "    throw new RuntimeException(\"foo\");",
            "  }",
            "",
            "  @Test(enabled = false)",
            "  public void disabledTest() {}",
            "",
            "  @Test(enabled = true)",
            "  public void enabledTest() {}",
            "}")
        .addOutputLines(
            "A.java",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "",
            "import java.util.stream.Stream;",
            "import org.junit.jupiter.api.Disabled;",
            "import org.junit.jupiter.api.DisplayName;",
            "import org.junit.jupiter.api.MethodOrderer;",
            "import org.junit.jupiter.api.Order;",
            "import org.junit.jupiter.api.TestMethodOrder;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "import org.junit.jupiter.params.provider.Arguments;",
            "import org.junit.jupiter.params.provider.MethodSource;",
            "import org.testng.annotations.BeforeMethod;",
            "import org.testng.annotations.Test;",
            "",
            "@TestMethodOrder(MethodOrderer.OrderAnnotation.class)",
            "class A {",
            "  @org.junit.jupiter.api.BeforeEach",
            "  private void setup() {}",
            "",
            "  @org.junit.jupiter.api.Test",
            "  public void foo() {}",
            "",
            "  @Order(1)",
            "  @DisplayName(\"unit\")",
            "  @org.junit.jupiter.api.Test",
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
            "  @org.junit.jupiter.api.Test",
            "  public void singleExpectedException() {",
            "    org.junit.jupiter.api.Assertions.assertThrows(",
            "        RuntimeException.class,",
            "        () -> {",
            "          throw new RuntimeException(\"foo\");",
            "        });",
            "  }",
            "",
            "  // XXX: Removed handling of `RuntimeException.class` because this migration doesn't support",
            "  // XXX: multiple expected exceptions.",
            "  @org.junit.jupiter.api.Test",
            "  public void multipleExpectedExceptions() {",
            "    org.junit.jupiter.api.Assertions.assertThrows(",
            "        IllegalArgumentException.class,",
            "        () -> {",
            "          throw new RuntimeException(\"foo\");",
            "        });",
            "  }",
            "",
            "  @Disabled",
            "  @org.junit.jupiter.api.Test",
            "  public void disabledTest() {}",
            "",
            "  @org.junit.jupiter.api.Test",
            "  public void enabledTest() {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
