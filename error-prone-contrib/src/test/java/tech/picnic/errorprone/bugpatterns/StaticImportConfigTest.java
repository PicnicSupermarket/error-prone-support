package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableMap;
import com.google.errorprone.ErrorProneFlags;
import java.time.Clock;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class StaticImportConfigTest {
  @Test
  void candidateTypesDoNotClash() {
    assertThat(StaticImportConfig.STATIC_IMPORT_CANDIDATE_TYPES)
        .doesNotContainAnyElementsOf(StaticImportConfig.NON_STATIC_IMPORT_CANDIDATE_TYPES);
  }

  @Test
  void candidateMembersAreNotRedundant() {
    assertThat(StaticImportConfig.STATIC_IMPORT_CANDIDATE_MEMBERS.keySet())
        .doesNotContainAnyElementsOf(StaticImportConfig.STATIC_IMPORT_CANDIDATE_TYPES);

    assertThat(StaticImportConfig.NON_STATIC_IMPORT_CANDIDATE_MEMBERS.keySet())
        .doesNotContainAnyElementsOf(StaticImportConfig.NON_STATIC_IMPORT_CANDIDATE_TYPES);

    assertThat(StaticImportConfig.NON_STATIC_IMPORT_CANDIDATE_MEMBERS.values())
        .doesNotContainAnyElementsOf(StaticImportConfig.NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS);
  }

  @Test
  void candidateMembersDoNotClash() {
    assertThat(StaticImportConfig.STATIC_IMPORT_CANDIDATE_MEMBERS.entries())
        .doesNotContainAnyElementsOf(
            StaticImportConfig.NON_STATIC_IMPORT_CANDIDATE_MEMBERS.entries());

    assertThat(StaticImportConfig.STATIC_IMPORT_CANDIDATE_MEMBERS.values())
        .doesNotContainAnyElementsOf(StaticImportConfig.NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS);
  }

  @SuppressWarnings(
      "java:S3415" /* Comparing a constant against a non-constant value is intentional here. */)
  @Test
  void candidateIdentifiersDoNotClash() {
    assertThat(StaticImportConfig.NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS)
        .doesNotContainAnyElementsOf(StaticImportConfig.STATIC_IMPORT_CANDIDATE_MEMBERS.values());
  }

  private static Stream<Arguments> additionalCandidateValidationTestCases() {
    /* { value, expectedError } */
    return Stream.of(
        arguments(
            Clock.class.getCanonicalName(),
            "Invalid `StaticImport:AdditionalCandidates` flag value 'java.time.Clock': type is a "
                + "non-static import candidate"),
        arguments(
            "java.lang.Math, java.util.UUID",
            "Invalid `StaticImport:AdditionalCandidates` flag value ' java.util.UUID': expected "
                + "'<type>' or '<type>#<member>'"),
        arguments(
            "java.lang.Math#min#max",
            "Invalid `StaticImport:AdditionalCandidates` flag value 'java.lang.Math#min#max': "
                + "expected '<type>' or '<type>#<member>'"),
        arguments(
            "#hash",
            "Invalid `StaticImport:AdditionalCandidates` flag value '#hash': expected '<type>' or "
                + "'<type>#<member>'"),
        arguments(
            "java.util.Objects#",
            "Invalid `StaticImport:AdditionalCandidates` flag value 'java.util.Objects#': "
                + "expected '<type>' or '<type>#<member>'"),
        arguments(
            "java.lang.Math#max, java.lang.Math#min",
            "Invalid `StaticImport:AdditionalCandidates` flag value ' java.lang.Math#min': "
                + "expected '<type>' or '<type>#<member>'"),
        arguments(
            "com.example.Foo#of",
            "Invalid `StaticImport:AdditionalCandidates` flag value 'com.example.Foo#of': "
                + "identifier is a non-static import candidate"),
        arguments(
            "java.util.Locale#ROOT",
            "Invalid `StaticImport:AdditionalCandidates` flag value 'java.util.Locale#ROOT': "
                + "member is a non-static import candidate"));
  }

  @MethodSource("additionalCandidateValidationTestCases")
  @ParameterizedTest
  void additionalCandidateValidation(String value, String expectedError) {
    assertThatThrownBy(
            () ->
                new StaticImportConfig(
                    ErrorProneFlags.fromMap(
                        ImmutableMap.of("StaticImport:AdditionalCandidates", value))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(expectedError);
  }
}
