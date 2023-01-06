package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class BadStaticImportTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(BadStaticImport.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(BadStaticImport.class, getClass());

  @Test
  void candidateMembersAreNotRedundant() {
    assertThat(BadStaticImport.BAD_STATIC_IMPORT_CANDIDATE_MEMBERS.keySet())
        .doesNotContainAnyElementsOf(BadStaticImport.BAD_STATIC_IMPORT_CANDIDATE_TYPES);

    assertThat(BadStaticImport.BAD_STATIC_IMPORT_CANDIDATE_MEMBERS.values())
        .doesNotContainAnyElementsOf(BadStaticImport.BAD_STATIC_IMPORT_CANDIDATE_IDENTIFIERS);
  }

  @Test
  void badTypesDontClashWithStaticImportCandidates() {
    assertThat(BadStaticImport.BAD_STATIC_IMPORT_CANDIDATE_TYPES)
        .doesNotContainAnyElementsOf(StaticImport.STATIC_IMPORT_CANDIDATE_TYPES);

    assertThat(BadStaticImport.BAD_STATIC_IMPORT_CANDIDATE_TYPES)
        .doesNotContainAnyElementsOf(StaticImport.STATIC_IMPORT_CANDIDATE_MEMBERS.keySet());
  }

  @Test
  void badMembersDontClashWithStaticImportCandidates() {
    assertThat(BadStaticImport.BAD_STATIC_IMPORT_CANDIDATE_MEMBERS.keySet())
        .doesNotContainAnyElementsOf(StaticImport.STATIC_IMPORT_CANDIDATE_TYPES);

    assertThat(BadStaticImport.BAD_STATIC_IMPORT_CANDIDATE_MEMBERS.values())
        .doesNotContainAnyElementsOf(StaticImport.STATIC_IMPORT_EXEMPTED_IDENTIFIERS);
  }

  @Test
  void badIdentifiersDontClashWithStaticImportCandidates() {
    assertThat(BadStaticImport.BAD_STATIC_IMPORT_CANDIDATE_IDENTIFIERS)
        .doesNotContainAnyElementsOf(StaticImport.STATIC_IMPORT_CANDIDATE_MEMBERS.values());
  }

  @Test
  void identifySimpleMethodInvocation() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static java.time.Clock.systemUTC;",
            "import static java.util.Optional.empty;",
            "import static com.google.common.collect.ImmutableList.copyOf;",
            "import static com.google.common.collect.ImmutableMap.of;",
            "import java.time.Clock;",
            "import com.google.common.collect.ImmutableList;",

            "",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic contains:",
            "    systemUTC();",
            "    Clock.systemUTC();",
            "",
            "    // BUG: Diagnostic contains:",
            "    Object o1 = empty();",
            "",
            "    // BUG: Diagnostic contains:",
            "    Object l1 = copyOf(ImmutableList.of());",
            "    Object l2 = ImmutableList.copyOf(ImmutableList.of());",
            "  }",
            "}")
        .doTest();
  }

  // XXX: Add more counterexamples
  @Test
  void replaceSimpleMethodInvocation() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import static java.time.Clock.systemUTC;",
            "import static java.util.Optional.empty;",
            "import static com.google.common.collect.ImmutableList.copyOf;",
            "import com.google.common.collect.ImmutableList;",
            "import java.time.Clock;",
            "",
            "class A {",
            "  void m() {",
            "    systemUTC();",
            "    Clock.systemUTC();",
            "",
            "    Object o1 = empty();",
            "",
            "    Object l1 = copyOf(ImmutableList.of());",
            "    Object l2 = ImmutableList.copyOf(ImmutableList.of());",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "import java.time.Clock;",
            "import java.util.Optional;",
            "",
            "class A {",
            "  void m() {",
            "    Clock.systemUTC();",
            "    Clock.systemUTC();",
            "",
            "    Object o1 = Optional.empty();",
            "",
            "    Object l1 = ImmutableList.copyOf(ImmutableList.of());",
            "    Object l2 = ImmutableList.copyOf(ImmutableList.of());",
            "  }",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }

  @Test
  void identifySimpleFieldAccess() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static java.time.Instant.MAX;",
            "import static java.time.Instant.MIN;",
            "import static java.util.Locale.ROOT;",
            "import java.util.Locale;",
            "",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic contains:",
            "    Locale l1 = ROOT;",
            "    Locale l2 = Locale.ROOT;",
            "",
            "    // BUG: Diagnostic contains:",
            "    Object c1 = MIN;",
            "    // BUG: Diagnostic contains:",
            "    Object c2 = MAX;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replaceSimpleFieldAccess() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import static java.time.Instant.MAX;",
            "import static java.time.Instant.MIN;",
            "import static java.util.Locale.ROOT;",
            "import java.util.Locale;",
            "",
            "class A {",
            "  void m() {",
            "    Locale l1 = ROOT;",
            "    Locale l2 = Locale.ROOT;",
            "",
            "    Object c1 = MIN;",
            "    Object c2 = MAX;",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import java.time.Instant;",
            "import java.util.Locale;",
            "",
            "class A {",
            "  void m() {",
            "    Locale l1 = Locale.ROOT;",
            "    Locale l2 = Locale.ROOT;",
            "",
            "    Object c1 = Instant.MIN;",
            "    Object c2 = Instant.MAX;",
            "  }",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }
}