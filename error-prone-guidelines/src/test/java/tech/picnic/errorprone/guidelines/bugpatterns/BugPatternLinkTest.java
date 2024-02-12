package tech.picnic.errorprone.guidelines.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class BugPatternLinkTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(BugPatternLink.class, getClass())
        .addSourceLines(
            "A.java",
            "import com.google.errorprone.BugPattern;",
            "",
            "@BugPattern(summary = \"Class in default package\", severity = BugPattern.SeverityLevel.ERROR)",
            "class A {}")
        .addSourceLines(
            "com/example/B.java",
            "package com.example;",
            "",
            "import com.google.errorprone.BugPattern;",
            "",
            "@BugPattern(summary = \"Class in custom package\", severity = BugPattern.SeverityLevel.ERROR)",
            "class B {}")
        .addSourceLines(
            "tech/picnic/errorprone/C.java",
            "package tech.picnic.errorprone;",
            "",
            "import com.google.errorprone.BugPattern;",
            "",
            "@BugPattern(",
            "    summary = \"Class explicitly without link\",",
            "    linkType = BugPattern.LinkType.NONE,",
            "    severity = BugPattern.SeverityLevel.ERROR)",
            "class C {}")
        .addSourceLines(
            "tech/picnic/errorprone/subpackage/D.java",
            "package tech.picnic.errorprone.subpackage;",
            "",
            "import com.google.errorprone.BugPattern;",
            "import tech.picnic.errorprone.utils.Documentation;",
            "",
            "@BugPattern(",
            "    summary = \"Error Prone Support class in subpackage with proper link\",",
            "    link = Documentation.BUG_PATTERNS_BASE_URL + \"D\",",
            "    linkType = BugPattern.LinkType.CUSTOM,",
            "    severity = BugPattern.SeverityLevel.ERROR)",
            "class D {}")
        .addSourceLines(
            "tech/picnic/errorprone/E.java",
            "package tech.picnic.errorprone;",
            "",
            "import static com.google.errorprone.BugPattern.LinkType.CUSTOM;",
            "import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;",
            "import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;",
            "",
            "import com.google.errorprone.BugPattern;",
            "",
            "@BugPattern(",
            "    summary = \"Error Prone Support class with proper link and static imports\",",
            "    link = BUG_PATTERNS_BASE_URL + \"E\",",
            "    linkType = CUSTOM,",
            "    severity = ERROR)",
            "class E {}")
        .addSourceLines(
            "tech/picnic/errorprone/F.java",
            "package tech.picnic.errorprone;",
            "",
            "import com.google.errorprone.BugPattern;",
            "",
            "class F {",
            "  @BugPattern(",
            "      summary = \"Nested Error Prone Support class\",",
            "      severity = BugPattern.SeverityLevel.ERROR)",
            "  class Inner {}",
            "}")
        .addSourceLines(
            "tech/picnic/errorprone/G.java",
            "package tech.picnic.errorprone;",
            "",
            "import com.google.errorprone.BugPattern;",
            "",
            "// BUG: Diagnostic contains:",
            "@BugPattern(",
            "    summary = \"Error Prone Support class lacking link\",",
            "    severity = BugPattern.SeverityLevel.ERROR)",
            "class G {}")
        .addSourceLines(
            "tech/picnic/errorprone/H.java",
            "package tech.picnic.errorprone;",
            "",
            "import com.google.errorprone.BugPattern;",
            "import tech.picnic.errorprone.utils.Documentation;",
            "",
            "// BUG: Diagnostic contains:",
            "@BugPattern(",
            "    summary = \"Error Prone Support class with incorrect link\",",
            "    link = Documentation.BUG_PATTERNS_BASE_URL + \"NotH\",",
            "    linkType = BugPattern.LinkType.CUSTOM,",
            "    severity = BugPattern.SeverityLevel.ERROR)",
            "class H {}")
        .addSourceLines(
            "tech/picnic/errorprone/I.java",
            "package tech.picnic.errorprone;",
            "",
            "import com.google.errorprone.BugPattern;",
            "",
            "// BUG: Diagnostic contains:",
            "@BugPattern(",
            "    summary = \"Error Prone Support class with non-canonical link\",",
            "    link = \"https://error-prone.picnic.tech/bugpatterns/I\",",
            "    linkType = BugPattern.LinkType.CUSTOM,",
            "    severity = BugPattern.SeverityLevel.ERROR)",
            "class I {}")
        .addSourceLines(
            "tech/picnic/errorprone/J.java",
            "package tech.picnic.errorprone;",
            "",
            "import com.google.errorprone.BugPattern;",
            "",
            "// BUG: Diagnostic contains:",
            "@BugPattern(",
            "    summary = \"Error Prone Support class in with non-canonical link\",",
            "    link = \"https://error-prone.picnic.tech/bugpatterns/\" + \"J\",",
            "    linkType = BugPattern.LinkType.CUSTOM,",
            "    severity = BugPattern.SeverityLevel.ERROR)",
            "class J {}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(BugPatternLink.class, getClass())
        .addInputLines(
            "tech/picnic/errorprone/A.java",
            "package tech.picnic.errorprone;",
            "",
            "import com.google.errorprone.BugPattern;",
            "",
            "@BugPattern(",
            "    summary = \"Error Prone Support class lacking link\",",
            "    severity = BugPattern.SeverityLevel.ERROR)",
            "class A {}")
        .addOutputLines(
            "tech/picnic/errorprone/A.java",
            "package tech.picnic.errorprone;",
            "",
            "import static com.google.errorprone.BugPattern.LinkType.CUSTOM;",
            "import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;",
            "",
            "import com.google.errorprone.BugPattern;",
            "",
            "@BugPattern(",
            "    link = BUG_PATTERNS_BASE_URL + \"A\",",
            "    linkType = CUSTOM,",
            "    summary = \"Error Prone Support class lacking link\",",
            "    severity = BugPattern.SeverityLevel.ERROR)",
            "class A {}")
        .addInputLines(
            "tech/picnic/errorprone/B.java",
            "package tech.picnic.errorprone;",
            "",
            "import static com.google.errorprone.BugPattern.LinkType.CUSTOM;",
            "import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;",
            "",
            "import com.google.errorprone.BugPattern;",
            "",
            "@BugPattern(",
            "    summary = \"Error Prone Support class with incorrect link\",",
            "    link = \"Not the right link\",",
            "    linkType = CUSTOM,",
            "    severity = ERROR)",
            "class B {}")
        .addOutputLines(
            "tech/picnic/errorprone/B.java",
            "package tech.picnic.errorprone;",
            "",
            "import static com.google.errorprone.BugPattern.LinkType.CUSTOM;",
            "import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;",
            "import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;",
            "",
            "import com.google.errorprone.BugPattern;",
            "",
            "@BugPattern(",
            "    summary = \"Error Prone Support class with incorrect link\",",
            "    link = BUG_PATTERNS_BASE_URL + \"B\",",
            "    linkType = CUSTOM,",
            "    severity = ERROR)",
            "class B {}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
