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
            // XXX: This one should not be here...
            "// BUG: Diagnostic contains:",
            "class A {",
            "  private static final Logger LOG = LoggerFactory.getLogger(A.class);",
            "",
            "  // BUG: Diagnostic contains:",
            "  static class VariableMissingStaticFinal {",
            "    private Logger LOG = LoggerFactory.getLogger(VariableMissingStaticFinal.class);",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  static class VariableMissingFinal {",
            "    private static Logger LOG = LoggerFactory.getLogger(VariableMissingFinal.class);",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  static class VariableMissingPrivate {",
            "    static final Logger LOG = LoggerFactory.getLogger(VariableMissingPrivate.class);",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  static class VariableMissingStatic {",
            "    private final Logger LOG = LoggerFactory.getLogger(VariableMissingStatic.class);",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  static class WrongVariableName {",
            "    private static final Logger GRAPLY = LoggerFactory.getLogger(WrongVariableName.class);",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  static class WrongArgumentGetLogger {",
            "    private static final Logger LOG = LoggerFactory.getLogger(String.class);",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  interface K {",
            "    Logger FOO = LoggerFactory.getLogger(A.class);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacementWithDefaultCanonicalLoggerName() {
    BugCheckerRefactoringTestHelper.newInstance(Slf4jLogDeclaration.class, getClass())
        .addInputLines(
            "A.java",
            "import org.slf4j.Logger;",
            "import org.slf4j.LoggerFactory;",
            "",
            "class A {",
            "  Logger FOO = LoggerFactory.getLogger(A.class);",
            "",
            "  static class WrongArgumentGetLogger {",
            "    private static final Logger LOG = LoggerFactory.getLogger(String.class);",
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
            "  static class WrongArgumentGetLogger {",
            "    private static final Logger LOG = LoggerFactory.getLogger(WrongArgumentGetLogger.class);",
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
            "  Logger FOO = LoggerFactory.getLogger(A.class);",
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
