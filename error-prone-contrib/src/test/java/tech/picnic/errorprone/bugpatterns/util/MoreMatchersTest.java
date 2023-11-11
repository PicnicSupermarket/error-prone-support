package tech.picnic.errorprone.bugpatterns.util;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.generic;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.subOf;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.type;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.suppliers.Supplier;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import org.junit.jupiter.api.Test;

final class MoreMatchersTest {
  @Test
  void hasMetaAnnotation() {
    CompilationTestHelper.newInstance(HasMetaAnnotationTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import org.junit.jupiter.api.AfterAll;",
            "import org.junit.jupiter.api.RepeatedTest;",
            "import org.junit.jupiter.api.Test;",
            "import org.junit.jupiter.api.TestTemplate;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "",
            "class A {",
            "  void negative1() {}",
            "",
            "  @Test",
            "  void negative2() {}",
            "",
            "  @AfterAll",
            "  void negative3() {}",
            "",
            "  @TestTemplate",
            "  void negative4() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @ParameterizedTest",
            "  void positive1() {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  @RepeatedTest(2)",
            "  void positive2() {}",
            "}")
        .doTest();
  }

  @Test
  void isSubTypeOf() {
    CompilationTestHelper.newInstance(IsSubTypeOfTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableSet;",
            "import com.google.common.collect.ImmutableSortedSet;",
            "",
            "class A {",
            "  void m() {",
            "    ImmutableSet.of(\"foo\");",
            "    ImmutableSortedSet.of(\"foo\");",
            "    ImmutableList.of(\"foo\");",
            "    ImmutableList.of(1);",
            "    ImmutableList.of(1.0);",
            "    ImmutableList.of((Number) 1);",
            "",
            "    // BUG: Diagnostic contains:",
            "    ImmutableSet.of(1);",
            "    // BUG: Diagnostic contains:",
            "    ImmutableSet.of(1.0);",
            "    // BUG: Diagnostic contains:",
            "    ImmutableSet.of((Number) 1);",
            "    // BUG: Diagnostic contains:",
            "    ImmutableSortedSet.of(1);",
            "    // BUG: Diagnostic contains:",
            "    ImmutableSortedSet.of(1.0);",
            "    // BUG: Diagnostic contains:",
            "    ImmutableSortedSet.of((Number) 1);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void isSubTypeOfBoundTypeUnknown() {
    CompilationTestHelper.newInstance(IsSubTypeOfTestChecker.class, getClass())
        .withClasspath()
        .addSourceLines(
            "A.java",
            "class A {",
            "  void m() {",
            "    System.out.println(toString());",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} that delegates to {@link MoreMatchers#hasMetaAnnotation(String)}. */
  @BugPattern(summary = "Interacts with `MoreMatchers` for testing purposes", severity = ERROR)
  public static final class HasMetaAnnotationTestChecker extends BugChecker
      implements AnnotationTreeMatcher {
    private static final long serialVersionUID = 1L;
    private static final Matcher<AnnotationTree> DELEGATE =
        MoreMatchers.hasMetaAnnotation("org.junit.jupiter.api.TestTemplate");

    @Override
    public Description matchAnnotation(AnnotationTree tree, VisitorState state) {
      return DELEGATE.matches(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
    }
  }

  /** A {@link BugChecker} that delegates to {@link MoreMatchers#isSubTypeOf(Supplier)}. */
  @BugPattern(summary = "Interacts with `MoreMatchers` for testing purposes", severity = ERROR)
  public static final class IsSubTypeOfTestChecker extends BugChecker
      implements MethodInvocationTreeMatcher {
    private static final long serialVersionUID = 1L;
    private static final Matcher<Tree> DELEGATE =
        MoreMatchers.isSubTypeOf(
            generic(
                type(ImmutableSet.class.getCanonicalName()),
                subOf(type(Number.class.getCanonicalName()))));

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
      return DELEGATE.matches(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
    }
  }
}
