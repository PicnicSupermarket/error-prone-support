package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class TestNGDataProviderTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(TestNGDataProvider.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(TestNGDataProvider.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import org.testng.annotations.DataProvider;",
            "",
            "class A {",
            "  // BUG: Diagnostic contains:",
            "  @DataProvider",
            "  private Object[][] fooNumbers() {",
            "    return new Object[][] {",
            "      {\"1\", 1},",
            "      {\"2\", 2}",
            "    };",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  @DataProvider",
            "  private Object[] barNumbers() {",
            "    return new Object[][] {",
            "      {\"1\", 1},",
            "      {\"2\", 2}",
            "    };",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  @DataProvider",
            "  private Object[] bazNumbers() {",
            "    return new Object[] {1, 2};",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void alreadyMigratedIdentification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "",
            "import java.util.stream.Stream;",
            "import org.junit.jupiter.params.provider.Arguments;",
            "import org.testng.annotations.DataProvider;",
            "",
            "class A {",
            "  @DataProvider",
            "  private Object[][] quxNumbers() {",
            "    return new Object[][] {",
            "      {\"1\", 1},",
            "      {\"2\", 2}",
            "    };",
            "  }",
            "",
            "  @SuppressWarnings(\"UnusedMethod\" /* This is an intermediate state for the JUnit migration. */)",
            "  private static final Stream<Arguments> quxNumbersJunit() {",
            "    return Stream.of(arguments(\"1\", 1), arguments(\"2\", 2));",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement1DArray() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import org.testng.annotations.DataProvider;",
            "",
            "class A {",
            "  @DataProvider",
            "  private Object[] numbers() {",
            "    int[] values = new int[] {1, 2};",
            "    return new Object[] {",
            "      // first",
            "      values[0],",
            "      // second",
            "      values[1]",
            "    };",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "",
            "import java.util.stream.Stream;",
            "import org.junit.jupiter.params.provider.Arguments;",
            "import org.testng.annotations.DataProvider;",
            "",
            "class A {",
            "  @DataProvider",
            "  private Object[] numbers() {",
            "    int[] values = new int[] {1, 2};",
            "    return new Object[] {",
            "      // first",
            "      values[0],",
            "      // second",
            "      values[1]",
            "    };",
            "  }",
            "",
            "  @SuppressWarnings(\"UnusedMethod\" /* This is an intermediate state for the JUnit migration. */)",
            "  private static Stream<Arguments> numbersJunit() {",
            "    int[] values = new int[] {1, 2};",
            "    return Stream.of(",
            "        // first",
            "        arguments(values[0]),",
            "        // second",
            "        arguments(values[1]));",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacement1DArray2DReturnType() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import org.testng.annotations.DataProvider;",
            "",
            "class A {",
            "  @DataProvider",
            "  private Object[] numbers() {",
            "    int[] values = new int[] {1, 2};",
            "    return new Object[][] {",
            "      {String.valueOf(values[0]), values[0]},",
            "      {String.valueOf(values[1]), values[1]}",
            "    };",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "",
            "import java.util.stream.Stream;",
            "import org.junit.jupiter.params.provider.Arguments;",
            "import org.testng.annotations.DataProvider;",
            "",
            "class A {",
            "  @DataProvider",
            "  private Object[] numbers() {",
            "    int[] values = new int[] {1, 2};",
            "    return new Object[][] {",
            "      {String.valueOf(values[0]), values[0]},",
            "      {String.valueOf(values[1]), values[1]}",
            "    };",
            "  }",
            "",
            "  @SuppressWarnings(\"UnusedMethod\" /* This is an intermediate state for the JUnit migration. */)",
            "  private static Stream<Arguments> numbersJunit() {",
            "    int[] values = new int[] {1, 2};",
            "    return Stream.of(",
            "        arguments(String.valueOf(values[0]), values[0]),",
            "        arguments(String.valueOf(values[1]), values[1]));",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacement2DArray() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import org.testng.annotations.DataProvider;",
            "",
            "class A {",
            "  @DataProvider",
            "  private Object[][] numbers() {",
            "    int[] values = new int[] {1, 2};",
            "    return new Object[][] {",
            "      {\"1\", /* comment */ values[0]},",
            "      {\"2\", values[1]}",
            "    };",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "",
            "import java.util.stream.Stream;",
            "import org.junit.jupiter.params.provider.Arguments;",
            "import org.testng.annotations.DataProvider;",
            "",
            "class A {",
            "  @DataProvider",
            "  private Object[][] numbers() {",
            "    int[] values = new int[] {1, 2};",
            "    return new Object[][] {",
            "      {\"1\", /* comment */ values[0]},",
            "      {\"2\", values[1]}",
            "    };",
            "  }",
            "",
            "  @SuppressWarnings(\"UnusedMethod\" /* This is an intermediate state for the JUnit migration. */)",
            "  private static Stream<Arguments> numbersJunit() {",
            "    int[] values = new int[] {1, 2};",
            "    return Stream.of(arguments(\"1\", /* comment */ values[0]), arguments(\"2\", values[1]));",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementBody() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import org.testng.annotations.DataProvider;",
            "",
            "class A {",
            "  @DataProvider",
            "  private Object[][] numbers() {",
            "    // create value array",
            "    int[] values = new int[2];",
            "    values[0] = 1;",
            "",
            "    // floating comment",
            "",
            "    /* multi line comment */",
            "    values[1] = 2;",
            "    return new Object[][] {",
            "      // first",
            "      {\"1\", /* second */ values[0]},",
            "      // third",
            "      {",
            "        /* fourth */",
            "        \"2\", values[1]",
            "      }",
            "    };",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "",
            "import java.util.stream.Stream;",
            "import org.junit.jupiter.params.provider.Arguments;",
            "import org.testng.annotations.DataProvider;",
            "",
            "class A {",
            "  @DataProvider",
            "  private Object[][] numbers() {",
            "    // create value array",
            "    int[] values = new int[2];",
            "    values[0] = 1;",
            "",
            "    // floating comment",
            "",
            "    /* multi line comment */",
            "    values[1] = 2;",
            "    return new Object[][] {",
            "      // first",
            "      {\"1\", /* second */ values[0]},",
            "      // third",
            "      {",
            "        /* fourth */",
            "        \"2\", values[1]",
            "      }",
            "    };",
            "  }",
            "",
            "  @SuppressWarnings(\"UnusedMethod\" /* This is an intermediate state for the JUnit migration. */)",
            "  private static Stream<Arguments> numbersJunit() {",
            "    // create value array",
            "    int[] values = new int[2];",
            "    values[0] = 1;",
            "",
            "    // floating comment",
            "",
            "    /* multi line comment */",
            "    values[1] = 2;",
            "    return Stream.of(",
            "        // first",
            "        arguments(\"1\", /* second */ values[0]),",
            "        // third",
            "        arguments(",
            "            /* fourth */",
            "            \"2\", values[1]));",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementThrows() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import java.io.IOException;",
            "import org.testng.annotations.DataProvider;",
            "",
            "class A {",
            "  @DataProvider",
            "  private Object[][] numbers() throws InterruptedException, IOException {",
            "    // create value array",
            "    int[] values = new int[] {1, 2};",
            "    return new Object[][] {",
            "      {\"1\", values[0]},",
            "      {\"2\", values[1]}",
            "    };",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "",
            "import java.io.IOException;",
            "import java.util.stream.Stream;",
            "import org.junit.jupiter.params.provider.Arguments;",
            "import org.testng.annotations.DataProvider;",
            "",
            "class A {",
            "  @DataProvider",
            "  private Object[][] numbers() throws InterruptedException, IOException {",
            "    // create value array",
            "    int[] values = new int[] {1, 2};",
            "    return new Object[][] {",
            "      {\"1\", values[0]},",
            "      {\"2\", values[1]}",
            "    };",
            "  }",
            "",
            "  @SuppressWarnings(\"UnusedMethod\" /* This is an intermediate state for the JUnit migration. */)",
            "  private static Stream<Arguments> numbersJunit() throws InterruptedException, IOException {",
            "    // create value array",
            "    int[] values = new int[] {1, 2};",
            "    return Stream.of(arguments(\"1\", values[0]), arguments(\"2\", values[1]));",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementGetClass() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import org.testng.annotations.DataProvider;",
            "",
            "class A {",
            "",
            "  @DataProvider",
            "  private Object[][] numbers() {",
            "    return new Object[][] {",
            "      {getClass().getSimpleName(), 1},",
            "      {this.getClass().getSimpleName(), 2}",
            "    };",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "",
            "import java.util.stream.Stream;",
            "import org.junit.jupiter.params.provider.Arguments;",
            "import org.testng.annotations.DataProvider;",
            "",
            "class A {",
            "",
            "  @DataProvider",
            "  private Object[][] numbers() {",
            "    return new Object[][] {",
            "      {getClass().getSimpleName(), 1},",
            "      {this.getClass().getSimpleName(), 2}",
            "    };",
            "  }",
            "",
            "  @SuppressWarnings(\"UnusedMethod\" /* This is an intermediate state for the JUnit migration. */)",
            "  private static Stream<Arguments> numbersJunit() {",
            "    return Stream.of(arguments(A.class.getSimpleName(), 1), arguments(A.class.getSimpleName(), 2));",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
