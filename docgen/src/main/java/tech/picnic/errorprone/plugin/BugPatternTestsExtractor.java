package tech.picnic.errorprone.plugin;

import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.instanceMethod;

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
import tech.picnic.errorprone.plugin.models.BugPatternReplacementTestData;
import tech.picnic.errorprone.plugin.models.BugPatternTestData;

/** XXX: Write this. */
// XXX: Take into account `expectUnchanged()`.
public final class BugPatternTestsExtractor implements DocExtractor<BugPatternTestData> {
  private static final Matcher<MethodTree> JUNIT_TEST_METHOD =
      allOf(hasAnnotation("org.junit.jupiter.api.Test"));

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

  @Override
  public BugPatternTestData extractData(ClassTree tree, TaskEvent taskEvent, VisitorState state) {
    String name = tree.getSimpleName().toString().replace("Test", "");
    ScanBugCheckerTestData scanner = new ScanBugCheckerTestData(state);

    tree.getMembers().stream()
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast)
        .filter(m -> JUNIT_TEST_METHOD.matches(m, state))
        .forEach(m -> scanner.scan(m, null));

    return BugPatternTestData.create(
        name, scanner.getIdentificationTests(), scanner.getReplacementTests());
  }

  private static final class ScanBugCheckerTestData extends TreeScanner<Void, Void> {
    private final VisitorState state;
    private final List<String> identificationTests = new ArrayList<>();
    private final List<BugPatternReplacementTestData> replacementTests = new ArrayList<>();

    // XXX: Using this output field is a bit hacky. Come up with a better solution.
    @Var private String output;

    ScanBugCheckerTestData(VisitorState state) {
      this.state = state;
    }

    public List<String> getIdentificationTests() {
      return identificationTests;
    }

    public List<BugPatternReplacementTestData> getReplacementTests() {
      return replacementTests;
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
      if (IDENTIFICATION_SOURCE_LINES.matches(node, state)) {
        identificationTests.add(getSourceLines(node));
      } else if (REPLACEMENT_INPUT.matches(node, state)) {
        replacementTests.add(BugPatternReplacementTestData.create(getSourceLines(node), output));
      } else if (REPLACEMENT_OUTPUT.matches(node, state)) {
        output = getSourceLines(node);
      }
      return super.visitMethodInvocation(node, unused);
    }

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
