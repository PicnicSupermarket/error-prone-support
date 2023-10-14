package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class NonStaticImportTest {
  @Test
  void candidateTypesDoNotClash() {
    assertThat(NonStaticImport.NON_STATIC_IMPORT_CANDIDATE_TYPES)
        .doesNotContainAnyElementsOf(StaticImport.STATIC_IMPORT_CANDIDATE_TYPES);
  }

  @Test
  void candidateMembersAreNotRedundant() {
    assertThat(NonStaticImport.NON_STATIC_IMPORT_CANDIDATE_MEMBERS.keySet())
        .doesNotContainAnyElementsOf(NonStaticImport.NON_STATIC_IMPORT_CANDIDATE_TYPES);

    assertThat(NonStaticImport.NON_STATIC_IMPORT_CANDIDATE_MEMBERS.values())
        .doesNotContainAnyElementsOf(NonStaticImport.NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS);
  }

  @Test
  void candidateMembersDoNotClash() {
    assertThat(NonStaticImport.NON_STATIC_IMPORT_CANDIDATE_MEMBERS.entries())
        .doesNotContainAnyElementsOf(StaticImport.STATIC_IMPORT_CANDIDATE_MEMBERS.entries());
  }

  @Test
  void candidateIdentifiersDoNotClash() {
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
            "// BUG: Diagnostic contains:",
            "import static com.google.common.collect.ImmutableList.copyOf;",
            "// BUG: Diagnostic contains:",
            "import static java.lang.Integer.MAX_VALUE;",
            "// BUG: Diagnostic contains:",
            "import static java.lang.Integer.MIN_VALUE;",
            "// BUG: Diagnostic contains:",
            "import static java.time.Clock.systemUTC;",
            "// BUG: Diagnostic contains:",
            "import static java.time.Instant.MIN;",
            "import static java.time.ZoneOffset.UTC;",
            "// BUG: Diagnostic contains:",
            "import static java.util.Collections.min;",
            "import static java.util.Locale.ENGLISH;",
            "// BUG: Diagnostic contains:",
            "import static java.util.Locale.ROOT;",
            "// BUG: Diagnostic contains:",
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
            "    systemUTC();",
            "    Clock.systemUTC();",
            "    ZoneOffset utcIsExempted = UTC;",
            "",
            "    Optional<Integer> optional1 = empty();",
            "    Optional<Integer> optional2 = Optional.empty();",
            "",
            "    ImmutableList<Integer> list1 = copyOf(ImmutableList.of());",
            "    ImmutableList<Integer> list2 = ImmutableList.copyOf(ImmutableList.of());",
            "",
            "    Locale locale1 = ROOT;",
            "    Locale locale2 = Locale.ROOT;",
            "    Locale isNotACandidate = ENGLISH;",
            "",
            "    Instant instant1 = MIN;",
            "    Instant instant2 = Instant.MIN;",
            "",
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
            "  }",
            "}")
        .doTest();
  }

  // XXX: Test multiple replacements of the same import.
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
            "",
            "  private static final class WithCustomConstant {",
            "    private static final int MIN = 42;",
            "    private static final int MAX = MIN;",
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
            "",
            "  private static final class WithCustomConstant {",
            "    private static final int MIN = 42;",
            "    private static final int MAX = MIN;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
