package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class CanonicalClassNameUsageTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(CanonicalClassNameUsage.class, getClass())
        .setArgs(
            "--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
            "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED")
        .addSourceLines(
            "A.java",
            "import static com.google.errorprone.matchers.Matchers.instanceMethod;",
            "",
            "import com.google.errorprone.VisitorState;",
            "import tech.picnic.errorprone.utils.MoreTypes;",
            "",
            "class A {",
            "  void m(VisitorState state) {",
            "    String a = A.class.getName();",
            "    String b = getClass().getName();",
            "    A.class.getName().toString();",
            "    System.out.println(A.class.getName());",
            "    methodInUnnamedPackage(A.class.getName());",
            "    instanceMethod().onExactClass(A.class.getCanonicalName());",
            "    MoreTypes.type(A.class.getCanonicalName());",
            "    MoreTypes.type(A.class.getCanonicalName() + \".SubType\");",
            "    instanceMethod().onExactClass(new Object() {}.getClass().getName());",
            "    instanceMethod().onExactClass(methodInUnnamedPackage(A.class.getName()));",
            "    // BUG: Diagnostic contains:",
            "    instanceMethod().onExactClass(A.class.getName());",
            "    // BUG: Diagnostic contains:",
            "    MoreTypes.type(A.class.getName());",
            "    // BUG: Diagnostic contains:",
            "    state.binaryNameFromClassname(A.class.getName() + \".SubType\");",
            "  }",
            "",
            "  String methodInUnnamedPackage(String str) {",
            "    return str;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(CanonicalClassNameUsage.class, getClass())
        .addInputLines(
            "A.java",
            "import static com.google.errorprone.matchers.Matchers.instanceMethod;",
            "",
            "import com.google.errorprone.BugPattern;",
            "import tech.picnic.errorprone.utils.MoreTypes;",
            "",
            "class A {",
            "  void m() {",
            "    instanceMethod().onDescendantOfAny(A.class.getName(), BugPattern.LinkType.class.getName());",
            "    MoreTypes.type(String.class.getName());",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static com.google.errorprone.matchers.Matchers.instanceMethod;",
            "",
            "import com.google.errorprone.BugPattern;",
            "import tech.picnic.errorprone.utils.MoreTypes;",
            "",
            "class A {",
            "  void m() {",
            "    instanceMethod()",
            "        .onDescendantOfAny(",
            "            A.class.getCanonicalName(), BugPattern.LinkType.class.getCanonicalName());",
            "    MoreTypes.type(String.class.getCanonicalName());",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
