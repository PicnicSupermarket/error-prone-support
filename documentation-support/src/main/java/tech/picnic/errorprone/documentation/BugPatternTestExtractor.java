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
@Immutable
@AutoService(Extractor.class)
@SuppressWarnings("rawtypes")
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
    private static final Matcher<ExpressionTree> REPLACEMENT_INPUT_SOURCE_LINES =
        instanceMethod()
            .onDescendantOf("com.google.errorprone.BugCheckerRefactoringTestHelper")
            .named("addInputLines");
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

    // XXX: Consider:
    // - Whether to omit or handle differently identification tests without `// BUG: Diagnostic
    //   (contains|matches)` markers.
    @Override
    public @Nullable Void visitMethodInvocation(MethodInvocationTree node, VisitorState state) {
      ArrayList<TestEntry> inputEntries = new ArrayList<>();
      ArrayList<TestEntry> outputEntries = new ArrayList<>();

      if (TEST_HELPER_DO_TEST.matches(node, state)) {
        String classTestForMethod = getClassTestForMethod(node, state);

        if (REPLACEMENT_DO_TEST.matches(node, state)) {
          extractReplacementTestCases(inputEntries, outputEntries, node, state);
        } else {
          extractIdentificationTestCases(inputEntries, node, state);
        }

        testCases.add(
            new AutoValue_BugPatternTestExtractor_TestCase(
                classTestForMethod,
                ImmutableList.copyOf(inputEntries),
                ImmutableList.copyOf(outputEntries)));
      }

      return super.visitMethodInvocation(node, state);
    }

    private static String getClassTestForMethod(MethodInvocationTree node, VisitorState state) {
      ExpressionTree receiver = ASTHelpers.getReceiver(node);
      if (TEST_HELPER_NEW_INSTANCE.matches(receiver, state)) {
        return ((MethodInvocationTree) receiver)
            .getArguments()
            .get(0)
            .toString()
            .replace(".class", "");
      }
      return getClassTestForMethod((MethodInvocationTree) receiver, state);
    }

    private static void extractIdentificationTestCases(
        List<TestEntry> result, MethodInvocationTree node, VisitorState state) {
      MethodInvocationTree receiver = (MethodInvocationTree) ASTHelpers.getReceiver(node);
      if (IDENTIFICATION_SOURCE_LINES.matches(receiver, state)) {
        result.add(
            new AutoValue_BugPatternTestExtractor_TestEntry(
                ASTHelpers.constValue(receiver.getArguments().get(0), String.class),
                getSourceCode(receiver).orElseThrow()));
      }

      if (!TEST_HELPER_NEW_INSTANCE.matches(receiver, state)) {
        extractIdentificationTestCases(result, receiver, state);
      }
    }

    private static void extractReplacementTestCases(
        List<TestEntry> inputEntries,
        ArrayList<TestEntry> outputEntries,
        MethodInvocationTree node,
        VisitorState state) {
      MethodInvocationTree receiver = (MethodInvocationTree) ASTHelpers.getReceiver(node);
      if (REPLACEMENT_INPUT_SOURCE_LINES.matches(receiver, state)) {
        inputEntries.add(
            new AutoValue_BugPatternTestExtractor_TestEntry(
                ASTHelpers.constValue(receiver.getArguments().get(0), String.class),
                getSourceCode(receiver).orElseThrow()));
      } else if (REPLACEMENT_OUTPUT_SOURCE_LINES.matches(receiver, state)) {
        // This is the `expectUnchanged` case, we should do something special here.
        if (receiver.getArguments().isEmpty()) {
          outputEntries.add(new AutoValue_BugPatternTestExtractor_TestEntry("", ""));
        } else {
          outputEntries.add(
              new AutoValue_BugPatternTestExtractor_TestEntry(
                  ASTHelpers.constValue(receiver.getArguments().get(0), String.class),
                  getSourceCode(receiver).orElseThrow()));
        }
      }

      if (!TEST_HELPER_NEW_INSTANCE.matches(receiver, state)) {
        extractReplacementTestCases(inputEntries, outputEntries, receiver, state);
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

    abstract String code();
  }

  @AutoValue
  abstract static class TestCase {
    abstract String classUnderTest();

    abstract ImmutableList<TestEntry> input();

    abstract ImmutableList<TestEntry> output();
  }

  @AutoValue
  abstract static class TestCases {
    abstract String testClass();

    abstract ImmutableList<TestCase> testCases();
  }
}
