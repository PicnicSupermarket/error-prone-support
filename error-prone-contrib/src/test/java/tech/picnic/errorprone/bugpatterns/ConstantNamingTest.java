package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class ConstantNamingTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(ConstantNaming.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  private static final long serialVersionUID = 1L;",
            "  private static final int FOO = 1;",
            "  // BUG: Diagnostic contains: consider renaming to 'BAR', though note that this is not a private",
            "  // constant",
            "  static final int bar = 2;",
            "  // BUG: Diagnostic contains:",
            "  private static final int baz = 3;",
            "  // BUG: Diagnostic contains: consider renaming to 'QUX_QUUX', though note that a variable with",
            "  // this name is already declared",
            "  private static final int qux_QUUX = 4;",
            "  // BUG: Diagnostic contains: consider renaming to 'QUUZ', though note that a variable with",
            "  // this name is already declared",
            "  private static final int quuz = 3;",
            "",
            "  private final int foo = 4;",
            "  private final Runnable QUX_QUUX =",
            "      new Runnable() {",
            "        private static final int QUUZ = 1;",
            "",
            "        @Override",
            "        public void run() {}",
            "      };",
            "}")
        .doTest();
  }

  @Test
  void identificationWithCustomExemption() {
    CompilationTestHelper.newInstance(ConstantNaming.class, getClass())
        .setArgs("-XepOpt:CanonicalConstantNaming:ExemptedNames=foo,baz")
        .addSourceLines(
            "A.java",
            "class A {",
            "  private static final long serialVersionUID = 1L;",
            "  private static final int foo = 1;",
            "  // BUG: Diagnostic contains:",
            "  private static final int bar = 2;",
            "  private static final int baz = 3;",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(ConstantNaming.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "  static final int foo = 1;",
            "  private static final int bar = 2;",
            "  private static final int baz = 3;",
            "  private static final int BAZ = 4;",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "  static final int foo = 1;",
            "  private static final int BAR = 2;",
            "  private static final int baz = 3;",
            "  private static final int BAZ = 4;",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
