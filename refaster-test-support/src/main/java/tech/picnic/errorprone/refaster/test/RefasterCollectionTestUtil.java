package tech.picnic.errorprone.refaster.test;

import static com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugCheckerRefactoringTestHelper;

/**
 * Utility to test Refaster templates and validate that the tests use meet all requirements for
 * template collection tests.
 */
// XXX: Consider dropping this class in favour of a static
// `RefasterTemplateCollectionValidator#validate` method.
public final class RefasterCollectionTestUtil {
  private RefasterCollectionTestUtil() {}

  /**
   * Verifies that all Refaster templates in the given collection class are covered by precisely one
   * test method, defined explicitly for the purpose of exercising that template.
   *
   * <p>Note that a passing test does not guarantee full coverage: this test does not ascertain that
   * all {@link com.google.errorprone.refaster.Refaster#anyOf} branches are tested. Likewise for
   * {@link com.google.errorprone.refaster.annotation.BeforeTemplate} methods in case there are
   * multiple.
   *
   * @param clazz The Refaster template collection under test.
   */
  public static void validateTemplateCollection(Class<?> clazz) {
    String className = clazz.getSimpleName();

    BugCheckerRefactoringTestHelper.newInstance(RefasterValidateTests.class, clazz)
        .setArgs(ImmutableList.of("-XepOpt:RefasterValidateTests:TemplateCollection=" + className))
        .addInput(className + "TestInput.java")
        .addOutput(className + "TestOutput.java")
        .doTest(TEXT_MATCH);
  }
}
