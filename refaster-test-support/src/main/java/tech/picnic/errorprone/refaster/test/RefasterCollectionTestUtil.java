package tech.picnic.errorprone.refaster.test;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.refaster.runner.RefasterCheck.INCLUDED_TEMPLATES_PATTERN_FLAG;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.FileObjects;
import com.google.errorprone.SubContext;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.fixes.Replacement;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collector;
import javax.tools.JavaFileObject;
import tech.picnic.errorprone.refaster.runner.CodeTransformers;
import tech.picnic.errorprone.refaster.runner.RefasterCheck;

/** Utility to test Refaster templates and validate the tests for template collections. */
public final class RefasterCollectionTestUtil {
  static final Supplier<ImmutableListMultimap<String, CodeTransformer>> ALL_CODE_TRANSFORMERS =
      Suppliers.memoize(CodeTransformers::loadAllCodeTransformers);

  private RefasterCollectionTestUtil() {}

  /**
   * Verifies that all Refaster templates from a collection are covered by at least one test and
   * that the match rewrites code in the correct test method.
   *
   * <p>Note that this doesn't guarantee full coverage: this test does not ascertain that all {@link
   * com.google.errorprone.refaster.Refaster#anyOf} branches are tested. Idem for {@link
   * com.google.errorprone.refaster.annotation.BeforeTemplate} methods in case there are multiple.
   *
   * @param clazz The Refaster template collection under test.
   */
  public static void validateTemplateCollection(Class<?> clazz) {
    String className = clazz.getSimpleName();
    JavaFileObject input = FileObjects.forResource(clazz, className + "TestInput.java");
    JavaFileObject output = FileObjects.forResource(clazz, className + "TestOutput.java");
    String inputFileContent;
    String outputFileContent;
    try {
      inputFileContent = input.getCharContent(false).toString();
      outputFileContent = output.getCharContent(false).toString();
    } catch (IOException e) {
      throw new IllegalStateException(
          "Can't retrieve content from input or output file for class " + clazz.getName(), e);
    }
    BugCheckerRefactoringTestHelper.newInstance(
            RefasterTestBugChecker.class, RefasterCollectionTestUtil.class)
        .setArgs(ImmutableList.of("-XepOpt:RefasterTestChecker:TemplateCollection=" + className))
        .addInputLines(clazz.getName() + "TestInput.java", inputFileContent)
        .addOutputLines(clazz.getName() + "TestOutput.java", outputFileContent)
        .doTest(TEXT_MATCH);
  }

  /**
   * A {@link BugChecker} that tests the Refaster templates of a given template collection and
   * validates its tests.
   */
  @BugPattern(
      name = "RefasterTestBugChecker",
      summary = "Validate a Refaster template collection and its tests",
      severity = ERROR)
  public static final class RefasterTestBugChecker extends BugChecker
      implements CompilationUnitTreeMatcher {
    private static final long serialVersionUID = 1L;
    private static final Collector<CharSequence, ?, String> LIST_COLLECTOR =
        joining("\n- ", "\n- ", "\n");

    private final ImmutableSet<String> templateNamesFromClassPath;
    private final RefasterCheck delegate;

    /**
     * Instantiates a customized {@link RefasterTestBugChecker}.
     *
     * @param flags Any provided command line flags.
     */
    public RefasterTestBugChecker(ErrorProneFlags flags) {
      String templateCollection = flags.get("RefasterTestChecker:TemplateCollection").orElseThrow();
      delegate =
          new RefasterCheck(
              ErrorProneFlags.fromMap(
                  ImmutableMap.of(INCLUDED_TEMPLATES_PATTERN_FLAG, templateCollection + ".*")));
      templateNamesFromClassPath =
          ALL_CODE_TRANSFORMERS.get().keySet().stream()
              .filter(k -> k.contains(templateCollection))
              .map(k -> k.replace(templateCollection + "$", ""))
              .sorted()
              .collect(toImmutableSet());
    }

    @Override
    public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
      List<Description> matches = new ArrayList<>();
      delegate.matchCompilationUnit(
          tree,
          VisitorState.createForCustomFindingCollection(new SubContext(state.context), matches::add)
              .withPath(state.getPath()));

      ImmutableRangeMap<Integer, String> matchesRangeMap =
          buildRangeMapForMatches(matches, ((JCCompilationUnit) tree).endPositions);

      ImmutableSet<String> templatesWithoutMatch = getTemplateNamesWithoutMatch(matches);
      if (!templatesWithoutMatch.isEmpty()) {
        appendCommentToCompilationUnit(
            String.format(
                "Did not encounter test in %s for the following template(s)",
                ASTHelpers.getFileName(tree)),
            templatesWithoutMatch.stream().collect(LIST_COLLECTOR),
            state);
      }

      ValidateMatchesInMethodsScanner scanner =
          new ValidateMatchesInMethodsScanner(matchesRangeMap);
      scanner.scan(tree.getTypeDecls(), state);

      matches.forEach(state::reportMatch);
      return Description.NO_MATCH;
    }

    private ImmutableSet<String> getTemplateNamesWithoutMatch(List<Description> matches) {
      ImmutableSet<String> templateNamesOfMatches =
          matches.stream()
              .map(description -> description.checkName)
              .map(RefasterTestBugChecker::getNameFromFQCN)
              .collect(toImmutableSet());

      return Sets.difference(templateNamesFromClassPath, templateNamesOfMatches).immutableCopy();
    }

    private void appendCommentToCompilationUnit(String message, String list, VisitorState state) {
      String comment = String.format("\n/* %s:%s*/", message, list);
      CompilationUnitTree compilationUnit = state.getPath().getCompilationUnit();
      state.reportMatch(
          describeMatch(compilationUnit, SuggestedFix.postfixWith(compilationUnit, comment)));
    }

    private static ImmutableRangeMap<Integer, String> buildRangeMapForMatches(
        List<Description> matches, EndPosTable endPositions) {
      ImmutableRangeMap.Builder<Integer, String> rangeMap = ImmutableRangeMap.builder();

      for (Description description : matches) {
        Set<Replacement> replacements =
            Iterables.getOnlyElement(description.fixes).getReplacements(endPositions);
        Replacement replacement = Iterables.getOnlyElement(replacements);

        rangeMap.put(replacement.range(), getNameFromFQCN(description.checkName));
      }
      return rangeMap.build();
    }

    private static String getNameFromFQCN(String fqcn) {
      return fqcn.substring(fqcn.lastIndexOf('.') + 1);
    }

    private class ValidateMatchesInMethodsScanner extends TreeScanner<Void, VisitorState> {
      private final ImmutableRangeMap<Integer, String> matchesRangeMap;

      ValidateMatchesInMethodsScanner(ImmutableRangeMap<Integer, String> matchesRangeMap) {
        this.matchesRangeMap = matchesRangeMap;
      }

      @Override
      public Void visitMethod(MethodTree tree, VisitorState state) {
        if (ASTHelpers.isGeneratedConstructor(tree)) {
          return super.visitMethod(tree, state);
        }

        String methodName = tree.getName().toString().replace("test", "");
        int startPosition = ASTHelpers.getStartPosition(tree);
        int endPosition = state.getEndPosition(tree);

        ImmutableRangeMap<Integer, String> matchesInCurrentMethod =
            matchesRangeMap.subRangeMap(Range.open(startPosition, endPosition));
        boolean correctTemplatesMatchedInMethod =
            matchesInCurrentMethod.asMapOfRanges().values().stream().allMatch(methodName::equals);
        if (!correctTemplatesMatchedInMethod) {
          appendCommentToCompilationUnit(
              String.format(
                  "The following matches occurred in method `%s` (position: [%s,%s])",
                  tree.getName(), startPosition, endPosition),
              matchesRangeMap.asMapOfRanges().entrySet().stream()
                  .map(e -> "Template `" + e.getValue() + "` matched on position: " + e.getKey())
                  .collect(LIST_COLLECTOR),
              state);
        }
        return super.visitMethod(tree, state);
      }
    }
  }
}
