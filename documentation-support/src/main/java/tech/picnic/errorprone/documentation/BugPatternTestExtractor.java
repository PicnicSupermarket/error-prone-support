package tech.picnic.errorprone.documentation;

import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anything;
import static com.google.errorprone.matchers.Matchers.argument;
import static com.google.errorprone.matchers.Matchers.classLiteral;
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
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.documentation.BugPatternTestExtractor.BugPatternTestDocumentation;

/**
 * An {@link Extractor} that describes how to extract data from classes that test a {@code
 * BugChecker}.
 */
@Immutable
@AutoService(Extractor.class)
public final class BugPatternTestExtractor implements Extractor<BugPatternTestDocumentation> {
  private static final Pattern TEST_CLASS_NAME_PATTERN = Pattern.compile("(.*)Test");
  private static final Matcher<Tree> JUNIT_TEST_METHOD =
      toType(MethodTree.class, hasAnnotation("org.junit.jupiter.api.Test"));
  private static final Matcher<MethodInvocationTree> BUG_PATTERN_TEST_METHOD =
      allOf(
          staticMethod()
              .onDescendantOfAny(
                  "com.google.errorprone.CompilationTestHelper",
                  "com.google.errorprone.BugCheckerRefactoringTestHelper")
              .named("newInstance"),
          argument(0, classLiteral(anything())));

  /** Instantiates a new {@link BugPatternTestExtractor} instance. */
  public BugPatternTestExtractor() {}

  @Override
  public String identifier() {
    return "bugpattern-test";
  }

  // XXX: Improve support for correctly extracting multiple sources from a single
  // `{BugCheckerRefactoring,Compilation}TestHelper` test.
  @Override
  public Optional<BugPatternTestDocumentation> tryExtract(ClassTree tree, VisitorState state) {
    return getClassUnderTest(tree)
        .filter(bugPatternName -> testsBugPattern(bugPatternName, tree, state))
        .map(
            bugPatternName -> {
              CollectBugPatternTests scanner = new CollectBugPatternTests();

              for (Tree m : tree.getMembers()) {
                if (JUNIT_TEST_METHOD.matches(m, state)) {
                  scanner.scan(m, state);
                }
              }

              return new AutoValue_BugPatternTestExtractor_BugPatternTestDocumentation(
                  bugPatternName, scanner.getIdentificationTests(), scanner.getReplacementTests());
            });
  }

  private static boolean testsBugPattern(
      String bugPatternName, ClassTree tree, VisitorState state) {
    AtomicBoolean result = new AtomicBoolean(false);

    new TreeScanner<@Nullable Void, @Nullable Void>() {
      @Override
      public @Nullable Void visitMethodInvocation(MethodInvocationTree node, @Nullable Void v) {
        if (BUG_PATTERN_TEST_METHOD.matches(node, state)) {
          MemberSelectTree firstArgumentTree = (MemberSelectTree) node.getArguments().get(0);
          result.compareAndSet(
              /* expectedValue= */ false,
              bugPatternName.equals(firstArgumentTree.getExpression().toString()));
        }

        return super.visitMethodInvocation(node, v);
      }
    }.scan(tree, null);

    return result.get();
  }

  private static Optional<String> getClassUnderTest(ClassTree tree) {
    return Optional.of(TEST_CLASS_NAME_PATTERN.matcher(tree.getSimpleName().toString()))
        .filter(java.util.regex.Matcher::matches)
        .map(m -> m.group(1));
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
