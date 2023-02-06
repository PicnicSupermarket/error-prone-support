package tech.picnic.errorprone.bugpatterns.testngtojunit;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.REFACTORING;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
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
import org.testng.annotations.Test;
import tech.picnic.errorprone.bugpatterns.testngtojunit.migrators.AnnotationMigrator;

/**
 * A {@link BugChecker} that migrates TestNG unit tests to JUnit 5.
 *
 * <p>Supported TestNG annotation arguments are:
 *
 * <ul>
 *   <li>{@link Test#dataProvider()}
 *   <li>{@link Test#description()}
 *   <li>{@link Test#expectedExceptions()}
 *   <li>{@link Test#priority()}
 * </ul>
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Migrate TestNG tests to JUnit",
    linkType = NONE,
    tags = REFACTORING,
    severity = ERROR)
public final class TestNGJUnitMigration extends BugChecker implements CompilationUnitTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String CONSERVATIVE_MIGRATION_MODE_FLAG =
      "TestNGJUnitMigration:ConservativeMode";
  private final boolean conservativeMode;

  /**
   * Instantiates a new {@link TestNGJUnitMigration} instance. This will default to aggressive
   * migration mode.
   */
  public TestNGJUnitMigration() {
    this(ErrorProneFlags.empty());
  }

  /**
   * Instantiates a new {@link TestNGJUnitMigration} with the specified {@link ErrorProneFlags}.
   *
   * @param flags the error-prone flags used to set the migration mode
   */
  public TestNGJUnitMigration(ErrorProneFlags flags) {
    this.conservativeMode = flags.getBoolean(CONSERVATIVE_MIGRATION_MODE_FLAG).orElse(false);
  }

  @Override
  public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
    TestNGScanner scanner = new TestNGScanner(state);
    scanner.scan(tree, null);
    ImmutableMap<ClassTree, TestNGMetadata> classMetaData = scanner.buildMetaDataForEachClassTree();

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
            TestNGMigrationContext.create(conservativeMode, metaData.getClassTree());

        /* Make sure ALL tests in the class can be migrated. */
        if (context.isConservativeMode()
            && !metaData.getAnnotations().stream()
                .allMatch(
                    annotation ->
                        canMigrateTest(
                            metaData.getClassTree(), tree, metaData, annotation, state))) {
          return super.visitMethod(tree, metaData);
        }

        metaData
            .getAnnotation(tree)
            .filter(
                annotation ->
                    canMigrateTest(metaData.getClassTree(), tree, metaData, annotation, state))
            .ifPresent(
                annotation -> {
                  SuggestedFix.Builder fixBuilder = SuggestedFix.builder();

                  // migrate arguments
                  buildArgumentFixes(metaData.getClassTree(), annotation, tree, state)
                      .forEach(fixBuilder::merge);

                  // @Test annotation fix
                  //                  fixBuilder.merge(buildAnnotationFixes(annotation, tree));
                  new AnnotationMigrator()
                      .createFix(metaData.getClassTree(), tree, annotation, state)
                      .ifPresent(fixBuilder::merge);

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
      ClassTree classTree,
      TestNGMetadata.AnnotationMetadata annotationMetadata,
      MethodTree methodTree,
      VisitorState state) {
    return annotationMetadata.getArguments().entrySet().stream()
        .flatMap(
            entry ->
                trySuggestFix(classTree, methodTree, entry.getKey(), entry.getValue(), state)
                    .stream())
        .collect(toImmutableList());
  }

  private static SuggestedFix buildAnnotationFixes(
      TestNGMetadata.AnnotationMetadata annotationMetadata, MethodTree methodTree) {
    SuggestedFix.Builder builder =
        SuggestedFix.builder().merge(SuggestedFix.delete(annotationMetadata.getAnnotationTree()));
    if (!annotationMetadata.getArguments().containsKey("dataProvider")) {
      builder
          .addImport("org.junit.jupiter.api.Test")
          .merge(SuggestedFix.prefixWith(methodTree, "@Test\n"));
      //      String dataProviderName =
      //          SourceCode.treeToString(annotationMetadata.getArguments().get("dataProvider"),
      // state);
      //      builder
      //          .addImport("org.junit.jupiter.params.ParameterizedTest")
      //          .addImport("org.junit.jupiter.params.provider.MethodSource")
      //          .merge(
      //              SuggestedFix.prefixWith(
      //                  methodTree, "@ParameterizedTest\n  @MethodSource(" + dataProviderName +
      // ")\n"));
    }

    return builder.build();
  }

  private static boolean canMigrateTest(
      ClassTree classTree,
      MethodTree methodTree,
      TestNGMetadata metadata,
      TestNGMetadata.AnnotationMetadata annotationMetadata,
      VisitorState state) {
    return annotationMetadata.getArguments().keySet().stream()
        .map(SupportedArgumentKind::fromString)
        .flatMap(Optional::stream)
        .allMatch(
            kind ->
                kind.getArgumentMigrator().canFix(metadata, annotationMetadata, methodTree, state));
  }

  private static Optional<SuggestedFix> trySuggestFix(
      ClassTree classTree,
      MethodTree methodTree,
      String argumentName,
      ExpressionTree argumentContent,
      VisitorState state) {
    return SupportedArgumentKind.fromString(argumentName)
        .map(SupportedArgumentKind::getArgumentMigrator)
        .flatMap(fixer -> fixer.createFix(classTree, methodTree, argumentContent, state));
  }
}
