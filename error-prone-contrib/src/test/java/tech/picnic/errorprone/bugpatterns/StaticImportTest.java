package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class StaticImportTest {
  @Test
  void candidateTypesDoNotClash() {
    assertThat(StaticImport.STATIC_IMPORT_CANDIDATE_TYPES)
        .doesNotContainAnyElementsOf(NonStaticImport.NON_STATIC_IMPORT_CANDIDATE_TYPES);
  }

  @Test
  void candidateMembersAreNotRedundant() {
    assertThat(StaticImport.STATIC_IMPORT_CANDIDATE_MEMBERS.keySet())
        .doesNotContainAnyElementsOf(StaticImport.STATIC_IMPORT_CANDIDATE_TYPES);
  }

  @Test
  void candidateMembersDoNotClash() {
    assertThat(StaticImport.STATIC_IMPORT_CANDIDATE_MEMBERS.entries())
        .doesNotContainAnyElementsOf(NonStaticImport.NON_STATIC_IMPORT_CANDIDATE_MEMBERS.entries());

    assertThat(StaticImport.STATIC_IMPORT_CANDIDATE_MEMBERS.values())
        .doesNotContainAnyElementsOf(NonStaticImport.NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS);
  }

  @Test
  void identification() {
    CompilationTestHelper.newInstance(StaticImport.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import static com.google.common.collect.ImmutableMap.toImmutableMap;
            import static com.google.common.collect.ImmutableSet.toImmutableSet;
            import static java.nio.charset.StandardCharsets.UTF_8;
            import static java.util.function.Predicate.not;
            import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

            import com.fasterxml.jackson.annotation.JsonCreator;
            import com.google.common.base.Predicates;
            import com.google.common.collect.ImmutableMap;
            import com.google.common.collect.ImmutableMultiset;
            import com.google.common.collect.ImmutableSet;
            import com.google.errorprone.refaster.ImportPolicy;
            import com.google.errorprone.refaster.annotation.UseImportPolicy;
            import com.mongodb.client.model.Filters;
            import java.nio.charset.StandardCharsets;
            import java.time.ZoneOffset;
            import java.util.Optional;
            import java.util.UUID;
            import java.util.function.Predicate;
            import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
            import org.springframework.http.MediaType;

            class A {
              void m() {
                // BUG: Diagnostic contains:
                ImmutableMap.toImmutableMap(v -> v, v -> v);
                ImmutableMap.<String, String, String>toImmutableMap(v -> v, v -> v);
                toImmutableMap(v -> v, v -> v);

                // BUG: Diagnostic contains:
                ImmutableSet.toImmutableSet();
                ImmutableSet.<String>toImmutableSet();
                toImmutableSet();

                // Not flagged because we define `#toImmutableMultiset` below.
                ImmutableMultiset.toImmutableMultiset();
                ImmutableMultiset.<String>toImmutableMultiset();
                toImmutableMultiset();

                // BUG: Diagnostic contains:
                Predicate.not(null);
                not(null);

                // BUG: Diagnostic contains:
                Predicates.alwaysTrue();
                // BUG: Diagnostic contains:
                Predicates.alwaysFalse();
                // Not flagged because of `java.util.function.Predicate.not` import.
                Predicates.not(null);

                // BUG: Diagnostic contains:
                UUID uuid = UUID.randomUUID();

                // BUG: Diagnostic contains:
                Object o1 = StandardCharsets.UTF_8;
                Object o2 = UTF_8;

                // BUG: Diagnostic contains:
                Object e1 = WebEnvironment.RANDOM_PORT;
                Object e2 = RANDOM_PORT;

                // Not flagged because `MediaType.ALL` is exempted.
                MediaType t1 = MediaType.ALL;
                // BUG: Diagnostic contains:
                MediaType t2 = MediaType.APPLICATION_JSON;

                // BUG: Diagnostic contains:
                Filters.empty();
                Optional.empty();

                // BUG: Diagnostic contains:
                ZoneOffset zo1 = ZoneOffset.UTC;
                ZoneOffset zo2 = ZoneOffset.MIN;
              }

              // BUG: Diagnostic contains:
              @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
              private static A jsonCreator(int a) {
                return new A();
              }

              // BUG: Diagnostic contains:
              @UseImportPolicy(ImportPolicy.IMPORT_TOP_LEVEL)
              void refasterAfterTemplate() {}

              void toImmutableMultiset() {}
            }
            """)
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(StaticImport.class, getClass())
        .addInputLines(
            "A.java",
            """
            import static java.util.function.Predicate.not;

            import com.fasterxml.jackson.annotation.JsonCreator;
            import com.google.common.base.Predicates;
            import com.google.common.collect.ImmutableMap;
            import com.google.common.collect.ImmutableSet;
            import com.google.errorprone.BugPattern;
            import com.google.errorprone.BugPattern.SeverityLevel;
            import java.nio.charset.StandardCharsets;
            import java.util.ArrayList;
            import java.util.Collections;
            import java.util.Objects;
            import java.util.regex.Pattern;
            import org.junit.jupiter.params.provider.Arguments;
            import org.springframework.boot.test.context.SpringBootTest;
            import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
            import org.springframework.format.annotation.DateTimeFormat;
            import org.springframework.format.annotation.DateTimeFormat.ISO;
            import org.springframework.http.MediaType;

            class A {
              void m1() {
                ImmutableMap.toImmutableMap(v -> v, v -> v);
                ImmutableMap.<String, String, String>toImmutableMap(v -> v, v -> v);

                ImmutableSet.toImmutableSet();
                ImmutableSet.<String>toImmutableSet();

                Collections.disjoint(ImmutableSet.of(), ImmutableSet.of());
                Collections.reverse(new ArrayList<>());

                Predicates.not(null);
                not(null);

                Arguments.arguments("foo");

                Objects.requireNonNull("bar");

                Object o = StandardCharsets.UTF_8;

                ImmutableSet.of(
                    MediaType.ALL,
                    MediaType.APPLICATION_XHTML_XML,
                    MediaType.TEXT_HTML,
                    MediaType.valueOf("image/webp"));

                Pattern.compile("", Pattern.CASE_INSENSITIVE);
              }

              void m2(
                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String date,
                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) String dateTime,
                  @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) String time) {}

              void m3(
                  @DateTimeFormat(iso = ISO.DATE) String date,
                  @DateTimeFormat(iso = ISO.DATE_TIME) String dateTime,
                  @DateTimeFormat(iso = ISO.TIME) String time) {}

              @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
              private static A jsonCreator(int a) {
                return new A();
              }

              @BugPattern(
                  summary = "",
                  linkType = BugPattern.LinkType.NONE,
                  severity = SeverityLevel.SUGGESTION,
                  tags = BugPattern.StandardTags.SIMPLIFICATION)
              static final class TestBugPattern {}

              @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
              final class Test {}
            }
            """)
        .addOutputLines(
            "A.java",
            """
            import static com.fasterxml.jackson.annotation.JsonCreator.Mode.DELEGATING;
            import static com.google.common.collect.ImmutableMap.toImmutableMap;
            import static com.google.common.collect.ImmutableSet.toImmutableSet;
            import static com.google.errorprone.BugPattern.LinkType.NONE;
            import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
            import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
            import static java.nio.charset.StandardCharsets.UTF_8;
            import static java.util.Collections.disjoint;
            import static java.util.Collections.reverse;
            import static java.util.Objects.requireNonNull;
            import static java.util.function.Predicate.not;
            import static java.util.regex.Pattern.CASE_INSENSITIVE;
            import static org.junit.jupiter.params.provider.Arguments.arguments;
            import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
            import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;
            import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
            import static org.springframework.format.annotation.DateTimeFormat.ISO.TIME;
            import static org.springframework.http.MediaType.APPLICATION_XHTML_XML;
            import static org.springframework.http.MediaType.TEXT_HTML;

            import com.fasterxml.jackson.annotation.JsonCreator;
            import com.google.common.base.Predicates;
            import com.google.common.collect.ImmutableMap;
            import com.google.common.collect.ImmutableSet;
            import com.google.errorprone.BugPattern;
            import com.google.errorprone.BugPattern.SeverityLevel;
            import java.nio.charset.StandardCharsets;
            import java.util.ArrayList;
            import java.util.Collections;
            import java.util.Objects;
            import java.util.regex.Pattern;
            import org.junit.jupiter.params.provider.Arguments;
            import org.springframework.boot.test.context.SpringBootTest;
            import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
            import org.springframework.format.annotation.DateTimeFormat;
            import org.springframework.format.annotation.DateTimeFormat.ISO;
            import org.springframework.http.MediaType;

            class A {
              void m1() {
                toImmutableMap(v -> v, v -> v);
                ImmutableMap.<String, String, String>toImmutableMap(v -> v, v -> v);

                toImmutableSet();
                ImmutableSet.<String>toImmutableSet();

                disjoint(ImmutableSet.of(), ImmutableSet.of());
                reverse(new ArrayList<>());

                Predicates.not(null);
                not(null);

                arguments("foo");

                requireNonNull("bar");

                Object o = UTF_8;

                ImmutableSet.of(
                    MediaType.ALL, APPLICATION_XHTML_XML, TEXT_HTML, MediaType.valueOf("image/webp"));

                Pattern.compile("", CASE_INSENSITIVE);
              }

              void m2(
                  @DateTimeFormat(iso = DATE) String date,
                  @DateTimeFormat(iso = DATE_TIME) String dateTime,
                  @DateTimeFormat(iso = TIME) String time) {}

              void m3(
                  @DateTimeFormat(iso = DATE) String date,
                  @DateTimeFormat(iso = DATE_TIME) String dateTime,
                  @DateTimeFormat(iso = TIME) String time) {}

              @JsonCreator(mode = DELEGATING)
              private static A jsonCreator(int a) {
                return new A();
              }

              @BugPattern(summary = "", linkType = NONE, severity = SUGGESTION, tags = SIMPLIFICATION)
              static final class TestBugPattern {}

              @SpringBootTest(webEnvironment = RANDOM_PORT)
              final class Test {}
            }
            """)
        .doTest(TestMode.TEXT_MATCH);
  }
}
