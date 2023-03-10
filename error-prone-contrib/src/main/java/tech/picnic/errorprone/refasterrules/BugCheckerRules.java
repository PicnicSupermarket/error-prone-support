package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChooser;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to BugChecker classes. */
@OnlineDocumentation
final class BugCheckerRules {
  private BugCheckerRules() {}

  /**
   * Drop {@link BugCheckerRefactoringTestHelper#setFixChooser(FixChooser)} when set to the default
   * {@link FixChoosers#FIRST}.
   */
  static final class SetFixChooserDefault {
    @BeforeTemplate
    BugCheckerRefactoringTestHelper before(BugCheckerRefactoringTestHelper helper) {
      return helper.setFixChooser(FixChoosers.FIRST);
    }

    @AfterTemplate
    BugCheckerRefactoringTestHelper after(BugCheckerRefactoringTestHelper helper) {
      return helper;
    }
  }
}
