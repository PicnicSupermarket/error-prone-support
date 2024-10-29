package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;
import com.google.errorprone.bugpatterns.BugChecker;
import com.sun.tools.javac.util.Convert;
import javax.lang.model.element.Name;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class BugCheckerRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Convert.class, FixChoosers.class);
  }

  ImmutableSet<BugCheckerRefactoringTestHelper> testBugCheckerRefactoringTestHelperIdentity() {
    return ImmutableSet.of(
        BugCheckerRefactoringTestHelper.newInstance(BugChecker.class, getClass())
            .setFixChooser(FixChoosers.FIRST),
        BugCheckerRefactoringTestHelper.newInstance(BugChecker.class, getClass())
            .setImportOrder("static-first"));
  }

  BugCheckerRefactoringTestHelper
      testBugCheckerRefactoringTestHelperAddInputLinesExpectUnchanged() {
    return BugCheckerRefactoringTestHelper.newInstance(BugChecker.class, getClass())
        .addInputLines("A.java", "class A {}")
        .addOutputLines("A.java", "class A {}");
  }

  String testConstantsFormat() {
    return String.format("\"%s\"", Convert.quote("foo"));
  }

  ImmutableSet<Boolean> testNameContentEquals() {
    return ImmutableSet.of(
        ((Name) null).toString().equals("foo".subSequence(0, 1).toString()),
        ((com.sun.tools.javac.util.Name) null).toString().equals("bar"));
  }
}
