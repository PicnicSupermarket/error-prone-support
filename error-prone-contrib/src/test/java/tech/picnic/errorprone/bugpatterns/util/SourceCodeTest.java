package tech.picnic.errorprone.bugpatterns.util;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import javax.lang.model.element.Name;
import org.junit.jupiter.api.Test;

final class SourceCodeTest {
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(TestChecker.class, getClass());

  @Test
  void deleteWithTrailingWhitespaceAnnotations() {
    refactoringTestHelper
        .addInputLines("AnnotationToBeDeleted.java", "@interface AnnotationToBeDeleted {}")
        .expectUnchanged()
        .addInputLines(
            "AnotherAnnotationToBeDeleted.java", "@interface AnotherAnnotationToBeDeleted {}")
        .expectUnchanged()
        .addInputLines(
            "AnnotationDeletions.java",
            "",
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
            "",
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
    refactoringTestHelper
        .addInputLines(
            "MethodDeletions.java",
            "",
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
            "",
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

  /**
   * Uses {@link SourceCode#deleteWithTrailingWhitespace(Tree, VisitorState)} to suggest the
   * deletion of annotations and methods with a name containing {@value DELETION_MARKER}.
   */
  @BugPattern(severity = ERROR, summary = "Interacts with `SourceCode` for testing purposes")
  public static final class TestChecker extends BugChecker
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
}
