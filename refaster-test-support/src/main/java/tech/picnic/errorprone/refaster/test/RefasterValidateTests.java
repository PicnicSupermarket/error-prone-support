package tech.picnic.errorprone.refaster.test;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.refaster.runner.RefasterCheck.INCLUDED_TEMPLATES_PATTERN_FLAG;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.errorprone.BugPattern;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.SubContext;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.fixes.Replacement;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.runner.CodeTransformers;
import tech.picnic.errorprone.refaster.runner.RefasterCheck;

/**
 * A {@link BugChecker} that tests the Refaster templates of a given template collection and
 * validates its tests.
 */
@BugPattern(
    name = "RefasterValidateTests",
    summary = "Validate a Refaster template collection and its tests",
    severity = ERROR)
public final class RefasterValidateTests extends BugChecker implements CompilationUnitTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Supplier<ImmutableListMultimap<String, CodeTransformer>>
      ALL_CODE_TRANSFORMERS = Suppliers.memoize(CodeTransformers::loadAllCodeTransformers);

  private final ImmutableSet<String> templateNamesFromClassPath;
  private final RefasterCheck delegate;

  /**
   * Instantiates a customized {@link RefasterValidateTests}.
   *
   * @param flags Any provided command line flags.
   */
  public RefasterValidateTests(ErrorProneFlags flags) {
    String templateCollection = flags.get("RefasterValidateTests:TemplateCollection").orElseThrow();
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

    JCCompilationUnit compilationUnit = (JCCompilationUnit) tree;
    ImmutableRangeMap<Integer, String> matchesRangeMap =
        buildRangeMapForMatches(matches, compilationUnit.endPositions);

    ImmutableSet<String> templatesWithoutMatch =
        getTemplateNamesWithoutMatch(matchesRangeMap.asMapOfRanges().values());
    if (!templatesWithoutMatch.isEmpty()) {
      addCommentToTreeInOutputFile(
          tree,
          String.format(
              "Did not encounter a test in `%s` for the following template(s)",
              getNameFromFQCN(compilationUnit.sourcefile.getName().replace(".java", ""))),
          templatesWithoutMatch.stream(),
          state);
    }

    ValidateMatchesInMethodsScanner scanner = new ValidateMatchesInMethodsScanner(matchesRangeMap);
    scanner.scan(tree.getTypeDecls(), state);

    matches.forEach(state::reportMatch);
    return Description.NO_MATCH;
  }

  private ImmutableSet<String> getTemplateNamesWithoutMatch(
      ImmutableCollection<String> templateNamesOfMatches) {
    return Sets.difference(templateNamesFromClassPath, ImmutableSet.copyOf(templateNamesOfMatches))
        .immutableCopy();
  }

  private void addCommentToTreeInOutputFile(
      Tree tree, String message, Stream<String> violations, VisitorState state) {
    String listOfViolations = violations.collect(joining("\n*  - ", "\n*  - ", "\n"));
    String comment = String.format("/*\n*  %s:%s*/\n", message, listOfViolations);
    SuggestedFix fixWithComment =
        tree instanceof MethodTree
            ? SuggestedFix.prefixWith(tree, comment)
            : SuggestedFix.postfixWith(tree, "\n" + comment);
    state.reportMatch(describeMatch(tree, fixWithComment));
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
      LineMap lineMap = state.getPath().getCompilationUnit().getLineMap();

      ImmutableRangeMap<Integer, String> matchesInCurrentMethod =
          matchesRangeMap.subRangeMap(Range.open(startPosition, endPosition));
      boolean correctTemplatesMatchedInMethod =
          matchesInCurrentMethod.asMapOfRanges().values().stream().allMatch(methodName::equals);
      if (!correctTemplatesMatchedInMethod) {
        addCommentToTreeInOutputFile(
            tree,
            String.format(
                "The following matches unexpectedly occurred in method `%s`", tree.getName()),
            matchesRangeMap.asMapOfRanges().entrySet().stream()
                .filter(e -> !e.getValue().equals(methodName))
                .map(
                    e ->
                        String.format(
                            "Template `%s` matches on line %s, while it should match in a method named `test%s`.",
                            e.getValue(),
                            lineMap.getLineNumber(e.getKey().lowerEndpoint()),
                            e.getValue())),
            state);
      }
      return super.visitMethod(tree, state);
    }
  }
}
