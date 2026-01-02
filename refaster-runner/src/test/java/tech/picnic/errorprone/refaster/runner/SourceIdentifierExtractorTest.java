package tech.picnic.errorprone.refaster.runner;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.CompilationUnitTree;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class SourceIdentifierExtractorTest {
  @Test
  void simpleMethodWithMethodCall() {
    CompilationTestHelper.newInstance(SourceIdentifierExtractorTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains: [toString]",
            "class A {",
            "  String foo() {",
            "    return toString();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void methodWithBinaryOperator() {
    CompilationTestHelper.newInstance(SourceIdentifierExtractorTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains: [+, a, b]",
            "class A {",
            "  int add(int a, int b) {",
            "    return a + b;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void methodWithUnaryOperator() {
    CompilationTestHelper.newInstance(SourceIdentifierExtractorTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains: [++x, a]",
            "class A {",
            "  int increment(int a) {",
            "    return ++a;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void methodWithMemberSelect() {
    CompilationTestHelper.newInstance(SourceIdentifierExtractorTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains: [System, out, println]",
            "class A {",
            "  void print() {",
            "    System.out.println();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void methodWithMemberReference() {
    CompilationTestHelper.newInstance(SourceIdentifierExtractorTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains: [<init>, Object, Supplier, function, java, util]",
            "import java.util.function.Supplier;",
            "",
            "class A {",
            "  Supplier<Object> getSupplier() {",
            "    return Object::new;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void methodWithAssignment() {
    CompilationTestHelper.newInstance(SourceIdentifierExtractorTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains: [=, x]",
            "class A {",
            "  void assign() {",
            "    int x;",
            "    x = 5;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void methodWithCompoundAssignment() {
    CompilationTestHelper.newInstance(SourceIdentifierExtractorTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains: [+=, x]",
            "class A {",
            "  void compound(int x) {",
            "    x += 5;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void methodWithMultipleOperations() {
    CompilationTestHelper.newInstance(SourceIdentifierExtractorTestChecker.class, getClass())
        .addSourceLines(
            "A.java",
            "// BUG: Diagnostic contains: [&&, <, >, a, b]",
            "class A {",
            "  boolean compare(int a, int b) {",
            "    return a > 0 && b < 10;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void emptyMethod() {
    CompilationTestHelper.newInstance(SourceIdentifierExtractorTestChecker.class, getClass())
        .addSourceLines(
            "A.java", "// BUG: Diagnostic contains: []", "class A {", "  void empty() {}", "}")
        .doTest();
  }

  /**
   * A {@link BugChecker} that extracts identifiers from a {@link CompilationUnitTree} and reports
   * them as a diagnostic message.
   */
  @BugPattern(
      summary = "Interacts with `SourceIdentifierExtractor` for testing purposes",
      severity = ERROR)
  public static final class SourceIdentifierExtractorTestChecker extends BugChecker
      implements CompilationUnitTreeMatcher {
    private static final long serialVersionUID = 1L;

    @Override
    public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
      Set<String> identifiers = SourceIdentifierExtractor.extractIdentifiers(tree);
      ImmutableSet<String> sortedIdentifiers =
          identifiers.stream().sorted().collect(toImmutableSet());
      return buildDescription(tree).setMessage(sortedIdentifiers.toString()).build();
    }
  }
}
