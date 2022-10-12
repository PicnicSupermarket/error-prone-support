package tech.picnic.errorprone.refaster.test;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;
import static com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static java.util.Comparator.naturalOrder;
import static tech.picnic.errorprone.refaster.runner.Refaster.INCLUDED_RULES_PATTERN_FLAG;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.SubContext;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.fixes.Replacement;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Position;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import tech.picnic.errorprone.refaster.runner.CodeTransformers;
import tech.picnic.errorprone.refaster.runner.Refaster;

/**
 * A {@link BugChecker} that applies a Refaster rule collection to an associated test input file by
 * delegating to the {@link Refaster} checker, and subsequently validates that each rule modifies
 * exactly one distinct method, as indicated by each method's name.
 *
 * <p>The test input and output files must be classpath resources located in the same package as the
 * rule collection class. Their names are derived from the rule collection class by suffixing {@code
 * TestInput.java} and {@code TestOutput.java}, respectively. Each test method's name must be
 * derived from the rule that modifies said method by prefixing {@code test}.
 */
// XXX: This check currently only validates that one `Refaster.anyOf` branch in one
// `@BeforeTemplate` method is covered by a test. Review how we can make sure that _all_
// `@BeforeTemplate` methods and `Refaster.anyOf` branches are covered.
@BugPattern(summary = "Exercises a Refaster rule collection", severity = ERROR)
public final class RefasterRuleCollection extends BugChecker implements CompilationUnitTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String RULE_COLLECTION_FLAG = "RefasterRuleCollection:RuleCollection";
  private static final String TEST_METHOD_NAME_PREFIX = "test";

  private final String ruleCollectionUnderTest;
  private final ImmutableSortedSet<String> rulesUnderTest;
  private final Refaster delegate;

  /**
   * Instantiates a {@link RefasterRuleCollection} instance.
   *
   * @param flags Any provided command line flags.
   */
  public RefasterRuleCollection(ErrorProneFlags flags) {
    ruleCollectionUnderTest = getRuleCollectionUnderTest(flags);
    delegate = createRefasterChecker(ruleCollectionUnderTest);
    rulesUnderTest = getRulesUnderTest(ruleCollectionUnderTest);
  }

  private static String getRuleCollectionUnderTest(ErrorProneFlags flags) {
    return flags
        .get(RULE_COLLECTION_FLAG)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    String.format(
                        "Error Prone flag `%s` must be specified", RULE_COLLECTION_FLAG)));
  }

  private static Refaster createRefasterChecker(String ruleCollectionUnderTest) {
    return new Refaster(
        ErrorProneFlags.fromMap(
            ImmutableMap.of(
                INCLUDED_RULES_PATTERN_FLAG, Pattern.quote(ruleCollectionUnderTest) + ".*")));
  }

  private static ImmutableSortedSet<String> getRulesUnderTest(String ruleCollectionUnderTest) {
    return CodeTransformers.getAllCodeTransformers().keySet().stream()
        .filter(k -> k.startsWith(ruleCollectionUnderTest))
        .map(k -> k.replace(ruleCollectionUnderTest + '$', ""))
        .collect(toImmutableSortedSet(naturalOrder()));
  }

  /**
   * Verifies that all Refaster rules in the given collection class are covered by precisely one
   * test method, defined explicitly for the purpose of exercising that rule.
   *
   * <p>Note that a passing test does not guarantee full coverage: this test does not ascertain that
   * all {@code com.google.errorprone.refaster.Refaster#anyOf} branches are tested. Likewise for
   * {@code com.google.errorprone.refaster.annotation.BeforeTemplate} methods in case there are
   * multiple.
   *
   * @param clazz The Refaster rule collection under test.
   */
  public static void validate(Class<?> clazz) {
    String className = clazz.getSimpleName();

    BugCheckerRefactoringTestHelper.newInstance(RefasterRuleCollection.class, clazz)
        .setArgs(ImmutableList.of("-XepOpt:" + RULE_COLLECTION_FLAG + '=' + className))
        .addInput(className + "TestInput.java")
        .addOutput(className + "TestOutput.java")
        .doTest(TEXT_MATCH);
  }

  @Override
  public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
    reportIncorrectClassName(tree, state);

    List<Description> matches = new ArrayList<>();
    delegate.matchCompilationUnit(
        tree,
        VisitorState.createForCustomFindingCollection(new SubContext(state.context), matches::add)
            .withPath(state.getPath()));

    ImmutableRangeMap<Integer, String> indexedMatches =
        indexRuleMatches(matches, ((JCCompilationUnit) tree).endPositions);

    matches.forEach(state::reportMatch);
    reportMissingMatches(tree, indexedMatches, state);
    reportUnexpectedMatches(tree, indexedMatches, state);

    return Description.NO_MATCH;
  }

  private void reportIncorrectClassName(CompilationUnitTree tree, VisitorState state) {
    String expectedClassName = ruleCollectionUnderTest + "Test";

    for (Tree typeDeclaration : tree.getTypeDecls()) {
      if (typeDeclaration instanceof ClassTree) {
        if (!((ClassTree) typeDeclaration).getSimpleName().contentEquals(expectedClassName)) {
          state.reportMatch(
              describeMatch(
                  typeDeclaration,
                  SuggestedFix.prefixWith(
                      typeDeclaration,
                      String.format(
                          "/* ERROR: Class should be named `%s`. */\n", expectedClassName))));
        }
      } else {
        state.reportMatch(
            describeMatch(
                typeDeclaration,
                SuggestedFix.prefixWith(typeDeclaration, "/* ERROR: Unexpected token. */\n")));
      }
    }
  }

  private static ImmutableRangeMap<Integer, String> indexRuleMatches(
      List<Description> matches, EndPosTable endPositions) {
    ImmutableRangeMap.Builder<Integer, String> ruleMatches = ImmutableRangeMap.builder();

    for (Description description : matches) {
      String ruleName = extractRefasterRuleName(description);
      Set<Replacement> replacements =
          Iterables.getOnlyElement(description.fixes).getReplacements(endPositions);
      for (Replacement replacement : replacements) {
        ruleMatches.put(replacement.range(), ruleName);
      }
    }

    return ruleMatches.build();
  }

  private void reportMissingMatches(
      CompilationUnitTree tree,
      ImmutableRangeMap<Integer, String> indexedMatches,
      VisitorState state) {
    ImmutableSet<String> rulesWithoutMatch =
        Sets.difference(
                rulesUnderTest, ImmutableSet.copyOf(indexedMatches.asMapOfRanges().values()))
            .immutableCopy();
    if (!rulesWithoutMatch.isEmpty()) {
      String sourceFile = ((JCCompilationUnit) tree).sourcefile.getName();
      reportViolations(
          tree,
          String.format(
              "Did not encounter a test in `%s` for the following rule(s)",
              getSubstringAfterFinalDelimiter('/', sourceFile)),
          rulesWithoutMatch,
          state);
    }
  }

  private void reportUnexpectedMatches(
      CompilationUnitTree tree,
      ImmutableRangeMap<Integer, String> indexedMatches,
      VisitorState state) {
    UnexpectedMatchReporter unexpectedMatchReporter = new UnexpectedMatchReporter(indexedMatches);
    unexpectedMatchReporter.scan(tree.getTypeDecls(), state);
  }

  private void reportViolations(
      Tree tree, String message, ImmutableSet<String> violations, VisitorState state) {
    String violationEnumeration = String.join("\n*  - ", violations);
    String comment =
        String.format("/*\n*  ERROR: %s:\n*  - %s\n*/\n", message, violationEnumeration);
    SuggestedFix fixWithComment =
        tree instanceof MethodTree
            ? SuggestedFix.prefixWith(tree, comment)
            : SuggestedFix.postfixWith(tree, '\n' + comment);
    state.reportMatch(describeMatch(tree, fixWithComment));
  }

  private static String extractRefasterRuleName(Description description) {
    String message = description.getRawMessage();
    int index = message.indexOf(':');
    checkState(index >= 0, "Failed to extract Refaster rule name from string '%s'", message);
    return getSubstringAfterFinalDelimiter('.', message.substring(0, index));
  }

  private static String getSubstringAfterFinalDelimiter(char delimiter, String value) {
    int index = value.lastIndexOf(delimiter);
    checkState(index >= 0, "String '%s' does not contain character '%s'", value, delimiter);
    return value.substring(index + 1);
  }

  private class UnexpectedMatchReporter extends TreeScanner<Void, VisitorState> {
    private final ImmutableRangeMap<Integer, String> indexedMatches;

    UnexpectedMatchReporter(ImmutableRangeMap<Integer, String> indexedMatches) {
      this.indexedMatches = indexedMatches;
    }

    @Nullable
    @Override
    public Void visitMethod(MethodTree tree, VisitorState state) {
      if (!ASTHelpers.isGeneratedConstructor(tree)) {
        getRuleUnderTest(tree, state)
            .ifPresent(ruleUnderTest -> reportUnexpectedMatches(tree, ruleUnderTest, state));
      }

      return super.visitMethod(tree, state);
    }

    private void reportUnexpectedMatches(
        MethodTree tree, String ruleUnderTest, VisitorState state) {
      // XXX: Validate that `getMatchesInTree(tree, state)` returns a non-empty result (strictly
      // speaking one of the values should match `ruleUnderTest`, but we can skip that check).

      ImmutableListMultimap<Long, String> unexpectedMatchesByLineNumber =
          getUnexpectedMatchesByLineNumber(getMatchesInTree(tree, state), ruleUnderTest, state);

      if (!unexpectedMatchesByLineNumber.isEmpty()) {
        reportViolations(
            tree,
            String.format(
                "The following matches unexpectedly occurred in method `%s`", tree.getName()),
            unexpectedMatchesByLineNumber.entries().stream()
                .map(
                    e ->
                        String.format(
                            "Rule `%s` matches on line %s, while it should match in a method named `test%s`.",
                            e.getValue(), e.getKey(), e.getValue()))
                .collect(toImmutableSet()),
            state);
      }
    }

    private Optional<String> getRuleUnderTest(MethodTree tree, VisitorState state) {
      String methodName = tree.getName().toString();
      if (methodName.startsWith(TEST_METHOD_NAME_PREFIX)) {
        return Optional.of(methodName.substring(TEST_METHOD_NAME_PREFIX.length()));
      }

      /*
       * Unless this method is `RefasterRuleCollectionTestCase#elidedTypesAndStaticImports`, it's
       * misnamed.
       */
      if (!"elidedTypesAndStaticImports".equals(methodName)) {
        state.reportMatch(
            describeMatch(
                tree,
                SuggestedFix.prefixWith(
                    tree, "/* ERROR: Method names should start with `test`. */\n")));
      }

      return Optional.empty();
    }

    private ImmutableRangeMap<Integer, String> getMatchesInTree(
        MethodTree tree, VisitorState state) {
      int startPosition = ASTHelpers.getStartPosition(tree);
      int endPosition = state.getEndPosition(tree);

      checkState(
          startPosition != Position.NOPOS && endPosition != Position.NOPOS,
          "Cannot determine location of method in source code");

      return indexedMatches.subRangeMap(Range.closedOpen(startPosition, endPosition));
    }

    private ImmutableListMultimap<Long, String> getUnexpectedMatchesByLineNumber(
        ImmutableRangeMap<Integer, String> matches, String ruleUnderTest, VisitorState state) {
      LineMap lineMap = state.getPath().getCompilationUnit().getLineMap();
      return matches.asMapOfRanges().entrySet().stream()
          .filter(e -> !e.getValue().equals(ruleUnderTest))
          .collect(
              toImmutableListMultimap(
                  e -> lineMap.getLineNumber(e.getKey().lowerEndpoint()), Map.Entry::getValue));
    }
  }
}
