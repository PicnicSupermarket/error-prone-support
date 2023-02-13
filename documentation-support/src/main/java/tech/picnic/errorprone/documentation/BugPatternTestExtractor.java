package tech.picnic.errorprone.documentation;

import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.util.Context;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.documentation.BugPatternTestExtractor.BugPatternTestDocumentation;

/**
 * An {@link Extractor} that describes how to extract data from a test that tests a {@code
 * BugChecker}.
 */
@Immutable
final class BugPatternTestExtractor implements Extractor<BugPatternTestDocumentation> {
  private static final Matcher<MethodTree> JUNIT_TEST_METHOD =
      hasAnnotation("org.junit.jupiter.api.Test");

  // XXX: Improve support for correctly extracting multiple sources from a single
  // `{BugCheckerRefactoring,Compilation}TestHelper` test.
  @Override
  public BugPatternTestDocumentation extract(ClassTree tree, Context context) {
    VisitorState state = VisitorState.createForUtilityPurposes(context);
    CollectBugPatternTests scanner = new CollectBugPatternTests(state);

    tree.getMembers().stream()
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast)
        .filter(m -> JUNIT_TEST_METHOD.matches(m, state))
        .forEach(m -> scanner.scan(m, null));

    String className = tree.getSimpleName().toString();
    return new AutoValue_BugPatternTestExtractor_BugPatternTestDocumentation(
        className.substring(0, className.lastIndexOf("Test")),
        scanner.getIdentificationTests(),
        scanner.getReplacementTests());
  }

  @Override
  public boolean canExtract(ClassTree tree, VisitorState state) {
    String className = tree.getSimpleName().toString();
    if (!className.endsWith("Test")) {
      return false;
    }

    ScanBugPatternTest scanBugPatternTest = new ScanBugPatternTest();
    scanBugPatternTest.scan(tree, state);

    String bugPatternName = className.substring(0, className.lastIndexOf("Test"));
    return scanBugPatternTest.hasTestUsingClassInstance(bugPatternName);
  }

  private static final class ScanBugPatternTest extends TreeScanner<@Nullable Void, VisitorState> {
    private static final Matcher<ExpressionTree> BUG_PATTERN_TEST_METHOD =
        staticMethod()
            .onDescendantOfAny(
                "com.google.errorprone.CompilationTestHelper",
                "com.google.errorprone.BugCheckerRefactoringTestHelper")
            .named("newInstance");

    private final List<String> encounteredClasses = new ArrayList<>();

    boolean hasTestUsingClassInstance(String clazz) {
      return encounteredClasses.contains(clazz);
    }

    @Override
    public @Nullable Void visitMethodInvocation(MethodInvocationTree node, VisitorState state) {
      if (BUG_PATTERN_TEST_METHOD.matches(node, state)) {
        MemberSelectTree firstArgumentTree = (MemberSelectTree) node.getArguments().get(0);
        encounteredClasses.add(firstArgumentTree.getExpression().toString());
      }
      return super.visitMethodInvocation(node, state);
    }
  }

  private static final class CollectBugPatternTests
      extends TreeScanner<@Nullable Void, @Nullable Void> {
    private static final Matcher<ExpressionTree> IDENTIFICATION_SOURCE_LINES =
        instanceMethod()
            .onDescendantOf("com.google.errorprone.CompilationTestHelper")
            .named("addSourceLines");
    private static final Matcher<ExpressionTree> REPLACEMENT_INPUT =
        instanceMethod()
            .onDescendantOf("com.google.errorprone.BugCheckerRefactoringTestHelper")
            .named("addInputLines");
    private static final Matcher<ExpressionTree> REPLACEMENT_OUTPUT =
        instanceMethod()
            .onDescendantOf("com.google.errorprone.BugCheckerRefactoringTestHelper.ExpectOutput")
            .named("addOutputLines");

    private final VisitorState state;
    private final List<String> identificationTests = new ArrayList<>();
    private final List<BugPatternReplacementTestDocumentation> replacementTests = new ArrayList<>();

    @Var private String replacementOutputLines = "";

    CollectBugPatternTests(VisitorState state) {
      this.state = state;
    }

    public ImmutableList<String> getIdentificationTests() {
      return ImmutableList.copyOf(identificationTests);
    }

    public ImmutableList<BugPatternReplacementTestDocumentation> getReplacementTests() {
      return ImmutableList.copyOf(replacementTests);
    }

    @Override
    public @Nullable Void visitMethodInvocation(MethodInvocationTree node, @Nullable Void unused) {
      if (IDENTIFICATION_SOURCE_LINES.matches(node, state)) {
        identificationTests.add(getSourceLines(node));
      } else if (REPLACEMENT_INPUT.matches(node, state)) {
        /* The visitor starts with `addOutputLines` and in the next visit it will go over the `addInputLines`. */
        replacementTests.add(
            BugPatternReplacementTestDocumentation.create(
                getSourceLines(node), replacementOutputLines));
      } else if (REPLACEMENT_OUTPUT.matches(node, state)) {
        replacementOutputLines = getSourceLines(node);
      }
      return super.visitMethodInvocation(node, unused);
    }

    // XXX: Duplicate from `ErrorProneTestSourceFormat`, should we move this to `SourceCode` util?
    private static String getSourceLines(MethodInvocationTree tree) {
      List<? extends ExpressionTree> sourceLines =
          tree.getArguments().subList(1, tree.getArguments().size());
      StringBuilder source = new StringBuilder();

      for (ExpressionTree sourceLine : sourceLines) {
        Object value = ASTHelpers.constValue(sourceLine);
        if (value == null) {
          return "";
        }
        source.append(value).append('\n');
      }

      return source.toString();
    }
  }

  @AutoValue
  abstract static class BugPatternTestDocumentation {
    abstract String name();

    abstract ImmutableList<String> identificationTests();

    abstract ImmutableList<BugPatternReplacementTestDocumentation> replacementTests();
  }

  @AutoValue
  abstract static class BugPatternReplacementTestDocumentation {
    static BugPatternReplacementTestDocumentation create(String sourceLines, String outputLines) {
      return new AutoValue_BugPatternTestExtractor_BugPatternReplacementTestDocumentation(
          sourceLines, outputLines);
    }

    abstract String inputLines();

    abstract String outputLines();
  }
}
