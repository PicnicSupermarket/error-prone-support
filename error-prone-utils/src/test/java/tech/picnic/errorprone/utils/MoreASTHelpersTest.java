package tech.picnic.errorprone.utils;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ExpressionStatementTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.ReturnTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.MethodInvocationTree;
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
    CompilationTestHelper.newInstance(FndMethodExitedOnReturnTestChecker.class, getClass())
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
            "        .map(",
            "            n -> {",
            "              toString();",
            "              return toString();",
            "            });",
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
  void findDirectReturnStatements() {
    CompilationTestHelper.newInstance(FindDirectReturnStatementsTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  // BUG: Diagnostic contains: 0: []",
            "  A() {}",
            "",
            "  // BUG: Diagnostic contains: 0: []",
            "  void noReturn() {}",
            "",
            "  // BUG: Diagnostic contains: 1: [return;]",
            "  void voidReturn() {",
            "    return;",
            "  }",
            "",
            "  // BUG: Diagnostic contains: 1: [return 1;]",
            "  int intReturn() {",
            "    return 1;",
            "  }",
            "",
            "  // BUG: Diagnostic contains: 2: [return 1;, return 2;]",
            "  int branching(boolean b) {",
            "    if (b) {",
            "      return 1;",
            "    }",
            "    return 2;",
            "  }",
            "",
            "  // BUG: Diagnostic contains: 1: [return () -> {",
            "  Runnable withLambdaInline() {",
            "    return () -> {",
            "      return;",
            "    };",
            "  }",
            "",
            "  // BUG: Diagnostic contains: 1: [return stream;]",
            "  Stream<String> withLambdaSeparate() {",
            "    Stream<String> stream =",
            "        Stream.of(1)",
            "            .map(",
            "                n -> {",
            "                  return String.valueOf(n);",
            "                });",
            "    return stream;",
            "  }",
            "",
            "  // BUG: Diagnostic contains: 1: [return new Object() {",
            "  Object withAnonymousClassInline() {",
            "    return new Object() {",
            "      @Override",
            "      // BUG: Diagnostic contains: 1: [return \"anonymous\";]",
            "      public String toString() {",
            "        return \"anonymous\";",
            "      }",
            "    };",
            "  }",
            "",
            "  // BUG: Diagnostic contains: 1: [return object;]",
            "  Object withAnonymousClassSeparate() {",
            "    Object object = new Object() {",
            "      @Override",
            "      // BUG: Diagnostic contains: 1: [return \"anonymous\";]",
            "      public String toString() {",
            "        return \"anonymous\";",
            "      }",
            "    };",
            "    return object;",
            "  }",
            "",
            "  // BUG: Diagnostic contains: 1: [return null;]",
            "  Object withLocalClass() {",
            "    class LocalClass extends Object {",
            "      @Override",
            "      // BUG: Diagnostic contains: 1: [return \"anonymous\";]",
            "      public String toString() {",
            "        return \"anonymous\";",
            "      }",
            "    }",
            "    return null;",
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

  @Test
  void isStringTyped() {
    CompilationTestHelper.newInstance(IsStringTypedTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "class A {",
            "  void m() {",
            "    int foo = 1;",
            "    // BUG: Diagnostic contains:",
            "    String s = \"foo\";",
            "",
            "    hashCode();",
            "    // BUG: Diagnostic contains:",
            "    toString();",
            "  }",
            "}")
        .doTest();
  }

  private static String createMethodSearchDiagnosticsMessage(
      BiFunction<String, VisitorState, Object> valueFunction, VisitorState state) {
    return Maps.toMap(ImmutableSet.of("foo", "bar", "baz"), key -> valueFunction.apply(key, state))
        .toString();
  }

  /**
   * A {@link BugChecker} that delegates to {@link MoreASTHelpers#findMethods(CharSequence,
   * VisitorState)}.
   */
  @BugPattern(summary = "Interacts with `MoreASTHelpers` for testing purposes", severity = ERROR)
  private static final class FindMethodsTestChecker extends BugChecker
      implements MethodTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      return buildDescription(tree)
          .setMessage(
              createMethodSearchDiagnosticsMessage(
                  (methodName, s) -> MoreASTHelpers.findMethods(methodName, s).size(), state))
          .build();
    }
  }

  /**
   * A {@link BugChecker} that delegates to {@link
   * MoreASTHelpers#methodExistsInEnclosingClass(CharSequence, VisitorState)}.
   */
  @BugPattern(summary = "Interacts with `MoreASTHelpers` for testing purposes", severity = ERROR)
  private static final class MethodExistsTestChecker extends BugChecker
      implements MethodTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      return buildDescription(tree)
          .setMessage(
              createMethodSearchDiagnosticsMessage(
                  MoreASTHelpers::methodExistsInEnclosingClass, state))
          .build();
    }
  }

  /**
   * A {@link BugChecker} that delegates to {@link
   * MoreASTHelpers#findMethodExitedOnReturn(VisitorState)}.
   */
  @BugPattern(summary = "Interacts with `MoreASTHelpers` for testing purposes", severity = ERROR)
  private static final class FndMethodExitedOnReturnTestChecker extends BugChecker
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
          .map(m -> buildDescription(tree).setMessage(m.getName().toString()).build())
          .orElse(Description.NO_MATCH);
    }
  }

  /**
   * A {@link BugChecker} that delegates to {@link
   * MoreASTHelpers#findDirectReturnStatements(MethodTree)}.
   */
  @BugPattern(summary = "Interacts with `MoreASTHelpers` for testing purposes", severity = ERROR)
  private static final class FindDirectReturnStatementsTestChecker extends BugChecker
      implements MethodTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      ImmutableList<String> statements =
          MoreASTHelpers.findDirectReturnStatements(tree).stream()
              .map(s -> SourceCode.treeToString(s, state))
              .collect(toImmutableList());
      return buildDescription(tree)
          .setMessage("%s: %s".formatted(statements.size(), statements.toString()))
          .build();
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

  /**
   * A {@link BugChecker} that delegates to {@link MoreASTHelpers#isStringTyped(Tree,
   * VisitorState)}.
   */
  @BugPattern(summary = "Interacts with `MoreASTHelpers` for testing purposes", severity = ERROR)
  private static final class IsStringTypedTestChecker extends BugChecker
      implements MethodInvocationTreeMatcher, VariableTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
      return getDescription(tree, state);
    }

    @Override
    public Description matchVariable(VariableTree tree, VisitorState state) {
      return getDescription(tree, state);
    }

    private Description getDescription(Tree tree, VisitorState state) {
      return MoreASTHelpers.isStringTyped(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
    }
  }
}
