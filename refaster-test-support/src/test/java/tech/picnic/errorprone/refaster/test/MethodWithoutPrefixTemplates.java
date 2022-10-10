package tech.picnic.errorprone.refaster.test;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/**
 * Refaster rule collection to validate that in the tests all methods have the prefix `test`.
 *
 * <p>The only exception to this is {@link
 * RefasterRuleCollectionTestCase#elidedTypesAndStaticImports()}.
 */
final class MethodWithoutPrefixTemplates {
  private MethodWithoutPrefixTemplates() {}

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
