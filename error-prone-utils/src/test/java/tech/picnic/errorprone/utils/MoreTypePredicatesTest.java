package tech.picnic.errorprone.utils;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.Matchers.typePredicateMatcher;
import static com.google.errorprone.predicates.TypePredicates.isExactType;
import static tech.picnic.errorprone.utils.MoreTypes.generic;
import static tech.picnic.errorprone.utils.MoreTypes.subOf;
import static tech.picnic.errorprone.utils.MoreTypes.type;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.predicates.TypePredicate;
import com.google.errorprone.suppliers.Supplier;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import org.junit.jupiter.api.Test;

final class MoreTypePredicatesTest {
  @Test
  void hasAnnotation() {
    CompilationTestHelper.newInstance(HasAnnotationTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  class NoAnnotation {}",
            "",
            "  @SuppressWarnings(\"foo\")",
            "  interface OtherAnnotation {}",
            "",
            "  @Deprecated",
            "  // BUG: Diagnostic contains:",
            "  class FlaggedAnnotation {}",
            "",
            "  @Deprecated",
            "  @SuppressWarnings(\"foo\")",
            "  // BUG: Diagnostic contains:",
            "  class MultipleAnnotations {}",
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

  @Test
  void hasTypeParameter() {
    CompilationTestHelper.newInstance(HasTypeParameterTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableMap;",
            "import com.google.common.collect.ImmutableSet;",
            "",
            "class A {",
            "  void m() {",
            "    \"foo\".toString();",
            "    ImmutableList.of(1);",
            "    ImmutableSet.of(1);",
            "    ImmutableMap.of(1, \"foo\");",
            "",
            "    // BUG: Diagnostic contains:",
            "    ImmutableList.of(\"bar\");",
            "    // BUG: Diagnostic contains:",
            "    ImmutableSet.of(\"baz\");",
            "    // BUG: Diagnostic contains:",
            "    ImmutableMap.of(\"qux\", 1);",
            "  }",
            "}")
        .doTest();
  }

  /** A {@link BugChecker} that delegates to {@link MoreTypePredicates#hasAnnotation(String)}. */
  @BugPattern(
      summary = "Interacts with `MoreTypePredicates` for testing purposes",
      severity = ERROR)
  private static final class HasAnnotationTestChecker extends BugChecker
      implements ClassTreeMatcher {
    private static final long serialVersionUID = 1L;
    private static final Matcher<Tree> DELEGATE =
        typePredicateMatcher(MoreTypePredicates.hasAnnotation(Deprecated.class.getCanonicalName()));

    @Override
    public Description matchClass(ClassTree tree, VisitorState state) {
      return DELEGATE.matches(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
    }
  }

  /** A {@link BugChecker} that delegates to {@link MoreTypePredicates#isSubTypeOf(Supplier)}. */
  @BugPattern(
      summary = "Interacts with `MoreTypePredicates` for testing purposes",
      severity = ERROR)
  private static final class IsSubTypeOfTestChecker extends BugChecker
      implements MethodInvocationTreeMatcher {
    private static final long serialVersionUID = 1L;
    private static final Matcher<Tree> DELEGATE =
        typePredicateMatcher(
            MoreTypePredicates.isSubTypeOf(
                generic(
                    type(ImmutableSet.class.getCanonicalName()),
                    subOf(type(Number.class.getCanonicalName())))));

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
      return DELEGATE.matches(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
    }
  }

  /**
   * A {@link BugChecker} that delegates to {@link MoreTypePredicates#hasTypeParameter(int,
   * TypePredicate)}.
   */
  @BugPattern(
      summary = "Interacts with `MoreTypePredicates` for testing purposes",
      severity = ERROR)
  private static final class HasTypeParameterTestChecker extends BugChecker
      implements MethodInvocationTreeMatcher {
    private static final long serialVersionUID = 1L;
    private static final Matcher<Tree> DELEGATE =
        typePredicateMatcher(
            MoreTypePredicates.hasTypeParameter(0, isExactType(String.class.getCanonicalName())));

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
      return DELEGATE.matches(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
    }
  }
}
