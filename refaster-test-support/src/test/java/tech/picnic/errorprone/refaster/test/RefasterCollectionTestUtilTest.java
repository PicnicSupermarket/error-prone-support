package tech.picnic.errorprone.refaster.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Validate error reporting of the Refaster template collections.
 *
 * <p>The goal of the Refaster template collections under test is to verify the reporting of {@link
 * RefasterCollectionTestUtil} using the associated `TestInput` and `TestOutput` files. Normally,
 * {@link RefasterCollectionTestUtil} will report error messages directly in the console where the
 * results of the tests are shown. However, to verify that these messages are correct, the
 * `*TestOutput` files in this package contain error reporting that is normally not written.
 */
final class RefasterCollectionTestUtilTest {
  @ParameterizedTest
  @ValueSource(
      classes = {
        MatchInWrongMethodTemplates.class,
        MethodNameWithNumberTemplates.class,
        MissingTestAndWrongTestTemplates.class,
        PartialTestMatchTemplates.class,
        TemplateWithoutTestTemplates.class
      })
  void verifyRefasterTemplateCollections(Class<?> clazz) {
    RefasterCollectionTestUtil.validateTemplateCollection(clazz);
  }
}
