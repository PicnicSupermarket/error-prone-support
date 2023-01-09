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
  }

  @Test
  void badMembersDontClashWithStaticImportCandidates() {
    assertThat(BadStaticImport.BAD_STATIC_IMPORT_CANDIDATE_MEMBERS.entries())
        .doesNotContainAnyElementsOf(StaticImport.STATIC_IMPORT_CANDIDATE_MEMBERS.entries());
  }

  @Test
  void badIdentifiersDontClashWithStaticImportCandidates() {
    assertThat(BadStaticImport.BAD_STATIC_IMPORT_CANDIDATE_IDENTIFIERS)
        .doesNotContainAnyElementsOf(StaticImport.STATIC_IMPORT_CANDIDATE_MEMBERS.values());
  }

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "pkg/B.java",
            "package pkg;",
            "",
            "public class B {",
            "  public int MIN = 1;",
            "",
            "  public static class INSTANCE {}",
            "}")
        .addSourceLines(
            "A.java",
            "import static com.google.common.collect.ImmutableList.copyOf;",
            "import static java.lang.Integer.MIN_VALUE;",
            "import static java.time.Clock.systemUTC;",
            "import static java.time.Instant.MAX;",
            "import static java.time.Instant.MIN;",
            "import static java.time.ZoneOffset.UTC;",
            "import static java.util.Collections.min;",
            "import static java.util.Locale.ENGLISH;",
            "import static java.util.Locale.ROOT;",
            "import static java.util.Optional.empty;",
            "",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.time.Clock;",
            "import java.util.Locale;",
            "import pkg.B;",
            "import pkg.B.INSTANCE;",
            "",
            "class A {",
            "  private Integer MIN_VALUE = 12;",
            "",
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
            "",
            "    // BUG: Diagnostic contains:",
            "    Locale lo1 = ROOT;",
            "    Locale lo2 = Locale.ROOT;",
            "",
            "    // BUG: Diagnostic contains:",
            "    Object c1 = MIN;",
            "    // BUG: Diagnostic contains:",
            "    Object c2 = MAX;",
            "",
            "    // BUG: Diagnostic contains:",
            "    ImmutableSet.of(min(ImmutableSet.of()));",
            "",
            "    Object builder = null;",
            "    // Not flagged because identifier is variable",
            "    Object lBuilder = ImmutableList.of(builder);",
            "",
            "    // Not flagged because member of type is not a candidate",
            "    Locale lo3 = ENGLISH;",
            "",
            "    // Not flagged because member of type is exempted",
            "    Object utc = UTC;",
            "",
            "    // Not flagged because method is not statically imported",
            "    create();",
            "",
            "    // Not flagged because identifier is not statically imported",
            "    // A member variable did overwrite the statically imported identifier",
            "    Integer x1 = MIN_VALUE;",
            "",
            "    // Not flagged because identifier is not statically imported",
            "    Integer x2 = new B().MIN;",
            "",
            "    // Not flagged because identifier is not statically imported",
            "    Object inst = new INSTANCE();",
            "  }",
            "",
            "  void create() {}",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import static com.google.common.collect.ImmutableList.copyOf;",
            "import static java.time.Clock.systemUTC;",
            "import static java.time.Instant.MAX;",
            "import static java.time.Instant.MIN;",
            "import static java.util.Collections.min;",
            "import static java.util.Locale.ROOT;",
            "import static java.util.Optional.empty;",
            "",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.time.Clock;",
            "import java.util.Locale;",
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
            "",
            "    Locale lo1 = ROOT;",
            "    Locale lo2 = Locale.ROOT;",
            "",
            "    Object c1 = MIN;",
            "    Object c2 = MAX;",
            "",
            "    ImmutableSet.of(min(ImmutableSet.of()));",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.time.Clock;",
            "import java.time.Instant;",
            "import java.util.Collections;",
            "import java.util.Locale;",
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
            "",
            "    Locale lo1 = Locale.ROOT;",
            "    Locale lo2 = Locale.ROOT;",
            "",
            "    Object c1 = Instant.MIN;",
            "    Object c2 = Instant.MAX;",
            "",
            "    ImmutableSet.of(Collections.min(ImmutableSet.of()));",
            "  }",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }
}
