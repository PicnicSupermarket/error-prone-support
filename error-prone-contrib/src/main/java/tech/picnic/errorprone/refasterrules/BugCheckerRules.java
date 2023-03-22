package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChooser;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to {@link com.google.errorprone.bugpatterns.BugChecker} classes. */
@OnlineDocumentation
final class BugCheckerRules {
  private BugCheckerRules() {}

  /**
   * Avoid calling {@link BugCheckerRefactoringTestHelper#setFixChooser(FixChooser)} or {@link
   * BugCheckerRefactoringTestHelper#setImportOrder(String)} with their respective default values.
   */
  static final class BugCheckerRefactoringTestHelperIdentity {
    @BeforeTemplate
    BugCheckerRefactoringTestHelper before(BugCheckerRefactoringTestHelper helper) {
      return Refaster.anyOf(
          helper.setFixChooser(FixChoosers.FIRST), helper.setImportOrder("static-first"));
    }

    @AfterTemplate
    BugCheckerRefactoringTestHelper after(BugCheckerRefactoringTestHelper helper) {
      return helper;
    }
  }

  /**
   * Prefer {@link BugCheckerRefactoringTestHelper.ExpectOutput#expectUnchanged()} over repeating
   * the input.
   */
  // XXX: This rule assumes that the full source code is specified as a single string, e.g. using a
  // text block. Support for multi-line source code input would require a `BugChecker`
  // implementation instead.
  static final class BugCheckerRefactoringTestHelperAddInputLinesExpectUnchanged {
    @BeforeTemplate
    BugCheckerRefactoringTestHelper before(
        BugCheckerRefactoringTestHelper helper, String path, String source) {
      return helper.addInputLines(path, source).addOutputLines(path, source);
    }

    @AfterTemplate
    BugCheckerRefactoringTestHelper after(
        BugCheckerRefactoringTestHelper helper, String path, String source) {
      return helper.addInputLines(path, source).expectUnchanged();
    }
  }
}
