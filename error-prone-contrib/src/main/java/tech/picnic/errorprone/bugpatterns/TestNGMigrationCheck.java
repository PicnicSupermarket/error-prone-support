package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.REFACTORING;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreeScanner;
import java.util.Optional;
import tech.picnic.errorprone.bugpatterns.testmigrator.SupportedArgumentKind;
import tech.picnic.errorprone.bugpatterns.testmigrator.TestNGMigrationContext;
import tech.picnic.errorprone.bugpatterns.testmigrator.TestNGScanner;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

// XXX: Also here and other places. Try to add more Javadocs :D.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Migrate TestNG tests to JUnit",
    linkType = NONE,
    tags = REFACTORING,
    severity = ERROR)
public final class TestNGMigrationCheck extends BugChecker implements CompilationUnitTreeMatcher {
  private static final long serialVersionUID = 1L;

  // XXX: Default constructor komen.
  // XXX: Constructor met parameter voor "aggressive" mode. Add tests for this as well :wink:

  @Override
  public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
    TestNGScanner scanner = new TestNGScanner(state);
    scanner.scan(tree, null);
    ImmutableMap<ClassTree, TestNGMetadata> metadataMap = scanner.buildMetaDataTree();
    // XXX: Don't use map suffix. Try to come with more meaningful name :).

    new TreeScanner<Void, TestNGMetadata>() {
      @Override
      public Void visitClass(ClassTree node, TestNGMetadata testNGMetadata) {
        TestNGMetadata metadata = metadataMap.getOrDefault(node, null);
        if (metadata == null) {
          return super.visitClass(node, testNGMetadata);
        }

        super.visitClass(node, metadata);
        return null;
      }

      @Override
      public Void visitMethod(MethodTree tree, TestNGMetadata metaData) {
        TestNGMigrationContext context = new TestNGMigrationContext(metaData.getClassTree());
        metaData
            .getAnnotation(tree)
            .filter(annotation -> SupportedArgumentKind.canMigrateTest(context, annotation))
            .ifPresent(
                annotation -> {
                  SuggestedFix.Builder fixBuilder = SuggestedFix.builder();
                  buildArgumentFixes(context, annotation, tree, state).forEach(fixBuilder::merge);
                  fixBuilder.merge(buildAnnotationFixes(annotation, tree, state));

                  state.reportMatch(
                      describeMatch(annotation.getAnnotationTree(), fixBuilder.build()));
                });
        return super.visitMethod(tree, metaData);
      }
    }.scan(tree, null);

    /* All suggested fixes are directly reported to the visitor state already! */
    return Description.NO_MATCH;
  }

  private ImmutableList<SuggestedFix> buildArgumentFixes(
      TestNGMigrationContext context,
      TestNGMetadata.TestNGAnnotation annotation,
      MethodTree methodTree,
      VisitorState state) {
    return annotation.getArguments().entrySet().stream()
        .map(
            entry ->
                trySuggestFix(context, methodTree, entry.getKey(), entry.getValue(), state)
                    .orElseThrow())
        .collect(toImmutableList());
  }

  private static SuggestedFix buildAnnotationFixes(
      TestNGMetadata.TestNGAnnotation annotation, MethodTree methodTree, VisitorState state) {
    // XXX: Should we remove the qualifier; TestNGMetadata? Maybe change name of TestNGAnnotation to
    // not need the qualifier?
    SuggestedFix.Builder builder =
        SuggestedFix.builder().merge(SuggestedFix.delete(annotation.getAnnotationTree()));
    if (annotation.getArgumentNames().contains("dataProvider")) {
      String dataProviderName =
          SourceCode.treeToString(annotation.getArguments().get("dataProvider"), state);
      builder
          .addImport("org.junit.jupiter.params.ParameterizedTest")
          .addImport("org.junit.jupiter.params.provider.MethodSource")
          .removeImport("org.testng.annotations.Test")
          .merge(
              SuggestedFix.prefixWith(
                  methodTree, "@ParameterizedTest\n  @MethodSource(" + dataProviderName + ")\n"));
    } else {
      builder
          .removeImport("org.testng.annotations.Test")
          .addImport("org.junit.jupiter.api.Test")
          .merge(SuggestedFix.prefixWith(methodTree, "@Test\n"));
    }

    return builder.build();
  }

  private static Optional<SuggestedFix> trySuggestFix(
      TestNGMigrationContext context,
      MethodTree methodTree,
      String argumentName,
      ExpressionTree argumentContent,
      VisitorState state) {
    // XXX: Come up with more concrete name of `matchArgument`?
    return SupportedArgumentKind.matchArgument(argumentName)
        .map(SupportedArgumentKind::getArgumentMigrator)
        .map(fixer -> fixer.createFix(context, methodTree, argumentContent, state));
  }
}
