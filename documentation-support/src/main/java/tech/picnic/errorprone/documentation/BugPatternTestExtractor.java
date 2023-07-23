package tech.picnic.errorprone.documentation;

import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static java.util.function.Predicate.not;

import com.google.auto.service.AutoService;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.TreeScanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.documentation.BugPatternTestExtractor.TestCases;

/**
 * An {@link Extractor} that describes how to extract data from classes that test a {@code
 * BugChecker}.
 */
// XXX: Consider whether to omit or handle differently identification tests without `// BUG:
// Diagnostic (contains|matches)` markers.
// XXX: Handle other methods from `{BugCheckerRefactoring,Compilation}TestHelper`.
@Immutable
@AutoService(Extractor.class)
@SuppressWarnings("rawtypes" /* See https://github.com/google/auto/issues/870. */)
public final class BugPatternTestExtractor implements Extractor<TestCases> {
  /** Instantiates a new {@link BugPatternTestExtractor} instance. */
  public BugPatternTestExtractor() {}

  @Override
  public String identifier() {
    return "bugpattern-test";
  }

  @Override
  public Optional<TestCases> tryExtract(ClassTree tree, VisitorState state) {
    CollectBugPatternTests scanner = new CollectBugPatternTests();

    scanner.scan(tree, state);

    return Optional.of(scanner.getCollectedTests())
        .filter(not(ImmutableList::isEmpty))
        .map(
            tests ->
                new AutoValue_BugPatternTestExtractor_TestCases(
                    ASTHelpers.getSymbol(tree).className(), tests));
  }

  private static final class CollectBugPatternTests
      extends TreeScanner<@Nullable Void, VisitorState> {
    private static final Matcher<ExpressionTree> COMPILATION_HELPER_DO_TEST =
        instanceMethod()
            .onDescendantOf("com.google.errorprone.CompilationTestHelper")
            .named("doTest");
    private static final Matcher<ExpressionTree> TEST_HELPER_NEW_INSTANCE =
        staticMethod()
            .onDescendantOfAny(
                "com.google.errorprone.CompilationTestHelper",
                "com.google.errorprone.BugCheckerRefactoringTestHelper")
            .named("newInstance")
            .withParameters("java.lang.Class", "java.lang.Class");
    private static final Matcher<ExpressionTree> IDENTIFICATION_SOURCE_LINES =
        instanceMethod()
            .onDescendantOf("com.google.errorprone.CompilationTestHelper")
            .named("addSourceLines");
    private static final Matcher<ExpressionTree> REPLACEMENT_DO_TEST =
        instanceMethod()
            .onDescendantOf("com.google.errorprone.BugCheckerRefactoringTestHelper")
            .named("doTest");
    private static final Matcher<ExpressionTree> REPLACEMENT_EXPECT_UNCHANGED =
        instanceMethod()
            .onDescendantOf("com.google.errorprone.BugCheckerRefactoringTestHelper.ExpectOutput")
            .named("expectUnchanged");
    private static final Matcher<ExpressionTree> REPLACEMENT_OUTPUT_SOURCE_LINES =
        instanceMethod()
            .onDescendantOf("com.google.errorprone.BugCheckerRefactoringTestHelper.ExpectOutput")
            .namedAnyOf("addOutputLines", "expectUnchanged");

    private final List<TestCase> collectedTestCases = new ArrayList<>();

    public ImmutableList<TestCase> getCollectedTests() {
      return ImmutableList.copyOf(collectedTestCases);
    }

    @Override
    public @Nullable Void visitMethodInvocation(MethodInvocationTree node, VisitorState state) {
      boolean isReplacementTest = REPLACEMENT_DO_TEST.matches(node, state);
      if (isReplacementTest || COMPILATION_HELPER_DO_TEST.matches(node, state)) {
        getClassUnderTest(node, state)
            .ifPresent(
                classUnderTest -> {
                  List<TestEntry> entries = new ArrayList<>();
                  if (isReplacementTest) {
                    extractReplacementTestCases(node, entries, state);
                  } else {
                    extractIdentificationTestCases(node, entries, state);
                  }

                  collectedTestCases.add(
                      new AutoValue_BugPatternTestExtractor_TestCase(
                          classUnderTest, ImmutableList.copyOf(entries).reverse()));
                });
      }

      return super.visitMethodInvocation(node, state);
    }

    private static Optional<String> getClassUnderTest(
        MethodInvocationTree tree, VisitorState state) {
      if (TEST_HELPER_NEW_INSTANCE.matches(tree, state)) {
        return Optional.ofNullable(ASTHelpers.getSymbol(tree.getArguments().get(0)))
            .filter(s -> !s.type.allparams().isEmpty())
            .map(s -> s.type.allparams().get(0).tsym.getQualifiedName().toString());
      }

      ExpressionTree receiver = ASTHelpers.getReceiver(tree);
      return receiver instanceof MethodInvocationTree
          ? getClassUnderTest((MethodInvocationTree) receiver, state)
          : Optional.empty();
    }

    private static void extractIdentificationTestCases(
        MethodInvocationTree tree, List<TestEntry> sink, VisitorState state) {
      if (IDENTIFICATION_SOURCE_LINES.matches(tree, state)) {
        // XXX: Test the case where this code isn't a constant.
        String fileName = ASTHelpers.constValue(tree.getArguments().get(0), String.class);
        // XXX: Test the case where this code isn't a constant.
        Optional<String> sourceCode = getSourceCode(tree);
        if (fileName != null && sourceCode.isPresent()) {
          sink.add(
              new AutoValue_BugPatternTestExtractor_IdentificationTestEntry(
                  // XXX: Handle the case where this isn't a constant.
                  ASTHelpers.constValue(tree.getArguments().get(0), String.class),
                  sourceCode.orElseThrow()));
        }
      }

      ExpressionTree receiver = ASTHelpers.getReceiver(tree);
      if (receiver instanceof MethodInvocationTree) {
        extractIdentificationTestCases((MethodInvocationTree) receiver, sink, state);
      }
    }

    private static void extractReplacementTestCases(
        MethodInvocationTree tree, List<TestEntry> sink, VisitorState state) {
      if (REPLACEMENT_OUTPUT_SOURCE_LINES.matches(tree, state)) {
        // XXX: Add test case for when this isn't a `MethodInvocationTree`?
        // Answer: yes, because then the `.getArguments().get(0)` call below is also safe. So test
        // against `addInputLines`.
        MethodInvocationTree inputTree = (MethodInvocationTree) ASTHelpers.getReceiver(tree);

        // XXX: Test the case where this isn't a constant.
        String fileName = ASTHelpers.constValue(inputTree.getArguments().get(0), String.class);
        // XXX: Test the case where this isn't a constant.
        Optional<String> inputCode = getSourceCode(inputTree);
        if (fileName != null && inputCode.isPresent()) {
          // XXX: Test the case where this isn't a constant.
          Optional<String> outputCode =
              REPLACEMENT_EXPECT_UNCHANGED.matches(tree, state) ? inputCode : getSourceCode(tree);

          if (outputCode.isPresent()) {
            sink.add(
                new AutoValue_BugPatternTestExtractor_ReplacementTestEntry(
                    fileName, inputCode.orElseThrow(), outputCode.orElseThrow()));
          }
        }
      }

      ExpressionTree receiver = ASTHelpers.getReceiver(tree);
      if (receiver instanceof MethodInvocationTree) {
        extractReplacementTestCases((MethodInvocationTree) receiver, sink, state);
      }
    }

    // XXX: Duplicated from `ErrorProneTestSourceFormat`. Can we do better?
    private static Optional<String> getSourceCode(MethodInvocationTree tree) {
      List<? extends ExpressionTree> sourceLines =
          tree.getArguments().subList(1, tree.getArguments().size());
      StringBuilder source = new StringBuilder();

      for (ExpressionTree sourceLine : sourceLines) {
        String value = ASTHelpers.constValue(sourceLine, String.class);
        if (value == null) {
          return Optional.empty();
        }
        source.append(value).append('\n');
      }

      return Optional.of(source.toString());
    }
  }

  @AutoValue
  abstract static class TestCases {
    abstract String testClass();

    abstract ImmutableList<TestCase> testCases();
  }

  @AutoValue
  abstract static class TestCase {
    abstract String classUnderTest();

    abstract ImmutableList<TestEntry> entries();
  }

  interface TestEntry {
    String fileName();
  }

  @AutoValue
  abstract static class ReplacementTestEntry implements TestEntry {
    abstract String input();

    abstract String output();
  }

  @AutoValue
  abstract static class IdentificationTestEntry implements TestEntry {
    abstract String code();
  }
}
