package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class ImplicitVarUsageTest {
  @Test
  void testIdentification() {
    CompilationTestHelper.newInstance(ImplicitVarUsage.class, getClass())
        .addSourceLines(
            "Test.java",
            "import java.util.List;",
            "",
            "class Test {",
            "  void test() {",
            "    // BUG: Diagnostic contains:",
            "    var methodCall = getList();",
            "    // BUG: Diagnostic contains:",
            "    var chainedMethodCall = getList().stream().filter(s -> s.length() > 2).toList();",
            "",
            "    // Should NOT be flagged - string literal",
            "    var stringLiteral = \"John Doe\";",
            "    // Should NOT be flagged - int literal",
            "    var intLiteral = 42;",
            "    // Should NOT be flagged - type cast",
            "    var typeCast = (List) getList();",
            "    // Should NOT be flagged - new operation",
            "    var newOperation = new Object();",
            "    // Should NOT be flagged - .class literal",
            "    var classLiteral = getForType(List.class);",
            "    // Should NOT be flagged - constructor reference",
            "    var constructorReference = getList().stream().map(String::new).findFirst();",
            "  }",
            "",
            "  private List<String> getList() {",
            "    return null;",
            "  }",
            "",
            "  private <T> T getForType(Class<T> clazz) {",
            "    return null;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void testRefactoring() {
    BugCheckerRefactoringTestHelper.newInstance(ImplicitVarUsage.class, getClass())
        .addInputLines(
            "Test.java",
            "import java.util.List;",
            "",
            "class Test {",
            "  void test() {",
            "    var methodCall = getList();",
            "    var chainedMethodCall = getList().stream().filter(s -> s.length() > 2).toList();",
            "  }",
            "",
            "  private List<String> getList() {",
            "    return null;",
            "  }",
            "}")
        .addOutputLines(
            "Test.java",
            "import java.util.List;",
            "",
            "class Test {",
            "  void test() {",
            "    List<String> methodCall = getList();",
            "    List<String> chainedMethodCall = getList().stream().filter(s -> s.length() > 2).toList();",
            "  }",
            "",
            "  private List<String> getList() {",
            "    return null;",
            "  }",
            "}")
        .doTest();
  }
}
