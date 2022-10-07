package tech.picnic.errorprone.plugin;

import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.instanceMethod;

import com.google.errorprone.VisitorState;
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
import tech.picnic.errorprone.plugin.objects.BugPatternTestData;

public final class BugPatternTestsExtractor implements DocExtractor<BugPatternTestData> {
  private static final Matcher<MethodTree> BUG_PATTERN_TEST =
      allOf(hasAnnotation("org.junit.jupiter.api.Test"));
  // anyOf(methodIsNamed("replacement"), methodIsNamed("identification"))
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
        .filter(m -> BUG_PATTERN_TEST.matches(m, state))
        .forEach(m -> scanner.scan(m, null));

    return BugPatternTestData.create(
        name, scanner.getIdentification(), scanner.getInput(), scanner.getOutput());
  }

  private static final class ScanBugCheckerTestData extends TreeScanner<Void, Void> {
    private final VisitorState state;

    private final List<String> identification = new ArrayList<>();
    private final List<String> input = new ArrayList<>();
    private final List<String> output = new ArrayList<>();

    ScanBugCheckerTestData(VisitorState state) {
      this.state = state;
    }

    public List<String> getIdentification() {
      return identification;
    }

    public List<String> getInput() {
      return input;
    }

    public List<String> getOutput() {
      return output;
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
      if (IDENTIFICATION_SOURCE_LINES.matches(node, state)) {
        identification.add(getSourceLines(node));
      } else if (REPLACEMENT_INPUT.matches(node, state)) {
        input.add(getSourceLines(node));
      } else if (REPLACEMENT_OUTPUT.matches(node, state)) {
        output.add(getSourceLines(node));
      }
      return super.visitMethodInvocation(node, unused);
    }

    private String getSourceLines(MethodInvocationTree tree) {
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
