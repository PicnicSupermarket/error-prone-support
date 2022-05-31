package tech.picnic.errorprone.refaster.test;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Code to test the Refaster templates from `DummyTemplates`. */
final class DummyTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Collections.class, Strings.class);
  }

  boolean testStringIsEmpty2() {
    return "foo".equals("");
  }

  boolean testStaticImportStringLength() {
    return "foo" == null || "foo".isEmpty();
  }

  void testSetAddElement() {
    Set<Integer> set = new HashSet<>();
    Integer element = 1;
    if (!set.contains(element)) {
      set.add(element);
      System.out.print("added the following element to set: " + element);
    }
  }

  private void foo() {}

  public String bar() {
    return "";
  }
}
