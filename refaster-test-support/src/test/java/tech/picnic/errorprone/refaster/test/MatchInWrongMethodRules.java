package tech.picnic.errorprone.refaster.test;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import tech.picnic.errorprone.refaster.annotation.TemplateCollection;

/** Refaster rule collection to validate reporting of a match occurring in an unexpected place. */
final class MatchInWrongMethodRules {
  private MatchInWrongMethodRules() {}

  // XXX: Demo: nesting overrides.
  @TemplateCollection(linkPattern = "YYY", severity = ERROR, description = "Foo")
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
