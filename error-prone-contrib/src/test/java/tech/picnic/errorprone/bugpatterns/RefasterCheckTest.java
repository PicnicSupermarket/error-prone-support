package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import java.io.IOException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class RefasterCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(RefasterCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(new RefasterCheck(), getClass());

  @Test
  @Ignore(
      "`RefasterCheck` itself doesn't 'directly' report issues, and that seems to confuse the `CompilationTestHelper` here")
  public void testIdentification() {
    compilationTestHelper
        .addSourceLines(
            "pkg/A.java",
            "import java.time.ZoneId;",
            "import java.time.ZoneOffset;",
            "",
            "class A {",
            "  void m() {",
            "    ZoneId z1 = ZoneOffset.UTC;",
            "    // BUG: Diagnostic contains:",
            "    ZoneId z2 = ZoneId.of(\"UTC\");",
            "  }",
            "}")
        .doTest();
  }

  /**
   * Verifies that the Refaster templates shipped with this module are picked up and enforced.
   *
   * <p>Rules used:
   *
   * <ul>
   *   <li>{@code tech.picnic.errorprone.refastertemplates.OptionalIsEmpty}
   *   <li>{@code tech.picnic.errorprone.refastertemplates.UtcConstant}
   * </ul>
   */
  @Test
  public void testReplacement() throws IOException {
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "import java.time.ZoneId;",
            "import java.util.Optional;",
            "",
            "class A {",
            "  void m() {",
            "    boolean b = !Optional.empty().isPresent();",
            "    ZoneId z = ZoneId.of(\"UTC\");",
            "  }",
            "}")
        .addOutputLines(
            "out/A.java",
            "import java.time.ZoneId;",
            "import java.time.ZoneOffset;",
            "import java.util.Optional;",
            "",
            "class A {",
            "  void m() {",
            "    boolean b = Optional.empty().isEmpty();",
            "    ZoneId z = ZoneOffset.UTC;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
