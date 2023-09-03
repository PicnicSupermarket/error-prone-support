package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import org.junit.jupiter.api.Test;

final class Slf4jLogDeclarationTest {
  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(Slf4jLogDeclaration.class, getClass())
        .addInputLines(
            "A.java",
            "import org.slf4j.Logger;",
            "import org.slf4j.LoggerFactory;",
            "",
            "class A {",
            "  Logger LOG1 = LoggerFactory.getLogger(A.class);",
            "",
            "  private Logger LOG2 = LoggerFactory.getLogger(A.class);",
            "",
            "  private static Logger LOG3 = LoggerFactory.getLogger(A.class);",
            "",
            "  static final Logger LOG4 = LoggerFactory.getLogger(A.class);",
            "",
            "  private final Logger LOG5 = LoggerFactory.getLogger(A.class);",
            "",
            "  private static final Logger LOG6 = LoggerFactory.getLogger(A.class);",
            "",
            "  private static final Logger NOT_PROPER_LOGGER_NAME = LoggerFactory.getLogger(A.class);",
            "",
            "  private static final Logger LOGGER_WITH_WRONG_CLASS_AS_ARGUMENT =",
            "      LoggerFactory.getLogger(B.class);",
            "",
            "  class B {}",
            "}")
        .addOutputLines(
            "A.java",
            "import org.slf4j.Logger;",
            "import org.slf4j.LoggerFactory;",
            "",
            "class A {",
            "  private static final Logger LOG1 = LoggerFactory.getLogger(A.class);",
            "",
            "  private static final Logger LOG2 = LoggerFactory.getLogger(A.class);",
            "",
            "  private static final Logger LOG3 = LoggerFactory.getLogger(A.class);",
            "",
            "  private static final Logger LOG4 = LoggerFactory.getLogger(A.class);",
            "",
            "  private static final Logger LOG5 = LoggerFactory.getLogger(A.class);",
            "",
            "  private static final Logger LOG6 = LoggerFactory.getLogger(A.class);",
            "",
            "  private static final Logger LOG = LoggerFactory.getLogger(A.class);",
            "",
            "  private static final Logger LOGGER_WITH_WRONG_CLASS_AS_ARGUMENT =",
            "      LoggerFactory.getLogger(A.class);",
            "",
            "  class B {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void doNotAddModifiersToDeclarationsInsideInterfaces() {
    BugCheckerRefactoringTestHelper.newInstance(Slf4jLogDeclaration.class, getClass())
        .addInputLines(
            "A.java",
            "import org.slf4j.Logger;",
            "import org.slf4j.LoggerFactory;",
            "",
            "interface A {",
            "  Logger LOG = LoggerFactory.getLogger(A.class);",
            "",
            "  Logger LOG1 = LoggerFactory.getLogger(B.class);",
            "",
            "  class B {}",
            "}")
        .addOutputLines(
            "A.java",
            "import org.slf4j.Logger;",
            "import org.slf4j.LoggerFactory;",
            "",
            "interface A {",
            "  Logger LOG = LoggerFactory.getLogger(A.class);",
            "",
            "  Logger LOG1 = LoggerFactory.getLogger(A.class);",
            "",
            "  class B {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
