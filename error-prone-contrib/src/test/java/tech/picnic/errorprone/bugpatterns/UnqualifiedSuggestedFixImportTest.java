package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class UnqualifiedSuggestedFixImportTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(UnqualifiedSuggestedFixImport.class, getClass())
        .expectErrorMessage(
            "IMPORT",
            m ->
                m.contains(
                    "Prefer `SuggestedFixes#qualifyType` over direct invocation of `SuggestedFix.Builder#addImport`"))
        .expectErrorMessage(
            "STATIC_IMPORT",
            m ->
                m.contains(
                    "Prefer `SuggestedFixes#qualifyStaticImport` over direct invocation of `SuggestedFix.Builder#addStaticImport`"))
        .addSourceLines(
            "A.java",
            "import com.google.errorprone.fixes.SuggestedFix;",
            "",
            "class A {",
            "  void m() {",
            "    System.out.println(\"foo\");",
            "    addImport(\"bar\");",
            "    addStaticImport(\"baz\");",
            "",
            "    SuggestedFix.Builder builder = SuggestedFix.builder();",
            "    // BUG: Diagnostic matches: IMPORT",
            "    builder.addImport(\"java.lang.String\");",
            "    // BUG: Diagnostic matches: STATIC_IMPORT",
            "    builder.addStaticImport(\"java.lang.String.toString\");",
            "    builder.build();",
            "  }",
            "",
            "  private void addImport(String s) {}",
            "",
            "  private void addStaticImport(String s) {}",
            "}")
        .doTest();
  }
}
