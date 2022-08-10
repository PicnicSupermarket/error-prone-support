package tech.picnic.errorprone.refaster.test;

import com.google.common.collect.ImmutableSet;

/** Code to test the Refaster templates from {@link MethodWithoutPrefixTemplates}. */
final class MethodWithoutPrefixTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of();
  }

  boolean testStringIsEmpty() {
    return "foo".equals("");
  }

  private void foo() {}

  public String bar() {
    return "";
  }
}
