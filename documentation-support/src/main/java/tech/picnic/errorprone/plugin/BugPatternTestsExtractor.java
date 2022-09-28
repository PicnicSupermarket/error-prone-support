package tech.picnic.errorprone.plugin;

import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.instanceMethod;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TreeScanner;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.plugin.models.BugPatternReplacementTestData;
import tech.picnic.errorprone.plugin.models.BugPatternTestData;

/** XXX: Write this. */
// XXX: Take into account `expectUnchanged()`.
public final class BugPatternTestsExtractor implements DocExtractor<BugPatternTestData> {
  private static final Matcher<MethodTree> JUNIT_TEST_METHOD =
      hasAnnotation("org.junit.jupiter.api.Test");

  @Override
  public BugPatternTestData extractData(ClassTree tree, TaskEvent taskEvent, VisitorState state) {
    CollectBugPatternTests scanner = new CollectBugPatternTests(state);

    tree.getMembers().stream()
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast)
        .filter(m -> JUNIT_TEST_METHOD.matches(m, state))
        .forEach(m -> scanner.scan(m, null));

    String className = tree.getSimpleName().toString();
    return BugPatternTestData.create(
        className.substring(0, className.lastIndexOf("Test")),
        scanner.getIdentificationTests(),
        scanner.getReplacementTests());
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
    private final List<BugPatternReplacementTestData> replacementTests = new ArrayList<>();

    @Var private String replacementOutputLines = "";

    CollectBugPatternTests(VisitorState state) {
      this.state = state;
    }

    public ImmutableList<String> getIdentificationTests() {
      return ImmutableList.copyOf(identificationTests);
    }

    public ImmutableList<BugPatternReplacementTestData> getReplacementTests() {
      return ImmutableList.copyOf(replacementTests);
    }

    @Override
    public @Nullable Void visitMethodInvocation(MethodInvocationTree node, @Nullable Void unused) {
      if (IDENTIFICATION_SOURCE_LINES.matches(node, state)) {
        identificationTests.add(getSourceLines(node));
      } else if (REPLACEMENT_INPUT.matches(node, state)) {
        /* The visitor starts with `addOutputLines` and in the next visit it will go over the `addInputLines`. */
        replacementTests.add(
            BugPatternReplacementTestData.create(getSourceLines(node), replacementOutputLines));
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
}
