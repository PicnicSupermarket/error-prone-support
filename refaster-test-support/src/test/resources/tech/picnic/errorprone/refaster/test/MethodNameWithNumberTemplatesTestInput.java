package tech.picnic.errorprone.refaster.test;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;

/** Code to test the Refaster templates from `MethodNameWithNumberTemplates`. */
final class MethodNameWithNumberTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Collections.class);
  }

  boolean testStringIsEmpty2() {
    return "foo".equals("");
  }

  private void foo() {}

  public String bar() {
    return "";
  }
}
