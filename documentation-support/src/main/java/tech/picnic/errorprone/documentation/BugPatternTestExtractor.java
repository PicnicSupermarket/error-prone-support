package tech.picnic.errorprone.documentation;

import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anything;
import static com.google.errorprone.matchers.Matchers.argument;
import static com.google.errorprone.matchers.Matchers.classLiteral;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.toType;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.documentation.BugPatternTestExtractor.BugPatternTestDocumentation;

/**
 * An {@link Extractor} that describes how to extract data from a test that tests a {@code
 * BugChecker}.
 */
@Immutable
final class BugPatternTestExtractor implements Extractor<BugPatternTestDocumentation> {
  private static final Matcher<Tree> JUNIT_TEST_METHOD =
      toType(MethodTree.class, hasAnnotation("org.junit.jupiter.api.Test"));

  // XXX: Improve support for correctly extracting multiple sources from a single
  // `{BugCheckerRefactoring,Compilation}TestHelper` test.
  @Override
  public BugPatternTestDocumentation extract(ClassTree tree, VisitorState state) {
    CollectBugPatternTests scanner = new CollectBugPatternTests();

    for (Tree m : tree.getMembers()) {
      if (JUNIT_TEST_METHOD.matches(m, state)) {
        scanner.scan(m, state);
      }
    }

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

  // XXX: Consider replacing this type with an anonymous class in a method. Possibly also below.
  private static final class ScanBugPatternTest extends TreeScanner<@Nullable Void, VisitorState> {
    private static final Matcher<MethodInvocationTree> BUG_PATTERN_TEST_METHOD =
        allOf(
            staticMethod()
                .onDescendantOfAny(
                    "com.google.errorprone.CompilationTestHelper",
                    "com.google.errorprone.BugCheckerRefactoringTestHelper")
                .named("newInstance"),
            argument(0, classLiteral(anything())));

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
      extends TreeScanner<@Nullable Void, VisitorState> {
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

    private final List<String> identificationTests = new ArrayList<>();
    private final List<BugPatternReplacementTestDocumentation> replacementTests = new ArrayList<>();

    public ImmutableList<String> getIdentificationTests() {
      return ImmutableList.copyOf(identificationTests);
    }

    public ImmutableList<BugPatternReplacementTestDocumentation> getReplacementTests() {
      return ImmutableList.copyOf(replacementTests);
    }

    // XXX: Consider:
    // - Whether to omit or handle differently identification tests without `// BUG: Diagnostic
    //   (contains|matches)` markers.
    // - Whether to omit or handle differently replacement tests with identical input and output.
    //   (Though arguably we should have a separate checker which replaces such cases with
    //   `.expectUnchanged()`.)
    // - Whether to track `.expectUnchanged()` test cases.
    @Override
    public @Nullable Void visitMethodInvocation(MethodInvocationTree node, VisitorState state) {
      if (IDENTIFICATION_SOURCE_LINES.matches(node, state)) {
        getSourceCode(node).ifPresent(identificationTests::add);
      } else if (REPLACEMENT_OUTPUT.matches(node, state)) {
        ExpressionTree receiver = ASTHelpers.getReceiver(node);
        // XXX: Make this code nicer.
        if (REPLACEMENT_INPUT.matches(receiver, state)) {
          getSourceCode(node)
              .ifPresent(
                  output ->
                      getSourceCode((MethodInvocationTree) receiver)
                          .ifPresent(
                              input ->
                                  replacementTests.add(
                                      new AutoValue_BugPatternTestExtractor_BugPatternReplacementTestDocumentation(
                                          input, output))));
        }
      }

      return super.visitMethodInvocation(node, state);
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

  // XXX: Rename?
  @AutoValue
  abstract static class BugPatternTestDocumentation {
    abstract String name();

    abstract ImmutableList<String> identificationTests();

    abstract ImmutableList<BugPatternReplacementTestDocumentation> replacementTests();
  }

  // XXX: Rename?
  @AutoValue
  abstract static class BugPatternReplacementTestDocumentation {
    abstract String inputLines();

    abstract String outputLines();
  }
}
