package tech.picnic.errorprone.refaster.test;

import static com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.FileObjects;
import java.io.IOException;
import javax.tools.JavaFileObject;

/** Utility to test Refaster templates and validate the tests for template collections. */
public final class RefasterCollectionTestUtil {
  private RefasterCollectionTestUtil() {}

  /**
   * Verifies that all Refaster templates from a collection are covered by at least one test and
   * that the match rewrites code in the correct test method.
   *
   * <p>Note that this doesn't guarantee full coverage: this test does not ascertain that all {@link
   * com.google.errorprone.refaster.Refaster#anyOf} branches are tested. Idem for {@link
   * com.google.errorprone.refaster.annotation.BeforeTemplate} methods in case there are multiple.
   *
   * @param clazz The Refaster template collection under test.
   */
  public static void validateTemplateCollection(Class<?> clazz) {
    String className = clazz.getSimpleName();

    BugCheckerRefactoringTestHelper.newInstance(
            RefasterValidateTests.class, RefasterCollectionTestUtil.class)
        .setArgs(ImmutableList.of("-XepOpt:RefasterValidateTests:TemplateCollection=" + className))
        .addInputLines(
            clazz.getName() + "TestInput.java",
            getContentOfResourceFile(clazz, className + "TestInput.java"))
        .addOutputLines(
            clazz.getName() + "TestOutput.java",
            getContentOfResourceFile(clazz, className + "TestOutput.java"))
        .doTest(TEXT_MATCH);
  }

  private static String getContentOfResourceFile(Class<?> clazz, String resourceName) {
    JavaFileObject object = FileObjects.forResource(clazz, resourceName);
    try {
      return object.getCharContent(false).toString();
    } catch (IOException e) {
      throw new IllegalStateException("Can't retrieve content for file " + resourceName, e);
    }
  }
}
