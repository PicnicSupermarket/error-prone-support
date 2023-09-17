package tech.picnic.errorprone.bugpatterns.util;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Name;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

final class SourceCodeTest {
  @Test
  void isLikelyAccurateSourceAvailable() {
    CompilationTestHelper.newInstance(IsLikelyAccurateSourceAvailableTestChecker.class, getClass())
        .setArgs("-processor", "lombok.launch.AnnotationProcessorHider$AnnotationProcessor")
        .addSourceLines(
            "A.java",
            "import com.fasterxml.jackson.annotation.JsonProperty;",
            "import lombok.Data;",
            "",
            "class A {",
                        "  class WithoutLombok {",
                        "    @JsonProperty(\"custom_field_name\")",
                        "    private String field;",
                        "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Data",
            "  class WithLombok {",
            "    // BUG: Diagnostic contains:",
            "    @JsonProperty(\"custom_field_name\")",
            "    private String field2;",
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
   * A {@link BugChecker} that uses {@link SourceCode#isLikelyAccurateSourceAvailable(VisitorState)}
   * to flag AST nodes for which accurate source code does not appear to be available.
   */
  @BugPattern(severity = ERROR, summary = "Interacts with `SourceCode` for testing purposes")
  public static final class IsLikelyAccurateSourceAvailableTestChecker extends BugChecker
      implements CompilationUnitTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
      Deque<Boolean> seenAccurateSource = new ArrayDeque<>();
      Set<Tree> maximalAccurateSubtrees = new LinkedHashSet<>();
      new TreeScanner<@Nullable Void, TreePath>() {
        @Override
        public @Nullable Void scan(Tree tree, TreePath treePath) {
          if (tree == null) {
            return null;
          }

          TreePath path = new TreePath(treePath, tree);
          boolean isAccurate = SourceCode.isLikelyAccurateSourceAvailable(state.withPath(path));
          if (!isAccurate) {
            assertThat(seenAccurateSource.peek()).isNotIn(Boolean.TRUE);
          } else if (!Boolean.TRUE.equals(seenAccurateSource.peek())) {
            maximalAccurateSubtrees.add(tree);
          }

          seenAccurateSource.push(isAccurate || Boolean.TRUE.equals(seenAccurateSource.peek()));
          try {
            return super.scan(tree, path);
          } finally {
            seenAccurateSource.pop();
          }
        }
      }.scan(tree, state.getPath());

      maximalAccurateSubtrees.stream().map(state::getSourceForNode).collect(Collectors.toList());

      return Description.NO_MATCH;
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
