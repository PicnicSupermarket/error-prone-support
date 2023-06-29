package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class NonStaticImportTest {
  @Test
  void candidateMembersAreNotRedundant() {
    assertThat(NonStaticImport.NON_STATIC_IMPORT_CANDIDATE_MEMBERS.keySet())
        .doesNotContainAnyElementsOf(NonStaticImport.NON_STATIC_IMPORT_CANDIDATE_TYPES);

    assertThat(NonStaticImport.NON_STATIC_IMPORT_CANDIDATE_MEMBERS.values())
        .doesNotContainAnyElementsOf(NonStaticImport.NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS);
  }

  @Test
  void badTypesDontClashWithStaticImportCandidates() {
    assertThat(NonStaticImport.NON_STATIC_IMPORT_CANDIDATE_TYPES)
        .doesNotContainAnyElementsOf(StaticImport.STATIC_IMPORT_CANDIDATE_TYPES);
  }

  @Test
  void badMembersDontClashWithStaticImportCandidates() {
    assertThat(NonStaticImport.NON_STATIC_IMPORT_CANDIDATE_MEMBERS.entries())
        .doesNotContainAnyElementsOf(StaticImport.STATIC_IMPORT_CANDIDATE_MEMBERS.entries());
  }

  @Test
  void badIdentifiersDontClashWithStaticImportCandidates() {
    assertThat(NonStaticImport.NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS)
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
        .addSourceLines("MAX_VALUE.java", "", "public final class MAX_VALUE {}")
        .addSourceLines(
            "pkg/A.java",
            "package pkg;",
            "",
            "import static com.google.common.collect.ImmutableList.copyOf;",
            "import static java.lang.Integer.MAX_VALUE;",
            "import static java.lang.Integer.MIN_VALUE;",
            "import static java.time.Clock.systemUTC;",
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
            "    ZoneOffset utcIsExempted = UTC;",
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
            "    Locale isNotACandidate = ENGLISH;",
            "",
            "    // BUG: Diagnostic contains:",
            "    Instant instant1 = MIN;",
            "    Instant instant2 = Instant.MIN;",
            "",
            "    // BUG: Diagnostic contains:",
            "    ImmutableSet.of(min(ImmutableSet.of()));",
            "",
            "    Object builder = null;",
            "    ImmutableList<Object> list = ImmutableList.of(builder);",
            "",
            "    Integer refersToMemberVariable = MIN_VALUE;",
            "    Integer minIsNotStatic = new B().MIN;",
            "    Object regularImport = new INSTANCE();",
            "    MAX_VALUE maxValue = new MAX_VALUE();",
            "",
            "    Integer from = 12;",
            "    Integer i1 = from;",
            "",
            "    create();",
            "  }",
            "",
            "  void create() {",
            "    Integer MIN_VALUE = 12;",
            "    // Not flagged because identifier is not statically imported.",
            "    Integer i1 = MIN_VALUE;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(NonStaticImport.class, getClass())
        .addInputLines(
            "A.java",
            "import static com.google.common.collect.ImmutableList.copyOf;",
            "import static com.google.common.collect.ImmutableSet.of;",
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
            "    ImmutableSet.of(min(of()));",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static com.google.common.collect.ImmutableList.copyOf;",
            "import static com.google.common.collect.ImmutableSet.of;",
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
