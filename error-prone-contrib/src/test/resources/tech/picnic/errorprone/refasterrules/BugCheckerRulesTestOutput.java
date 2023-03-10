package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;
import tech.picnic.errorprone.bugpatterns.StringCaseLocaleUsage;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class BugCheckerRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(FixChoosers.class);
  }

  BugCheckerRefactoringTestHelper testSetFixChooserDefault() {
    return BugCheckerRefactoringTestHelper.newInstance(StringCaseLocaleUsage.class, getClass());
  }
}
