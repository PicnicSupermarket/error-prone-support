package tech.picnic.errorprone.documentation;

import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.toType;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;

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
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

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
public final class BugPatternTestExtractor implements Extractor<BugPatternTestExtractor.TestCases> {
  private static final Pattern TEST_CLASS_NAME_PATTERN = Pattern.compile("(.*)Test");
  private static final Matcher<Tree> JUNIT_TEST_METHOD =
      toType(MethodTree.class, hasAnnotation("org.junit.jupiter.api.Test"));

  /** Instantiates a new {@link BugPatternTestExtractor} instance. */
  public BugPatternTestExtractor() {}

  @Override
  public String identifier() {
    return "bugpattern-test";
  }

  @Override
  public Optional<BugPatternTestExtractor.TestCases> tryExtract(
      ClassTree tree, VisitorState state) {
    return getClassUnderTest(tree)
        .map(
            bugPatternName -> {
              CollectBugPatternTests scanner = new CollectBugPatternTests();

              for (Tree m : tree.getMembers()) {
                if (JUNIT_TEST_METHOD.matches(m, state)) {
                  scanner.scan(m, state);
                }
              }

              return scanner.getTests().isEmpty()
                  ? null
                  : new AutoValue_BugPatternTestExtractor_TestCases(
                      bugPatternName, scanner.getTests());
            });
  }

  private static Optional<String> getClassUnderTest(ClassTree tree) {
    return Optional.of(TEST_CLASS_NAME_PATTERN.matcher(tree.getSimpleName().toString()))
        .filter(java.util.regex.Matcher::matches)
        .map(m -> m.group(1));
  }

  private static final class CollectBugPatternTests
      extends TreeScanner<@Nullable Void, VisitorState> {
    private static final Matcher<ExpressionTree> TEST_HELPER_DO_TEST =
        instanceMethod()
            .onDescendantOfAny(
                "com.google.errorprone.CompilationTestHelper",
                "com.google.errorprone.BugCheckerRefactoringTestHelper")
            .named("doTest");
    private static final Matcher<ExpressionTree> TEST_HELPER_NEW_INSTANCE =
        staticMethod()
            .onDescendantOfAny(
                "com.google.errorprone.CompilationTestHelper",
                "com.google.errorprone.BugCheckerRefactoringTestHelper")
            .named("newInstance");
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
            .onDescendantOfAny(
                "com.google.errorprone.BugCheckerRefactoringTestHelper",
                "com.google.errorprone.BugCheckerRefactoringTestHelper.ExpectOutput")
            .namedAnyOf("addOutputLines", "expectUnchanged");

    private final List<TestCase> testCases = new ArrayList<>();

    public ImmutableList<TestCase> getTests() {
      return ImmutableList.copyOf(testCases);
    }

    @Override
    public @Nullable Void visitMethodInvocation(MethodInvocationTree node, VisitorState state) {
      List<TestEntry> entries = new ArrayList<>();

      if (TEST_HELPER_DO_TEST.matches(node, state)) {
        String classTestForMethod = getClassUnderTestForMethod(node, state);

        if (REPLACEMENT_DO_TEST.matches(node, state)) {
          extractReplacementTestCases(entries, node, state);
        } else {
          extractIdentificationTestCases(entries, node, state);
        }

        testCases.add(
            new AutoValue_BugPatternTestExtractor_TestCase(
                classTestForMethod, ImmutableList.copyOf(entries).reverse()));
      }

      return super.visitMethodInvocation(node, state);
    }

    private static String getClassUnderTestForMethod(
        MethodInvocationTree tree, VisitorState state) {
      MethodInvocationTree receiver = (MethodInvocationTree) ASTHelpers.getReceiver(tree);
      if (TEST_HELPER_NEW_INSTANCE.matches(receiver, state)) {
        return receiver.getArguments().get(0).toString().replace(".class", "");
      }
      return getClassUnderTestForMethod(receiver, state);
    }

    private static void extractIdentificationTestCases(
        List<TestEntry> result, MethodInvocationTree tree, VisitorState state) {
      MethodInvocationTree receiver = (MethodInvocationTree) ASTHelpers.getReceiver(tree);
      if (IDENTIFICATION_SOURCE_LINES.matches(receiver, state)) {
        result.add(
            new AutoValue_BugPatternTestExtractor_TestEntry(
                ASTHelpers.constValue(receiver.getArguments().get(0), String.class),
                getSourceCode(receiver).orElseThrow(),
                ""));
      }

      if (!TEST_HELPER_NEW_INSTANCE.matches(receiver, state)) {
        extractIdentificationTestCases(result, receiver, state);
      }
    }

    private static void extractReplacementTestCases(
        List<TestEntry> entries, MethodInvocationTree node, VisitorState state) {
      MethodInvocationTree receiver = (MethodInvocationTree) ASTHelpers.getReceiver(node);

      if (REPLACEMENT_OUTPUT_SOURCE_LINES.matches(receiver, state)) {
        MethodInvocationTree inputTree = (MethodInvocationTree) ASTHelpers.getReceiver(receiver);
        String inputLines = getSourceCode(inputTree).orElseThrow();

        String outputLines =
            REPLACEMENT_EXPECT_UNCHANGED.matches(receiver, state)
                ? inputLines
                : getSourceCode(receiver).orElseThrow();

        entries.add(
            new AutoValue_BugPatternTestExtractor_TestEntry(
                ASTHelpers.constValue(inputTree.getArguments().get(0), String.class),
                inputLines,
                outputLines));
      }

      if (!TEST_HELPER_NEW_INSTANCE.matches(receiver, state)) {
        extractReplacementTestCases(entries, receiver, state);
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
  abstract static class TestEntry {
    abstract String fileName();

    abstract String input();

    abstract String output();
  }

  @AutoValue
  abstract static class TestCase {
    abstract String classUnderTest();

    abstract ImmutableList<TestEntry> entries();
  }

  @AutoValue
  abstract static class TestCases {
    abstract String testClass();

    abstract ImmutableList<TestCase> testCases();
  }
}
