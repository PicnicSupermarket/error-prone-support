package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class NonStaticImportTest {
  @Test
  void candidateMembersAreNotRedundant() {
    assertThat(NonStaticImport.BAD_STATIC_IMPORT_CANDIDATE_MEMBERS.keySet())
        .doesNotContainAnyElementsOf(NonStaticImport.BAD_STATIC_IMPORT_CANDIDATE_TYPES);

    assertThat(NonStaticImport.BAD_STATIC_IMPORT_CANDIDATE_MEMBERS.values())
        .doesNotContainAnyElementsOf(NonStaticImport.BAD_STATIC_IMPORT_CANDIDATE_IDENTIFIERS);
  }

  @Test
  void badTypesDontClashWithStaticImportCandidates() {
    assertThat(NonStaticImport.BAD_STATIC_IMPORT_CANDIDATE_TYPES)
        .doesNotContainAnyElementsOf(StaticImport.STATIC_IMPORT_CANDIDATE_TYPES);
  }

  @Test
  void badMembersDontClashWithStaticImportCandidates() {
    assertThat(NonStaticImport.BAD_STATIC_IMPORT_CANDIDATE_MEMBERS.entries())
        .doesNotContainAnyElementsOf(StaticImport.STATIC_IMPORT_CANDIDATE_MEMBERS.entries());
  }

  @Test
  void badIdentifiersDontClashWithStaticImportCandidates() {
    assertThat(NonStaticImport.BAD_STATIC_IMPORT_CANDIDATE_IDENTIFIERS)
        .doesNotContainAnyElementsOf(StaticImport.STATIC_IMPORT_CANDIDATE_MEMBERS.values());
  }

  @Test
  void identification() {
    CompilationTestHelper.newInstance(NonStaticImport.class, getClass())
        .addSourceLines(
            "pkg/B.java",
            "package pkg;",
            "",
            "public final class B {",
            "  public int MIN = 1;",
            "",
            "  public static class INSTANCE {}",
            "}")
        .addSourceLines(
            "pkg/A.java",
            "package pkg;",
            "",
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
            "import java.time.Instant;",
            "import java.time.ZoneOffset;",
            "import java.util.Locale;",
            "import java.util.Optional;",
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
            "    Optional<Integer> optional1 = empty();",
            "    Optional<Integer> optional2 = Optional.empty();",
            "",
            "    // BUG: Diagnostic contains:",
            "    ImmutableList<Integer> list1 = copyOf(ImmutableList.of());",
            "    ImmutableList<Integer> list2 = ImmutableList.copyOf(ImmutableList.of());",
            "",
            "    // BUG: Diagnostic contains:",
            "    Locale locale1 = ROOT;",
            "    Locale locale2 = Locale.ROOT;",
            "",
            "    // BUG: Diagnostic contains:",
            "    Instant instant1 = MIN;",
            "    Instant instant2 = Instant.MIN;",
            "    // BUG: Diagnostic contains:",
            "    Instant instant3 = MAX;",
            "    Instant instant4 = Instant.MAX;",
            "",
            "    // BUG: Diagnostic contains:",
            "    ImmutableSet.of(min(ImmutableSet.of()));",
            "",
            "    Object builder = null;",
            "    // Not flagged because identifier is variable",
            "    Object lBuilder = ImmutableList.of(builder);",
            "",
            "    // Not flagged because member of type is not a candidate.",
            "    Locale lo3 = ENGLISH;",
            "",
            "    // Not flagged because member of type is exempted.",
            "    ZoneOffset utc = UTC;",
            "",
            "    // Not flagged because method is not statically imported.",
            "    create();",
            "",
            "    // Not flagged because identifier is not statically imported.",
            "    // A member variable did overwrite the statically imported identifier.",
            "    Integer i1 = MIN_VALUE;",
            "",
            "    // Not flagged because identifier is not statically imported.",
            "    Integer i2 = new B().MIN;",
            "",
            "    // Not flagged because identifier is not statically imported.",
            "    Object inst = new INSTANCE();",
            "  }",
            "",
            "  void create() {}",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(NonStaticImport.class, getClass())
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
            "import java.time.Instant;",
            "import java.util.Locale;",
            "import java.util.Optional;",
            "",
            "class A {",
            "  void m() {",
            "    systemUTC();",
            "    Clock.systemUTC();",
            "",
            "    Optional<Integer> o1 = empty();",
            "    Optional<Integer> o2 = Optional.empty();",
            "",
            "    Object l1 = copyOf(ImmutableList.of());",
            "    Object l2 = ImmutableList.copyOf(ImmutableList.of());",
            "",
            "    Locale lo1 = ROOT;",
            "    Locale lo2 = Locale.ROOT;",
            "",
            "    Instant i1 = MIN;",
            "    Instant i2 = MAX;",
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
            "    Optional<Integer> o1 = Optional.empty();",
            "    Optional<Integer> o2 = Optional.empty();",
            "",
            "    Object l1 = ImmutableList.copyOf(ImmutableList.of());",
            "    Object l2 = ImmutableList.copyOf(ImmutableList.of());",
            "",
            "    Locale lo1 = Locale.ROOT;",
            "    Locale lo2 = Locale.ROOT;",
            "",
            "    Instant i1 = Instant.MIN;",
            "    Instant i2 = Instant.MAX;",
            "",
            "    ImmutableSet.of(Collections.min(ImmutableSet.of()));",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
