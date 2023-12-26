package tech.picnic.errorprone.refaster.test;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;

/** Code to test the Refaster rules from {@link ValidRules}. */
final class ValidRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Strings.class);
  }

  boolean testStringIsEmpty2() {
    return "foo".toCharArray().length == 0;
  }

  boolean testStaticImportStringLength() {
    return "foo" == null || "foo".toCharArray().length == 0;
  }

  void testBlockRuleSetAddElement() {
    Set<Integer> set = new HashSet<>();
    Integer element = 1;
    if (!set.contains(element)) {
      set.add(element);
      System.out.print("added the following element to set: " + element);
    }
  }
}
