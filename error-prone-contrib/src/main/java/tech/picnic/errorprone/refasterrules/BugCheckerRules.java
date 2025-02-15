package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChooser;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.sun.tools.javac.util.Constants;
import com.sun.tools.javac.util.Convert;
import javax.lang.model.element.Name;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.utils.SourceCode;

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
    @CanIgnoreReturnValue
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

  /**
   * Prefer {@link SourceCode#toStringConstantExpression(Object,
   * com.google.errorprone.VisitorState)} over alternatives that unnecessarily escape single quote
   * characters.
   */
  static final class ConstantsFormat {
    @BeforeTemplate
    String before(CharSequence value) {
      return Constants.format(value);
    }

    @BeforeTemplate
    String before(String value) {
      return String.format("\"%s\"", Convert.quote(value));
    }

    @AfterTemplate
    String after(CharSequence value) {
      return SourceCode.toStringConstantExpression(
          value, Refaster.emitCommentBefore("REPLACEME", null));
    }
  }

  /** Prefer {@link Name#contentEquals(CharSequence)} over more verbose alternatives. */
  static final class NameContentEquals {
    @BeforeTemplate
    boolean before(Name name, CharSequence string) {
      return name.toString().equals(string.toString());
    }

    @BeforeTemplate
    boolean before(Name name, String string) {
      return name.toString().equals(string);
    }

    @AfterTemplate
    boolean after(Name name, CharSequence string) {
      return name.contentEquals(string);
    }
  }
}
