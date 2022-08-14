package tech.picnic.errorprone.bugpatterns.util;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.ReturnTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import javax.lang.model.element.Name;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

final class SourceCodeTest {
  @DisabledForJreRange(max = JRE.JAVA_14)
  @Test
  void isTextBlock() {
    CompilationTestHelper.newInstance(TextBlockFlagger.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  String negative1() {",
            "    return toString();",
            "  }",
            "",
            "  String negative2() {",
            "    return \"foo\";",
            "  }",
            "",
            "  String positive1() {",
            "    // BUG: Diagnostic contains:",
            "    return \"\"\"",
            "    foo",
            "    \"\"\";",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void deleteWithTrailingWhitespaceAnnotations() {
    BugCheckerRefactoringTestHelper.newInstance(
            DeleteWithTrailingWhitespaceTestChecker.class, getClass())
        .addInputLines("AnnotationToBeDeleted.java", "@interface AnnotationToBeDeleted {}")
        .expectUnchanged()
        .addInputLines(
            "AnotherAnnotationToBeDeleted.java", "@interface AnotherAnnotationToBeDeleted {}")
        .expectUnchanged()
        .addInputLines(
            "AnnotationDeletions.java",
            "interface AnnotationDeletions {",
            "  class SoleAnnotation {",
            "    @AnnotationToBeDeleted",
            "    void m() {}",
            "  }",
            "",
            "  class FirstAnnotation {",
            "    @AnnotationToBeDeleted",
            "    @Deprecated",
            "    void m() {}",
            "  }",
            "",
            "  class MiddleAnnotation {",
            "    @Deprecated",
            "    @AnnotationToBeDeleted",
            "    @SuppressWarnings(\"foo\")",
            "    void m() {}",
            "  }",
            "",
            "  class LastAnnotation {",
            "    @Deprecated",
            "    @AnnotationToBeDeleted",
            "    void m() {}",
            "  }",
            "",
            "  class MultipleAnnotations {",
            "    @AnnotationToBeDeleted",
            "    @AnotherAnnotationToBeDeleted",
            "    @Deprecated",
            "    void m() {}",
            "  }",
            "}")
        .addOutputLines(
            "AnnotationDeletions.java",
            "interface AnnotationDeletions {",
            "  class SoleAnnotation {",
            "    void m() {}",
            "  }",
            "",
            "  class FirstAnnotation {",
            "    @Deprecated",
            "    void m() {}",
            "  }",
            "",
            "  class MiddleAnnotation {",
            "    @Deprecated",
            "    @SuppressWarnings(\"foo\")",
            "    void m() {}",
            "  }",
            "",
            "  class LastAnnotation {",
            "    @Deprecated",
            "    void m() {}",
            "  }",
            "",
            "  class MultipleAnnotations {",
            "    @Deprecated",
            "    void m() {}",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void deleteWithTrailingWhitespaceMethods() {
    BugCheckerRefactoringTestHelper.newInstance(
            DeleteWithTrailingWhitespaceTestChecker.class, getClass())
        .addInputLines(
            "MethodDeletions.java",
            "interface MethodDeletions {",
            "  class SoleMethod {",
            "    void methodToBeDeleted() {}",
            "  }",
            "",
            "  class FirstMethod {",
            "    void methodToBeDeleted() {}",
            "",
            "    void finalMethod() {}",
            "  }",
            "",
            "  class MiddleMethod {",
            "    void initialMethod() {}",
            "",
            "    void methodToBeDeleted() {}",
            "",
            "    void finalMethod() {}",
            "  }",
            "",
            "  class LastMethod {",
            "    void initialMethod() {}",
            "",
            "    void methodToBeDeleted() {}",
            "  }",
            "",
            "  class MultipleMethods {",
            "    void method1ToBeDeleted() {}",
            "",
            "    void method2ToBeDeleted() {}",
            "",
            "    void middleMethod() {}",
            "",
            "    void method3ToBeDeleted() {}",
            "",
            "    void method4ToBeDeleted() {}",
            "  }",
            "}")
        .addOutputLines(
            "MethodDeletions.java",
            "interface MethodDeletions {",
            "  class SoleMethod {}",
            "",
            "  class FirstMethod {",
            "    void finalMethod() {}",
            "  }",
            "",
            "  class MiddleMethod {",
            "    void initialMethod() {}",
            "",
            "    void finalMethod() {}",
            "  }",
            "",
            "  class LastMethod {",
            "    void initialMethod() {}",
            "  }",
            "",
            "  class MultipleMethods {",
            "    void middleMethod() {}",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void unwrapMethodInvocation() {
    BugCheckerRefactoringTestHelper.newInstance(UnwrapMethodInvocationTestChecker.class, getClass())
        .addInputLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "",
            "class A {",
            "  Object[] m() {",
            "    return new Object[][] {",
            "      {ImmutableList.of()},",
            "      {ImmutableList.of(1)},",
            "      {com.google.common.collect.ImmutableList.of(1, 2)},",
            "      {",
            "        0, /*a*/",
            "        ImmutableList /*b*/./*c*/ <Integer> /*d*/of /*e*/(/*f*/ 1 /*g*/, /*h*/ 2 /*i*/) /*j*/",
            "      }",
            "    };",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "",
            "class A {",
            "  Object[] m() {",
            "    return new Object[][] {{}, {1}, {1, 2}, {0, /*a*/ /*f*/ 1 /*g*/, /*h*/ 2 /*i*/ /*j*/}};",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void unwrapMethodInvocationDroppingWhitespaceAndComments() {
    BugCheckerRefactoringTestHelper.newInstance(
            UnwrapMethodInvocationDroppingWhitespaceAndCommentsTestChecker.class, getClass())
        .addInputLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "",
            "class A {",
            "  Object[] m() {",
            "    return new Object[][] {",
            "      {ImmutableList.of()},",
            "      {ImmutableList.of(1)},",
            "      {com.google.common.collect.ImmutableList.of(1, 2)},",
            "      {",
            "        0, /*a*/",
            "        ImmutableList /*b*/./*c*/ <Integer> /*d*/of /*e*/(/*f*/ 1 /*g*/, /*h*/ 2 /*i*/) /*j*/",
            "      }",
            "    };",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "",
            "class A {",
            "  Object[] m() {",
            "    return new Object[][] {{}, {1}, {1, 2}, {0, /*a*/ 1, 2 /*j*/}};",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  /**
   * A {@link BugChecker} that delegates to {@link SourceCode#isTextBlock(ExpressionTree,
   * VisitorState)}.
   */
  @BugPattern(summary = "Interacts with `SourceCode` for testing purposes", severity = ERROR)
  public static final class TextBlockFlagger extends BugChecker implements ReturnTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchReturn(ReturnTree tree, VisitorState state) {
      return SourceCode.isTextBlock(tree.getExpression(), state)
          ? describeMatch(tree)
          : Description.NO_MATCH;
    }
  }

  /**
   * A {@link BugChecker} that uses {@link SourceCode#deleteWithTrailingWhitespace(Tree,
   * VisitorState)} to suggest the deletion of annotations and methods with a name containing
   * {@value DELETION_MARKER}.
   */
  @BugPattern(severity = ERROR, summary = "Interacts with `SourceCode` for testing purposes")
  public static final class DeleteWithTrailingWhitespaceTestChecker extends BugChecker
      implements AnnotationTreeMatcher, MethodTreeMatcher {
    private static final long serialVersionUID = 1L;
    private static final String DELETION_MARKER = "ToBeDeleted";

    @Override
    public Description matchAnnotation(AnnotationTree tree, VisitorState state) {
      return match(
          tree,
          ASTHelpers.getAnnotationMirror(tree).getAnnotationType().asElement().getSimpleName(),
          state);
    }

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      return match(tree, tree.getName(), state);
    }

    private Description match(Tree tree, Name name, VisitorState state) {
      return name.toString().contains(DELETION_MARKER)
          ? describeMatch(tree, SourceCode.deleteWithTrailingWhitespace(tree, state))
          : Description.NO_MATCH;
    }
  }

  /**
   * A {@link BugChecker} that applies {@link
   * SourceCode#unwrapMethodInvocation(MethodInvocationTree, VisitorState)} to all method
   * invocations.
   */
  @BugPattern(severity = ERROR, summary = "Interacts with `SourceCode` for testing purposes")
  public static final class UnwrapMethodInvocationTestChecker extends BugChecker
      implements MethodInvocationTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
      return describeMatch(tree, SourceCode.unwrapMethodInvocation(tree, state));
    }
  }

  /**
   * A {@link BugChecker} that applies {@link
   * SourceCode#unwrapMethodInvocationDroppingWhitespaceAndComments(MethodInvocationTree,
   * VisitorState)} to all method invocations.
   */
  @BugPattern(severity = ERROR, summary = "Interacts with `SourceCode` for testing purposes")
  public static final class UnwrapMethodInvocationDroppingWhitespaceAndCommentsTestChecker
      extends BugChecker implements MethodInvocationTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
      return describeMatch(
          tree, SourceCode.unwrapMethodInvocationDroppingWhitespaceAndComments(tree, state));
    }
  }
}
