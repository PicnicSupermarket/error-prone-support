package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class Slf4jLogDeclarationTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(Slf4jLogDeclaration.class, getClass())
        .addSourceLines(
            "A.java",
            "import org.slf4j.Logger;",
            "import org.slf4j.LoggerFactory;",
            "",
            "class A {",
            "  private static final Logger LOG = LoggerFactory.getLogger(A.class);",
            "",
            "  // BUG: Diagnostic contains:",
            "  private static final Logger foo = LoggerFactory.getLogger(A.class);",
            "",
            "  static class VariableMissingStaticFinal {",
            "    // BUG: Diagnostic contains:",
            "    private Logger LOG = LoggerFactory.getLogger(VariableMissingStaticFinal.class);",
            "  }",
            "",
            "  static class VariableMissingFinal {",
            "    // BUG: Diagnostic contains:",
            "    private static Logger LOG = LoggerFactory.getLogger(VariableMissingFinal.class);",
            "  }",
            "",
            "  static class VariableMissingPrivate {",
            "    // BUG: Diagnostic contains:",
            "    static final Logger LOG = LoggerFactory.getLogger(VariableMissingPrivate.class);",
            "  }",
            "",
            "  static class VariableMissingStatic {",
            "    // BUG: Diagnostic contains:",
            "    private final Logger LOG = LoggerFactory.getLogger(VariableMissingStatic.class);",
            "  }",
            "",
            "  static class WrongVariableName {",
            "    // BUG: Diagnostic contains:",
            "    private static final Logger BAR = LoggerFactory.getLogger(WrongVariableName.class);",
            "  }",
            "",
            "  static class WrongArgumentGetLogger {",
            "    // BUG: Diagnostic contains:",
            "    private static final Logger LOG = LoggerFactory.getLogger(String.class);",
            "  }",
            "",
            "  interface InterfaceWithNoCanonicalModifiers {",
            "    Logger LOG = LoggerFactory.getLogger(InterfaceWithNoCanonicalModifiers.class);",
            "  }",
            "",
            "  interface InterfaceWithWrongVariableName {",
            "    // BUG: Diagnostic contains:",
            "    Logger BAZ = LoggerFactory.getLogger(InterfaceWithWrongVariableName.class);",
            "  }",
            "",
            "  interface WrongArgumentGetLoggerInterface {",
            "    // BUG: Diagnostic contains:",
            "    Logger LOG = LoggerFactory.getLogger(A.class);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(Slf4jLogDeclaration.class, getClass())
        .addInputLines(
            "A.java",
            "import org.slf4j.Logger;",
            "import org.slf4j.LoggerFactory;",
            "",
            "class A {",
            "  static Logger foo = LoggerFactory.getLogger(A.class);",
            "",
            "  void m() {",
            "    foo.trace(\"foo\");",
            "  }",
            "",
            "  static class NestedClass {",
            "    void m() {",
            "      foo.trace(\"foo\");",
            "    }",
            "  }",
            "",
            "  static class WrongArgumentGetLogger {",
            "    private static final Logger LOG = LoggerFactory.getLogger(String.class);",
            "  }",
            "",
            "  interface InterfaceWithDefaultMethod {",
            "    default void m() {",
            "      foo.trace(\"foo\");",
            "    }",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import org.slf4j.Logger;",
            "import org.slf4j.LoggerFactory;",
            "",
            "class A {",
            "  private static final Logger LOG = LoggerFactory.getLogger(A.class);",
            "",
            "  void m() {",
            "    LOG.trace(\"foo\");",
            "  }",
            "",
            "  static class NestedClass {",
            "    void m() {",
            "      LOG.trace(\"foo\");",
            "    }",
            "  }",
            "",
            "  static class WrongArgumentGetLogger {",
            "    private static final Logger LOG = LoggerFactory.getLogger(WrongArgumentGetLogger.class);",
            "  }",
            "",
            "  interface InterfaceWithDefaultMethod {",
            "    default void m() {",
            "      LOG.trace(\"foo\");",
            "    }",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementWithOverriddenCanonicalLoggerName() {
    BugCheckerRefactoringTestHelper.newInstance(Slf4jLogDeclaration.class, getClass())
        .setArgs(ImmutableList.of("-XepOpt:Slf4jLogDeclaration:CanonicalLoggerName=BAR"))
        .addInputLines(
            "A.java",
            "import org.slf4j.Logger;",
            "import org.slf4j.LoggerFactory;",
            "",
            "class A {",
            "  Logger LOG = LoggerFactory.getLogger(A.class);",
            "}")
        .addOutputLines(
            "A.java",
            "import org.slf4j.Logger;",
            "import org.slf4j.LoggerFactory;",
            "",
            "class A {",
            "  private static final Logger BAR = LoggerFactory.getLogger(A.class);",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
