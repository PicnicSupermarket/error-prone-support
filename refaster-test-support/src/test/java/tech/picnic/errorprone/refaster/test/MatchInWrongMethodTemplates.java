package tech.picnic.errorprone.refaster.test;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import tech.picnic.errorprone.refaster.annotation.TemplateCollection;

/**
 * Refaster template collection to validate reporting of a match occurring in an unexpected place.
 */
@TemplateCollection(linkPattern = "XXX")
final class MatchInWrongMethodTemplates {
  private MatchInWrongMethodTemplates() {}

  // XXX: Test merging/overriding.
  //  @TemplateCollection(linkPattern = "XXX")
  static final class StringIsEmpty {
    @BeforeTemplate
    boolean before(String string) {
      return string.equals("");
    }

    @AfterTemplate
    boolean after(String string) {
      return string.isEmpty();
    }
  }
}
