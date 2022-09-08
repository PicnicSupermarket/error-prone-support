package tech.picnic.errorprone.refaster.test;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;

/** Code to test the Refaster templates from {@link ValidTemplates}. */
final class ValidTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Strings.class);
  }

  boolean testStringIsEmpty2() {
    return "foo".toCharArray().length == 0;
  }

  boolean testStaticImportStringLength() {
    return "foo" == null || "foo".isEmpty();
  }

  void testBlockTemplateSetAddElement() {
    Set<Integer> set = new HashSet<>();
    Integer element = 1;
    if (!set.contains(element)) {
      set.add(element);
      System.out.print("added the following element to set: " + element);
    }
  }
}
