package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class StaticImportCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(StaticImportCheck.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(StaticImportCheck.class, getClass());

  @Test
  void candidateMethodsAreNotRedundant() {
    assertThat(StaticImportCheck.STATIC_IMPORT_CANDIDATE_METHODS.keySet())
        .doesNotContainAnyElementsOf(StaticImportCheck.STATIC_IMPORT_CANDIDATE_CLASSES);
  }

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static com.google.common.collect.ImmutableMap.toImmutableMap;",
            "import static com.google.common.collect.ImmutableSet.toImmutableSet;",
            "import static java.nio.charset.StandardCharsets.UTF_8;",
            "import static java.util.function.Predicate.not;",
            "import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;",
            "",
            "import com.google.common.base.Predicates;",
            "import com.google.common.collect.ImmutableMap;",
            "import com.google.common.collect.ImmutableMultiset;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.nio.charset.StandardCharsets;",
            "import java.util.Optional;",
            "import java.util.function.Predicate;",
            "import org.springframework.boot.test.context.SpringBootTest;",
            "import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;",
            "",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic contains:",
            "    ImmutableMap.toImmutableMap(v -> v, v -> v);",
            "    ImmutableMap.<String, String, String>toImmutableMap(v -> v, v -> v);",
            "    toImmutableMap(v -> v, v -> v);",
            "",
            "    // BUG: Diagnostic contains:",
            "    ImmutableSet.toImmutableSet();",
            "    ImmutableSet.<String>toImmutableSet();",
            "    toImmutableSet();",
            "",
            "    // Not flagged because we define `#toImmutableMultiset` below.",
            "    ImmutableMultiset.toImmutableMultiset();",
            "    ImmutableMultiset.<String>toImmutableMultiset();",
            "    toImmutableMultiset();",
            "",
            "    // BUG: Diagnostic contains:",
            "    Predicate.not(null);",
            "    not(null);",
            "",
            "    // BUG: Diagnostic contains:",
            "    Predicates.alwaysTrue();",
            "    // BUG: Diagnostic contains:",
            "    Predicates.alwaysFalse();",
            "    // Not flagged because of `java.util.function.Predicate.not` import.",
            "    Predicates.not(null);",
            "",
            "    // BUG: Diagnostic contains:",
            "    Object o1 = StandardCharsets.UTF_8;",
            "    Object o2 = UTF_8;",
            "",
            "    // BUG: Diagnostic contains:",
            "    Object e1 = WebEnvironment.RANDOM_PORT;",
            "    Object e2 = RANDOM_PORT;",
            "",
            "    Optional.empty();",
            "  }",
            "",
            "  void toImmutableMultiset() {}",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "import static java.util.function.Predicate.not;",
            "",
            "import com.google.common.base.Predicates;",
            "import com.google.common.collect.ImmutableMap;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.nio.charset.StandardCharsets;",
            "import java.util.Objects;",
            "import org.junit.jupiter.params.provider.Arguments;",
            "import org.springframework.format.annotation.DateTimeFormat;",
            "import org.springframework.format.annotation.DateTimeFormat.ISO;",
            "import org.springframework.boot.test.context.SpringBootTest;",
            "import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;",
            "",
            "class A {",
            "  void m1() {",
            "    ImmutableMap.toImmutableMap(v -> v, v -> v);",
            "    ImmutableMap.<String, String, String>toImmutableMap(v -> v, v -> v);",
            "",
            "    ImmutableSet.toImmutableSet();",
            "    ImmutableSet.<String>toImmutableSet();",
            "",
            "    Predicates.not(null);",
            "    not(null);",
            "",
            "    Arguments.arguments(\"foo\");",
            "",
            "    Objects.requireNonNull(\"bar\");",
            "",
            "    Object o = StandardCharsets.UTF_8;",
            "  }",
            "",
            "  void m2(",
            "      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String date,",
            "      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) String dateTime,",
            "      @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) String time) {}",
            "",
            "  void m3(",
            "      @DateTimeFormat(iso = ISO.DATE) String date,",
            "      @DateTimeFormat(iso = ISO.DATE_TIME) String dateTime,",
            "      @DateTimeFormat(iso = ISO.TIME) String time) {}",
            "",
            "   @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)",
            "   final class Test {}",
            "}")
        .addOutputLines(
            "out/A.java",
            "import static com.google.common.collect.ImmutableMap.toImmutableMap;",
            "import static com.google.common.collect.ImmutableSet.toImmutableSet;",
            "import static java.nio.charset.StandardCharsets.UTF_8;",
            "import static java.util.Objects.requireNonNull;",
            "import static java.util.function.Predicate.not;",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;",
            "import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;",
            "import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;",
            "import static org.springframework.format.annotation.DateTimeFormat.ISO.TIME;",
            "",
            "import com.google.common.base.Predicates;",
            "import com.google.common.collect.ImmutableMap;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.nio.charset.StandardCharsets;",
            "import java.util.Objects;",
            "import org.junit.jupiter.params.provider.Arguments;",
            "import org.springframework.boot.test.context.SpringBootTest;",
            "import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;",
            "import org.springframework.format.annotation.DateTimeFormat;",
            "import org.springframework.format.annotation.DateTimeFormat.ISO;",
            "",
            "class A {",
            "  void m1() {",
            "    toImmutableMap(v -> v, v -> v);",
            "    ImmutableMap.<String, String, String>toImmutableMap(v -> v, v -> v);",
            "",
            "    toImmutableSet();",
            "    ImmutableSet.<String>toImmutableSet();",
            "",
            "    Predicates.not(null);",
            "    not(null);",
            "",
            "    arguments(\"foo\");",
            "",
            "    requireNonNull(\"bar\");",
            "",
            "    Object o = UTF_8;",
            "  }",
            "",
            "  void m2(",
            "      @DateTimeFormat(iso = DATE) String date,",
            "      @DateTimeFormat(iso = DATE_TIME) String dateTime,",
            "      @DateTimeFormat(iso = TIME) String time) {}",
            "",
            "  void m3(",
            "      @DateTimeFormat(iso = DATE) String date,",
            "      @DateTimeFormat(iso = DATE_TIME) String dateTime,",
            "      @DateTimeFormat(iso = TIME) String time) {}",
            "",
            "   @SpringBootTest(webEnvironment = RANDOM_PORT)",
            "   final class Test {}",
            "}")
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }
}
