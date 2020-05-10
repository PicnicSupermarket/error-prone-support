package tech.picnic.errorprone.documentation;

import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static java.util.Objects.requireNonNull;
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
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.documentation.BugPatternTestExtractor.TestCases;

/**
 * An {@link Extractor} that describes how to extract data from classes that test a {@code
 * BugChecker}.
 */
// XXX: Handle other methods from `{BugCheckerRefactoring,Compilation}TestHelper`:
// - Indicate which custom arguments are specified, if any.
// - For replacement tests, indicate which `FixChooser` is used.
// - ... (We don't use all optional features; TBD what else to support.)
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
    BugPatternTestCollector collector = new BugPatternTestCollector();

    collector.scan(tree, state);

    return Optional.of(collector.getCollectedTests())
        .filter(not(ImmutableList::isEmpty))
        .map(
            tests ->
                new AutoValue_BugPatternTestExtractor_TestCases(
                    ASTHelpers.getSymbol(tree).className(), tests));
  }

  private static final class BugPatternTestCollector
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
            .withParameters(Class.class.getCanonicalName(), Class.class.getCanonicalName());
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

    private ImmutableList<TestCase> getCollectedTests() {
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

                  if (!entries.isEmpty()) {
                    collectedTestCases.add(
                        new AutoValue_BugPatternTestExtractor_TestCase(
                            classUnderTest, ImmutableList.copyOf(entries).reverse()));
                  }
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
        String path = ASTHelpers.constValue(tree.getArguments().get(0), String.class);
        Optional<String> sourceCode =
            getSourceCode(tree).filter(s -> s.contains("// BUG: Diagnostic"));
        if (path != null && sourceCode.isPresent()) {
          sink.add(
              new AutoValue_BugPatternTestExtractor_IdentificationTestEntry(
                  path, sourceCode.orElseThrow()));
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
        /*
         * Retrieve the method invocation that contains the input source code. Note that this cast
         * is safe, because this code is guarded by an earlier call to `#getClassUnderTest(..)`,
         * which ensures that `tree` is part of a longer method invocation chain.
         */
        MethodInvocationTree inputTree = (MethodInvocationTree) requireNonNull(ASTHelpers.getReceiver(tree));

        String path = ASTHelpers.constValue(inputTree.getArguments().get(0), String.class);
        Optional<String> inputCode = getSourceCode(inputTree);
        if (path != null && inputCode.isPresent()) {
          Optional<String> outputCode =
              REPLACEMENT_EXPECT_UNCHANGED.matches(tree, state) ? inputCode : getSourceCode(tree);

          if (outputCode.isPresent() && !inputCode.equals(outputCode)) {
            sink.add(
                new AutoValue_BugPatternTestExtractor_ReplacementTestEntry(
                    path, inputCode.orElseThrow(), outputCode.orElseThrow()));
          }
        }
      }

      ExpressionTree receiver = ASTHelpers.getReceiver(tree);
      if (receiver instanceof MethodInvocationTree) {
        extractReplacementTestCases((MethodInvocationTree) receiver, sink, state);
      }
    }

    // XXX: This logic is duplicated in `ErrorProneTestSourceFormat`. Can we do better?
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
    String path();
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
