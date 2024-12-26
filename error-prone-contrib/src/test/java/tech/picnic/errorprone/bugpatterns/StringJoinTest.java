package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class StringJoinTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(StringJoin.class, getClass())
        .expectErrorMessage(
            "valueOf", m -> m.contains("Prefer `String#valueOf` over `String#format`"))
        .expectErrorMessage("join", m -> m.contains("Prefer `String#join` over `String#format`"))
        .addSourceLines(
            "A.java",
            "import java.util.Formattable;",
            "import java.util.Locale;",
            "",
            "class A {",
            "  void m() {",
            "    String.join(\"-\", getClass().getName());",
            "    String.format(getClass().getName(), getClass().getName());",
            "    String.format(Locale.ROOT, \"%s\", getClass().getName());",
            "    String.format(\"%20s\", getClass().getName());",
            "    // BUG: Diagnostic matches: valueOf",
            "    String.format(\"%s\", getClass().getName());",
            "    // BUG: Diagnostic matches: valueOf",
            "    String.format(\"%s\", hashCode());",
            "    String.format(\"%s\", (Formattable) null);",
            "    String.format(\"-%s\", getClass().getName());",
            "    String.format(\"%s-\", getClass().getName());",
            "    String.format(\"-%s-\", getClass().getName());",
            "    // BUG: Diagnostic matches: join",
            "    String.format(\"%s%s\", getClass().getName(), getClass().getName());",
            "    // BUG: Diagnostic matches: join",
            "    String.format(\"%s%s\", getClass().getName(), hashCode());",
            "    // BUG: Diagnostic matches: join",
            "    String.format(\"%s%s\", hashCode(), getClass().getName());",
            "    String.format(\"%s%s\", getClass().getName(), (Formattable) null);",
            "    String.format(\"%s%s\", (Formattable) null, getClass().getName());",
            "    String.format(\"%s%s\", getClass().getName());",
            "    // BUG: Diagnostic matches: join",
            "    String.format(\"%s-%s\", getClass().getName(), getClass().getName());",
            "    // BUG: Diagnostic matches: join",
            "    String.format(\"%saa%s\", getClass().getName(), getClass().getName());",
            "    String.format(\"%s%%%s\", getClass().getName(), getClass().getName());",
            "    // BUG: Diagnostic matches: join",
            "    String.format(\"%s_%s_%s\", getClass().getName(), getClass().getName(), getClass().getName());",
            "    String.format(\"%s_%s_%s\", getClass().getName(), getClass().getName());",
            "    String.format(\"%s_%s-%s\", getClass().getName(), getClass().getName(), getClass().getName());",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(StringJoin.class, getClass())
        .addInputLines(
            "A.java",
            "class A {",
            "  void m() {",
            "    String.format(\"%s\", getClass().getName());",
            "    String.format(\"%s%s\", getClass().getName(), getClass().getName());",
            "    String.format(\"%s%s\", getClass().getName(), hashCode());",
            "    String.format(\"%s%s\", hashCode(), getClass().getName());",
            "    String.format(\"%s-%s\", getClass().getName(), getClass().getName());",
            "    String.format(\"%saa%s\", getClass().getName(), getClass().getName());",
            "    String.format(\"%s\\\"%s\", getClass().getName(), getClass().getName());",
            "    String.format(\"%s_%s_%s\", getClass().getName(), getClass().getName(), getClass().getName());",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "class A {",
            "  void m() {",
            "    String.valueOf(getClass().getName());",
            "    String.join(\"\", getClass().getName(), getClass().getName());",
            "    String.join(\"\", getClass().getName(), String.valueOf(hashCode()));",
            "    String.join(\"\", String.valueOf(hashCode()), getClass().getName());",
            "    String.join(\"-\", getClass().getName(), getClass().getName());",
            "    String.join(\"aa\", getClass().getName(), getClass().getName());",
            "    String.join(\"\\\"\", getClass().getName(), getClass().getName());",
            "    String.join(\"_\", getClass().getName(), getClass().getName(), getClass().getName());",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
