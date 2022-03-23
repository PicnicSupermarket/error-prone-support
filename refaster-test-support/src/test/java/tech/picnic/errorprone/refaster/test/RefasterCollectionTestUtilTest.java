package tech.picnic.errorprone.refaster.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Validate error reporting of the Refaster template collections. */
final class RefasterCollectionTestUtilTest {
  @ParameterizedTest
  @ValueSource(
      classes = {
        MatchInWrongMethodTemplates.class,
        MethodNameWithNumberTemplates.class,
        MissingTestAndWrongTestTemplates.class,
        PartialTemplateMatchTemplates.class,
        TemplateWithoutTestTemplates.class
      })
  void verifyRefasterTemplateCollections(Class<?> clazz) {
    RefasterCollectionTestUtil.validateTemplateCollection(clazz);
  }
}
