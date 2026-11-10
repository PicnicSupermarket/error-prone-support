package tech.picnic.errorprone.refaster.test;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Validates {@link RefasterRuleCollection} error reporting. */
// XXX: Also add a test for which `RefasterRuleCollection#validate` throws an exception; as-is, a
// no-op implementation also passes.
final class RefasterRuleCollectionTest {
  /**
   * Validates {@link RefasterRuleCollection} error reporting for the given Refaster rule
   * collection.
   *
   * <p>The goal of the Refaster rule collections under test is to verify the reporting of
   * violations by {@link RefasterRuleCollection} using the associated {@code TestInput.java} and
   * {@code TestOutput.java} files. Normally, {@link RefasterRuleCollection} will raise error
   * messages to be rendered in the console or IDE. However, to verify that these error messages are
   * emitted as intended, the {@code *TestOutput.java} files in this package contain error reporting
   * that is normally not present.
   */
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
  void verifyRefasterRuleCollections(Class<?> clazz) {
    RefasterRuleCollection.validate(clazz);
  }

  /**
   * Validates {@link RefasterRuleCollection} error reporting for unexpected tokens.
   *
   * @implNote This test is separate from the test cases passed to {@link
   *     #verifyRefasterRuleCollections(Class)}, as those are subject the Maven build's Google Java
   *     Format configuration, which disallows the top-level semicolon flagged here.
   */
  @Test
  void unexpectedToken() {
    BugCheckerRefactoringTestHelper.newInstance(RefasterRuleCollection.class, getClass())
        .setArgs("-XepOpt:RefasterRuleCollection:RuleCollection=EmptyRules")
        .addInputLines("A.java", "final class EmptyRulesTest {}", ";")
        .addOutputLines(
            "A.java", "final class EmptyRulesTest {}", "/* ERROR: Unexpected token. */", ";")
        .doTest();
  }
}
