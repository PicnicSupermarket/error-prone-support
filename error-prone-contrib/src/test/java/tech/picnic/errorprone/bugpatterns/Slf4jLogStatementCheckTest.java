package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class Slf4jLogStatementCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(Slf4jLogStatementCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(new Slf4jLogStatementCheck(), getClass());

  @Test
  public void testIdentification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import org.slf4j.Logger;",
            "import org.slf4j.LoggerFactory;",
            "import org.slf4j.Marker;",
            "import org.slf4j.MarkerFactory;",
            "",
            "class A {",
            "  private static final String FMT0 = \"format-string-without-placeholders\";",
            "  private static final String FMT1 = \"format-string-with-{}-placeholder\";",
            "  private static final String FMT2 = \"format-string-with-{}-{}-placeholders\";",
            "  private static final String FMT_ERR = \"format-string-with-%s-placeholder\";",
            "  private static final Logger LOG = LoggerFactory.getLogger(A.class);",
            "",
            "  private final Marker marker = MarkerFactory.getMarker(A.class.getName());",
            "  private final Object o = new Object();",
            "  private final String s = o.toString();",
            "  private final Throwable t = new Throwable();",
            "",
            "  void m() {",
            "    LOG.trace(s);",
            "    LOG.debug(s, o);",
            "    LOG.info(s, t);",
            "    LOG.warn(s, o, t);",
            "    LOG.error(marker, s);",
            "    LOG.trace(marker, s, o);",
            "    LOG.debug(marker, s, t);",
            "    LOG.info(marker, s, o, t);",
            "",
            "    LOG.warn(FMT0);",
            "    // BUG: Diagnostic contains: Log statement contains 0 placeholders, but specifies 1 matching argument(s)",
            "    LOG.error(FMT0, o);",
            "    LOG.trace(FMT0, t);",
            "    // BUG: Diagnostic contains:",
            "    LOG.debug(FMT0, o, t);",
            "    LOG.info(marker, FMT0);",
            "    // BUG: Diagnostic contains:",
            "    LOG.warn(marker, FMT0, o);",
            "    LOG.error(marker, FMT0, t);",
            "    // BUG: Diagnostic contains:",
            "    LOG.trace(marker, FMT0, o, t);",
            "",
            "    // BUG: Diagnostic contains: Log statement contains 1 placeholders, but specifies 0 matching argument(s)",
            "    LOG.debug(FMT1);",
            "    LOG.info(FMT1, o);",
            "    // BUG: Diagnostic contains:",
            "    LOG.warn(FMT1, t);",
            "    LOG.error(FMT1, o, t);",
            "    // BUG: Diagnostic contains: Log statement contains 1 placeholders, but specifies 2 matching argument(s)",
            "    LOG.trace(FMT1, o, o);",
            "    // BUG: Diagnostic contains:",
            "    LOG.debug(FMT1, o, o, t);",
            "    // BUG: Diagnostic contains:",
            "    LOG.info(marker, FMT1);",
            "    LOG.warn(marker, FMT1, o);",
            "    // BUG: Diagnostic contains:",
            "    LOG.error(marker, FMT1, t);",
            "    LOG.trace(marker, FMT1, o, t);",
            "    // BUG: Diagnostic contains:",
            "    LOG.debug(marker, FMT1, o, o);",
            "    // BUG: Diagnostic contains:",
            "    LOG.info(marker, FMT1, o, o, t);",
            "",
            "    // BUG: Diagnostic contains: SLF4J log statement placeholders are of the form `{}`, not `%s`",
            "    LOG.warn(FMT_ERR);",
            "    // BUG: Diagnostic contains:",
            "    LOG.error(FMT_ERR, t);",
            "    // BUG: Diagnostic contains:",
            "    LOG.trace(FMT_ERR, o);",
            "    // BUG: Diagnostic contains:",
            "    LOG.debug(FMT_ERR, o, t);",
            "  }",
            "}")
        .doTest();
  }

  // XXX: Drop what's unused.
  @Test
  public void testReplacement() {
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "import org.slf4j.Logger;",
            "import org.slf4j.LoggerFactory;",
            "import org.slf4j.Marker;",
            "import org.slf4j.MarkerFactory;",
            "",
            "class A {",
            "  private static final String FMT_ERR = \"format-string-with-%s-placeholder\";",
            "  private static final Logger LOG = LoggerFactory.getLogger(A.class);",
            "",
            "  private final Marker marker = MarkerFactory.getMarker(A.class.getName());",
            "  private final Object o = new Object();",
            "  private final String s = o.toString();",
            "  private final Throwable t = new Throwable();",
            "",
            "  void m() {",
            "    LOG.error(FMT_ERR, o);",
            "    LOG.error(\"format-string-with-'%s'-placeholder\", o);",
            "    LOG.error(\"format-string-with-\\\"%s\\\"-placeholder\", o);",
            "    LOG.error(\"format-string-with-%s\" + \"-placeholder\", o);",
            "  }",
            "}")
        .addOutputLines(
            "out/A.java",
            "import org.slf4j.Logger;",
            "import org.slf4j.LoggerFactory;",
            "import org.slf4j.Marker;",
            "import org.slf4j.MarkerFactory;",
            "",
            "class A {",
            "  private static final String FMT_ERR = \"format-string-with-%s-placeholder\";",
            "  private static final Logger LOG = LoggerFactory.getLogger(A.class);",
            "",
            "  private final Marker marker = MarkerFactory.getMarker(A.class.getName());",
            "  private final Object o = new Object();",
            "  private final String s = o.toString();",
            "  private final Throwable t = new Throwable();",
            "",
            "  void m() {",
            "    LOG.error(FMT_ERR, o);",
            "    LOG.error(\"format-string-with-'{}'-placeholder\", o);",
            "    LOG.error(\"format-string-with-\\\"{}\\\"-placeholder\", o);",
            "    LOG.error(\"format-string-with-{}\" + \"-placeholder\", o);",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
