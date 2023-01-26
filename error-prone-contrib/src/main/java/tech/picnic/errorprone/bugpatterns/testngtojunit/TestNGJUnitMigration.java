package tech.picnic.errorprone.bugpatterns.testngtojunit;

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
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

// XXX: Also here and other places. Try to add more Javadocs :D.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Migrate TestNG tests to JUnit",
    linkType = NONE,
    tags = REFACTORING,
    severity = ERROR)
public final class TestNGJUnitMigration extends BugChecker implements CompilationUnitTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String ENABLE_AGGRESSIVE_MIGRATION_MODE_FLAG =
      "ErrorProneSupport:AggressiveTestNGJUnitMigration";

  public TestNGJUnitMigration() {}

  @Override
  public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
    TestNGScanner scanner = new TestNGScanner(state);
    scanner.scan(tree, null);
    ImmutableMap<ClassTree, TestNGMetadata> classMetaData = scanner.buildMetaDataTree();

    new TreeScanner<@Nullable Void, TestNGMetadata>() {
      @Override
      public @Nullable Void visitClass(ClassTree node, TestNGMetadata testNGMetadata) {
        TestNGMetadata metadata = classMetaData.get(node);
        if (metadata == null) {
          return super.visitClass(node, testNGMetadata);
        }

        super.visitClass(node, metadata);
        return null;
      }

      @Override
      public @Nullable Void visitMethod(MethodTree tree, TestNGMetadata metaData) {
        TestNGMigrationContext context =
            new TestNGMigrationContext(isAggressiveMode(state), metaData.getClassTree());

        // make sure we ALL tests in the class can be migrated
        if (!context.isAggressiveMigration()
            && !metaData.getAnnotations().stream()
                .allMatch(annotation -> canMigrateTest(context, annotation))) {
          return super.visitMethod(tree, metaData);
        }

        metaData
            .getAnnotation(tree)
            .filter(annotation -> canMigrateTest(context, annotation))
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

  private static ImmutableList<SuggestedFix> buildArgumentFixes(
      TestNGMigrationContext context,
      TestNGMetadata.Annotation annotation,
      MethodTree methodTree,
      VisitorState state) {
    return annotation.getArguments().entrySet().stream()
        .flatMap(
            entry ->
                trySuggestFix(context, methodTree, entry.getKey(), entry.getValue(), state)
                    .stream())
        .collect(toImmutableList());
  }

  private static SuggestedFix buildAnnotationFixes(
      TestNGMetadata.Annotation annotation, MethodTree methodTree, VisitorState state) {
    SuggestedFix.Builder builder =
        SuggestedFix.builder().merge(SuggestedFix.delete(annotation.getAnnotationTree()));
    if (annotation.getArgumentNames().contains("dataProvider")) {
      String dataProviderName =
          SourceCode.treeToString(annotation.getArguments().get("dataProvider"), state);
      builder
          .addImport("org.junit.jupiter.params.ParameterizedTest")
          .addImport("org.junit.jupiter.params.provider.MethodSource")
          .merge(
              SuggestedFix.prefixWith(
                  methodTree, "@ParameterizedTest\n  @MethodSource(" + dataProviderName + ")\n"));
    } else {
      builder
          .addImport("org.junit.jupiter.api.Test")
          .merge(SuggestedFix.prefixWith(methodTree, "@Test\n"));
    }

    return builder.build();
  }

  private static boolean canMigrateTest(
      TestNGMigrationContext context, TestNGMetadata.Annotation annotation) {
    return annotation.getArgumentNames().stream()
        .map(SupportedArgumentKind::fromString)
        .flatMap(Optional::stream)
        .allMatch(kind -> kind.getArgumentMigrator().canFix(context, annotation));
  }

  private static Optional<SuggestedFix> trySuggestFix(
      TestNGMigrationContext context,
      MethodTree methodTree,
      String argumentName,
      ExpressionTree argumentContent,
      VisitorState state) {
    return SupportedArgumentKind.fromString(argumentName)
        .map(SupportedArgumentKind::getArgumentMigrator)
        .flatMap(fixer -> fixer.createFix(context, methodTree, argumentContent, state));
  }

  private static boolean isAggressiveMode(VisitorState state) {
    return state
        .errorProneOptions()
        .getFlags()
        .getBoolean(ENABLE_AGGRESSIVE_MIGRATION_MODE_FLAG)
        .orElse(true);
  }
}
