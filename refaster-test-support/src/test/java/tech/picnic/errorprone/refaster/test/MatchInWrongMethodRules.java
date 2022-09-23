package tech.picnic.errorprone.refaster.test;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rule collection to validate reporting of a match occurring in an unexpected place. */
final class MatchInWrongMethodRules {
  private MatchInWrongMethodRules() {}

  // XXX: Demo: nesting overrides.
  //  @Website("YYY")
  //  @Severity(ERROR)
  //  @Description("Fooo!")
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
