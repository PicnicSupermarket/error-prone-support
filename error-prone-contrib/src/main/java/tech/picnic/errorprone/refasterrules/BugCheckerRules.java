package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.util.Constants;
import com.sun.tools.javac.util.Convert;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import javax.lang.model.element.Name;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.utils.SourceCode;

/** Refaster rules related to {@link com.google.errorprone.bugpatterns.BugChecker} classes. */
@OnlineDocumentation
final class zBugCheckerRules {
  private BugCheckerRules() {}

  /** Prefer the {@link BugCheckerRefactoringTestHelper} as-is over more verbose alternatives. */
  static final class BugCheckerRefactoringTestHelperIdentity {
    @BeforeTemplate
    BugCheckerRefactoringTestHelper before(
        BugCheckerRefactoringTestHelper bugCheckerRefactoringTestHelper) {
      return Refaster.anyOf(
          bugCheckerRefactoringTestHelper.setFixChooser(FixChoosers.FIRST),
          bugCheckerRefactoringTestHelper.setImportOrder("static-first"));
    }

    @AfterTemplate
    @CanIgnoreReturnValue
    BugCheckerRefactoringTestHelper after(
        BugCheckerRefactoringTestHelper bugCheckerRefactoringTestHelper) {
      return bugCheckerRefactoringTestHelper;
    }
  }

  /**
   * Prefer {@link BugCheckerRefactoringTestHelper.ExpectOutput#expectUnchanged()} over more verbose
   * alternatives.
   */
  // XXX: This rule assumes that the full source code is specified as a single string, e.g. using a
  // text block. Support for multi-line source code input would require a `BugChecker`
  // implementation instead.
  static final class BugCheckerRefactoringTestHelperAddInputLinesExpectUnchanged {
    @BeforeTemplate
    BugCheckerRefactoringTestHelper before(
        BugCheckerRefactoringTestHelper bugCheckerRefactoringTestHelper, String path, String str) {
      return bugCheckerRefactoringTestHelper.addInputLines(path, str).addOutputLines(path, str);
    }

    @AfterTemplate
    BugCheckerRefactoringTestHelper after(
        BugCheckerRefactoringTestHelper bugCheckerRefactoringTestHelper, String path, String str) {
      return bugCheckerRefactoringTestHelper.addInputLines(path, str).expectUnchanged();
    }
  }

  /**
   * Prefer {@link SourceCode#toStringConstantExpression(Object,
   * com.google.errorprone.VisitorState)} over more contrived alternatives.
   */
  static final class SourceCodeToStringConstantExpression {
    @BeforeTemplate
    String before(CharSequence value) {
      return Constants.format(value);
    }

    @BeforeTemplate
    String before(String value) {
      return "\"%s\"".formatted(Convert.quote(value));
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
    boolean before(Name name, CharSequence anObject) {
      return Refaster.anyOf(
          name.toString().equals(anObject.toString()), anObject.toString().equals(name.toString()));
    }

    @BeforeTemplate
    boolean before(Name name, String anObject) {
      return Refaster.anyOf(name.toString().equals(anObject), anObject.equals(name.toString()));
    }

    @AfterTemplate
    boolean after(Name name, CharSequence anObject) {
      return name.contentEquals(anObject);
    }
  }

  /** Prefer {@link ASTHelpers#getStartPosition(Tree)} over more fragile alternatives. */
  static final class ASTHelpersGetStartPosition<T extends DiagnosticPosition> {
    @BeforeTemplate
    @SuppressWarnings("unchecked" /* This violation will be rewritten. */)
    int before(Tree tree) {
      return ((T) tree).getStartPosition();
    }

    @AfterTemplate
    int after(Tree tree) {
      return ASTHelpers.getStartPosition(tree);
    }
  }
}
