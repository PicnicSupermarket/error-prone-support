package tech.picnic.errorprone.refaster.test;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/** Refaster rule collection to validate reporting of a misnamed test class. */
final class MisnamedTestClassTemplates {
  private MisnamedTestClassTemplates() {}

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
