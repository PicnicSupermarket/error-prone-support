package tech.picnic.errorprone.utils;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ExpressionStatementTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.LambdaExpressionTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.ReturnTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.util.List;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;

final class MoreASTHelpersTest {
  @Test
  void findMethods() {
    CompilationTestHelper.newInstance(FindMethodsTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  // BUG: Diagnostic contains: {foo=1, bar=2, baz=0}",
            "  void foo() {}",
            "",
            "  // BUG: Diagnostic contains: {foo=1, bar=2, baz=0}",
            "  void bar() {}",
            "",
            "  // BUG: Diagnostic contains: {foo=1, bar=2, baz=0}",
            "  void bar(int i) {}",
            "",
            "  static class B {",
            "    // BUG: Diagnostic contains: {foo=0, bar=1, baz=1}",
            "    void bar() {}",
            "",
            "    // BUG: Diagnostic contains: {foo=0, bar=1, baz=1}",
            "    void baz() {}",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void methodExistsInEnclosingClass() {
    CompilationTestHelper.newInstance(MethodExistsTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  // BUG: Diagnostic contains: {foo=true, bar=true, baz=false}",
            "  void foo() {}",
            "",
            "  // BUG: Diagnostic contains: {foo=true, bar=true, baz=false}",
            "  void bar() {}",
            "",
            "  // BUG: Diagnostic contains: {foo=true, bar=true, baz=false}",
            "  void bar(int i) {}",
            "",
            "  static class B {",
            "    // BUG: Diagnostic contains: {foo=false, bar=true, baz=true}",
            "    void bar() {}",
            "",
            "    // BUG: Diagnostic contains: {foo=false, bar=true, baz=true}",
            "    void baz() {}",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void findMethodExitedOnReturn() {
    CompilationTestHelper.newInstance(FindMethodExitedOnReturnTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  {",
            "    toString();",
            "  }",
            "",
            "  String topLevelMethod() {",
            "    // BUG: Diagnostic contains: topLevelMethod",
            "    toString();",
            "    // BUG: Diagnostic contains: topLevelMethod",
            "    return toString();",
            "  }",
            "",
            "  Stream<String> anotherMethod() {",
            "    // BUG: Diagnostic contains: anotherMethod",
            "    return Stream.of(1)",
            "        .map(n -> {",
            "          toString();",
            "          return toString();",
            "        });",
            "  }",
            "",
            "  void recursiveMethod(Runnable r) {",
            "    // BUG: Diagnostic contains: recursiveMethod",
            "    recursiveMethod(",
            "        new Runnable() {",
            "          @Override",
            "          public void run() {",
            "            // BUG: Diagnostic contains: run",
            "            toString();",
            "          }",
            "        });",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void findResultExpressions() {
    CompilationTestHelper.newInstance(FindResultExpressionsTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.function.Supplier;",
            "",
            "abstract class A {",
            "  // BUG: Diagnostic contains: [\"foo\"]",
            "  Supplier<String> expressionBody = () -> (\"foo\");",
            "",
            "  // BUG: Diagnostic contains: []",
            "  abstract int abstractMethod();",
            "",
            "  // BUG: Diagnostic contains: []",
            "  void voidMethod() {",
            "    return;",
            "  }",
            "",
            "  // BUG: Diagnostic contains: [1, 2]",
            "  int multipleReturns(boolean b) {",
            "    if (b) {",
            "      return (1);",
            "    }",
            "    return 2;",
            "  }",
            "",
            "  // BUG: Diagnostic contains: [1, 2]",
            "  int switchStatement(int i) {",
            "    switch (i) {",
            "      case 0:",
            "        return 1;",
            "      default:",
            "        return 2;",
            "    }",
            "  }",
            "",
            "  // BUG: Diagnostic contains: [s]",
            "  Supplier<String> nestedScopes() {",
            "    Supplier<String> s =",
            "        // BUG: Diagnostic contains: [\"bar\"]",
            "        () -> {",
            "          Object o =",
            "              new Object() {",
            "                @Override",
            "                // BUG: Diagnostic contains: [\"baz\"]",
            "                public String toString() {",
            "                  return \"baz\";",
            "                }",
            "              };",
            "          Supplier<String> t =",
            "              // BUG: Diagnostic contains: [\"qux\"]",
            "              () -> {",
            "                return \"qux\";",
            "              };",
            "          return \"bar\";",
            "        };",
            "    return s;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void findBranchExpressions() {
    CompilationTestHelper.newInstance(FindBranchExpressionsTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  int plain() {",
            "    // BUG: Diagnostic contains: [1]",
            "    return (1);",
            "  }",
            "",
            "  int conditional(boolean b, boolean c) {",
            "    // BUG: Diagnostic contains: [1, 2, 3]",
            "    return b ? (1) : (c ? 2 : 3);",
            "  }",
            "",
            "  int switchExpression(int i) {",
            "    // XXX: Ideally the switch expression's result expressions are reported instead.",
            "    // BUG: Diagnostic contains: [switch",
            "    return switch (i) {",
            "      case 0 -> 1;",
            "      default -> 2;",
            "    };",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void areSameType() {
    CompilationTestHelper.newInstance(AreSameTypeTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  void negative1(String a, Integer b) {}",
            "",
            "  void negative2(Integer a, Number b) {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  void positive1(String a, String b) {}",
            "",
            "  // BUG: Diagnostic contains:",
            "  void positive2(Iterable<String> a, Iterable<Integer> b) {}",
            "}")
        .doTest();
  }

  /**
   * An abstract {@link BugChecker} that provides helper functions for the construction of {@link
   * Description}s against which test cases can match.
   */
  private abstract static class TestChecker extends BugChecker {
    private static final long serialVersionUID = 1L;

    <T> Description reportSearchResults(
        Tree tree, BiFunction<String, VisitorState, T> searchFunction, VisitorState state) {
      return report(
          tree,
          Maps.toMap(ImmutableSet.of("foo", "bar", "baz"), key -> searchFunction.apply(key, state))
              .toString());
    }

    Description reportSources(
        Tree tree, ImmutableList<? extends Tree> subTrees, VisitorState state) {
      return report(
          tree,
          subTrees.stream().map(e -> SourceCode.treeToString(e, state)).collect(toImmutableList()));
    }

    <T> Description report(Tree tree, T value) {
      return buildDescription(tree).setMessage(value.toString()).build();
    }
  }

  /**
   * A {@link BugChecker} that delegates to {@link MoreASTHelpers#findMethods(CharSequence,
   * VisitorState)}.
   */
  @BugPattern(summary = "Interacts with `MoreASTHelpers` for testing purposes", severity = ERROR)
  private static final class FindMethodsTestChecker extends TestChecker
      implements MethodTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      return reportSearchResults(
          tree, (methodName, s) -> MoreASTHelpers.findMethods(methodName, s).size(), state);
    }
  }

  /**
   * A {@link BugChecker} that delegates to {@link
   * MoreASTHelpers#methodExistsInEnclosingClass(CharSequence, VisitorState)}.
   */
  @BugPattern(summary = "Interacts with `MoreASTHelpers` for testing purposes", severity = ERROR)
  private static final class MethodExistsTestChecker extends TestChecker
      implements MethodTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      return reportSearchResults(tree, MoreASTHelpers::methodExistsInEnclosingClass, state);
    }
  }

  /**
   * A {@link BugChecker} that delegates to {@link
   * MoreASTHelpers#findMethodExitedOnReturn(VisitorState)}.
   */
  @BugPattern(summary = "Interacts with `MoreASTHelpers` for testing purposes", severity = ERROR)
  private static final class FindMethodExitedOnReturnTestChecker extends TestChecker
      implements ExpressionStatementTreeMatcher, ReturnTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchExpressionStatement(ExpressionStatementTree tree, VisitorState state) {
      return flagMethodReturnLocation(tree, state);
    }

    @Override
    public Description matchReturn(ReturnTree tree, VisitorState state) {
      return flagMethodReturnLocation(tree, state);
    }

    private Description flagMethodReturnLocation(Tree tree, VisitorState state) {
      return MoreASTHelpers.findMethodExitedOnReturn(state)
          .map(m -> report(tree, m.getName()))
          .orElse(Description.NO_MATCH);
    }
  }

  /**
   * A {@link BugChecker} that delegates to {@link
   * MoreASTHelpers#findResultExpressions(LambdaExpressionTree)} and {@link
   * MoreASTHelpers#findResultExpressions(MethodTree)}.
   */
  @BugPattern(summary = "Interacts with `MoreASTHelpers` for testing purposes", severity = ERROR)
  private static final class FindResultExpressionsTestChecker extends TestChecker
      implements LambdaExpressionTreeMatcher, MethodTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchLambdaExpression(LambdaExpressionTree tree, VisitorState state) {
      return reportSources(tree, MoreASTHelpers.findResultExpressions(tree), state);
    }

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      return reportSources(tree, MoreASTHelpers.findResultExpressions(tree), state);
    }
  }

  /**
   * A {@link BugChecker} that delegates to {@link
   * MoreASTHelpers#findBranchExpressions(ExpressionTree)}.
   */
  @BugPattern(summary = "Interacts with `MoreASTHelpers` for testing purposes", severity = ERROR)
  private static final class FindBranchExpressionsTestChecker extends TestChecker
      implements ReturnTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchReturn(ReturnTree tree, VisitorState state) {
      return reportSources(
          tree, MoreASTHelpers.findBranchExpressions(requireNonNull(tree.getExpression())), state);
    }
  }

  /**
   * A {@link BugChecker} that delegates to {@link MoreASTHelpers#areSameType(Tree, Tree,
   * VisitorState)}.
   */
  @BugPattern(summary = "Interacts with `MoreASTHelpers` for testing purposes", severity = ERROR)
  private static final class AreSameTypeTestChecker extends BugChecker
      implements MethodTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      List<? extends VariableTree> parameters = tree.getParameters();
      return parameters.stream()
              .skip(1)
              .allMatch(p -> MoreASTHelpers.areSameType(p, parameters.getFirst(), state))
          ? describeMatch(tree)
          : Description.NO_MATCH;
    }
  }
}
