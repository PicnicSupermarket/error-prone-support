package tech.picnic.errorprone.refaster.test;

import static com.google.common.base.Strings.isNullOrEmpty;

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
    return "foo".isEmpty();
  }

  boolean testStaticImportStringLength() {
    return isNullOrEmpty("foo");
  }

  void testSetAddElement() {
    Set<Integer> set = new HashSet<>();
    Integer element = 1;
    if (set.add(element)) {
      System.out.print("added the following element to set: " + element);
    }
  }

  /* ERROR: Method names should start with `test`. */
  private void foo() {}

  /* ERROR: Method names should start with `test`. */
  public String bar() {
    return "";
  }
}
