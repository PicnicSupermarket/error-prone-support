package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;
import com.google.errorprone.bugpatterns.BugChecker;
import com.sun.tools.javac.util.Constants;
import com.sun.tools.javac.util.Convert;
import javax.lang.model.element.Name;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;
import tech.picnic.errorprone.utils.SourceCode;

final class BugCheckerRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Constants.class, Convert.class, FixChoosers.class);
  }

  ImmutableSet<BugCheckerRefactoringTestHelper> testBugCheckerRefactoringTestHelperIdentity() {
    return ImmutableSet.of(
        BugCheckerRefactoringTestHelper.newInstance(BugChecker.class, getClass()),
        BugCheckerRefactoringTestHelper.newInstance(BugChecker.class, getClass()));
  }

  BugCheckerRefactoringTestHelper
      testBugCheckerRefactoringTestHelperAddInputLinesExpectUnchanged() {
    return BugCheckerRefactoringTestHelper.newInstance(BugChecker.class, getClass())
        .addInputLines("A.java", "class A {}")
        .expectUnchanged();
  }

  ImmutableSet<String> testConstantsFormat() {
    return ImmutableSet.of(
        SourceCode.toStringConstantExpression("foo", /* REPLACEME */ null),
        SourceCode.toStringConstantExpression("bar", /* REPLACEME */ null));
  }

  ImmutableSet<Boolean> testNameContentEquals() {
    return ImmutableSet.of(
        ((Name) null).contentEquals("foo".subSequence(0, 1)),
        ((com.sun.tools.javac.util.Name) null).contentEquals("bar"));
  }
}
