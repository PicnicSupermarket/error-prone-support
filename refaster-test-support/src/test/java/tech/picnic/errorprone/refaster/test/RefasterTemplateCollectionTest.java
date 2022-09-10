package tech.picnic.errorprone.refaster.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Validates {@link RefasterTemplateCollection} error reporting.
 *
 * <p>The goal of the Refaster template collections under test is to verify the reporting of
 * violations by {@link RefasterTemplateCollection} using the associated {@code TestInput.java} and
 * {@code TestOutput.java} files. Normally, {@link RefasterTemplateCollection} will raise error
 * messages to be rendered in the console or IDE. However, to verify that these error messages are
 * emitted as intended, the {@code *TestOutput.java} files in this package contain error reporting
 * that is normally not present.
 */
final class RefasterTemplateCollectionTest {
  @ParameterizedTest
  @ValueSource(
      classes = {
        MatchInWrongMethodTemplates.class,
        MethodWithoutPrefixTemplates.class,
        MisnamedTestClassTemplates.class,
        MissingTestAndWrongTestTemplates.class,
        PartialTestMatchTemplates.class,
        TemplateWithoutTestTemplates.class,
        ValidTemplates.class
      })
  void verifyRefasterTemplateCollections(Class<?> clazz) {
    RefasterTemplateCollection.validate(clazz);
  }
}
