package tech.picnic.errorprone.refaster.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

final class RefasterCollectionTestUtilTest {
  @ParameterizedTest
  @ValueSource(
      classes = {
        MatchInWrongMethodTemplates.class,
        MethodNameWithNumberTemplates.class,
        MissingTestAndWrongTestTemplates.class,
        TemplateWithoutTestTemplates.class
      })
  void verifyRefasterTemplateCollections(Class<?> clazz) {
    RefasterCollectionTestUtil.validateTemplateCollection(clazz);
  }
}
