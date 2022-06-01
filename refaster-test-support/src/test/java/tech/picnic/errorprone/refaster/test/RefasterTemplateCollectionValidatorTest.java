package tech.picnic.errorprone.refaster.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Validate error reporting of the Refaster template collections.
 *
 * <p>The goal of the Refaster template collections under test is to verify the reporting of {@link
 * RefasterTemplateCollectionValidator} using the associated `TestInput` and `TestOutput` files.
 * Normally, {@link RefasterTemplateCollectionValidator} will report error messages directly in the
 * console where the results of the tests are shown. However, to verify that these messages are
 * correct, the `*TestOutput` files in this package contain error reporting that is normally not
 * written.
 */
final class RefasterTemplateCollectionValidatorTest {
  @ParameterizedTest
  @ValueSource(
      classes = {
        MatchInWrongMethodTemplates.class,
        MethodWithoutPrefixTemplates.class,
        MissingTestAndWrongTestTemplates.class,
        PartialTestMatchTemplates.class,
        TemplateWithoutTestTemplates.class,
        ValidTemplates.class
      })
  void verifyRefasterTemplateCollections(Class<?> clazz) {
    RefasterTemplateCollectionValidator.validate(clazz);
  }
}
