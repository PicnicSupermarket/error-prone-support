package tech.picnic.errorprone.refaster.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Validates {@link RefasterRuleCollection} error reporting.
 *
 * <p>The goal of the Refaster rule collections under test is to verify the reporting of violations
 * by {@link RefasterRuleCollection} using the associated {@code TestInput.java} and {@code
 * TestOutput.java} files. Normally, {@link RefasterRuleCollection} will raise error messages to be
 * rendered in the console or IDE. However, to verify that these error messages are emitted as
 * intended, the {@code *TestOutput.java} files in this package contain error reporting that is
 * normally not present.
 */
final class RefasterRuleCollectionTest {
  @ParameterizedTest
  @ValueSource(
      classes = {
        MatchInWrongMethodRules.class,
        MethodWithoutPrefixRules.class,
        MisnamedTestClassRules.class,
        MissingTestAndWrongTestRules.class,
        PartialTestMatchRules.class,
        RuleWithoutTestRules.class,
        ValidRules.class
      })
  void verifyRefasterTemplateCollections(Class<?> clazz) {
    RefasterRuleCollection.validate(clazz);
  }
}
