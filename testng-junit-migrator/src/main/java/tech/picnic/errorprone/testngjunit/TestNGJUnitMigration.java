package tech.picnic.errorprone.testngjunit;

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
import tech.picnic.errorprone.testngjunit.TestNGMetadata.AnnotationMetadata;

/**
 * A {@link BugChecker} that migrates TestNG unit tests to JUnit 5.
 *
 * <p>Supported TestNG annotation arguments are:
 *
 * <ul>
 *   <li>{@code dataProvider}
 *   <li>{@code description}
 *   <li>{@code expectedExceptions}
 *   <li>{@code priority}
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
   * @param flags the error-prone flags used to set the migration mode.
   */
  public TestNGJUnitMigration(ErrorProneFlags flags) {
    this.conservativeMode = flags.getBoolean(CONSERVATIVE_MIGRATION_MODE_FLAG).orElse(false);
  }

  @Override
  public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
    TestNGScanner scanner = new TestNGScanner(state);
    ImmutableMap<ClassTree, TestNGMetadata> classMetaData = scanner.collectMetadataForClasses(tree);

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
        /* Make sure ALL tests in the class can be migrated. */
        if (conservativeMode
            && !metaData.getAnnotations().stream()
                .allMatch(annotation -> canMigrateTest(tree, metaData, annotation, state))) {
          return super.visitMethod(tree, metaData);
        }

        metaData
            .getDataProvidersInUse()
            .forEach(
                dataProviderMetadata -> {
                  new DataProviderMigrator()
                      .createFix(
                          metaData.getClassTree(), dataProviderMetadata.getMethodTree(), state)
                      .ifPresent(
                          fix ->
                              state.reportMatch(
                                  describeMatch(dataProviderMetadata.getMethodTree(), fix)));
                });

        metaData
            .getSetupTeardown()
            .forEach(
                (method, type) -> {
                  new SetupTeardownMethodMigrator()
                      .createFix(method, type, state)
                      .ifPresent(fix -> state.reportMatch(describeMatch(method, fix)));
                });

        metaData
            .getAnnotation(tree)
            .filter(annotation -> canMigrateTest(tree, metaData, annotation, state))
            .ifPresent(
                annotation -> {
                  SuggestedFix.Builder fixBuilder = SuggestedFix.builder();
                  buildArgumentFixes(metaData.getClassTree(), annotation, tree, state)
                      .forEach(fixBuilder::merge);

                  fixBuilder.merge(migrateAnnotation(annotation, tree));

                  state.reportMatch(describeMatch(tree, fixBuilder.build()));
                });
        return super.visitMethod(tree, metaData);
      }
    }.scan(tree, null);

    /* All suggested fixes are directly reported to the visitor state already! */
    return Description.NO_MATCH;
  }

  private static ImmutableList<SuggestedFix> buildArgumentFixes(
      ClassTree classTree,
      AnnotationMetadata annotationMetadata,
      MethodTree methodTree,
      VisitorState state) {
    return annotationMetadata.getArguments().entrySet().stream()
        .flatMap(
            entry ->
                trySuggestFix(classTree, methodTree, entry.getKey(), entry.getValue(), state)
                    .stream())
        .collect(toImmutableList());
  }

  private static boolean canMigrateTest(
      MethodTree methodTree,
      TestNGMetadata metadata,
      AnnotationMetadata annotationMetadata,
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

  private static SuggestedFix migrateAnnotation(
      AnnotationMetadata annotationMetadata, MethodTree methodTree) {
    SuggestedFix.Builder fixBuilder =
        SuggestedFix.builder().merge(SuggestedFix.delete(annotationMetadata.getAnnotationTree()));
    if (!annotationMetadata.getArguments().containsKey("dataProvider")) {
      fixBuilder.merge(SuggestedFix.prefixWith(methodTree, "@org.junit.jupiter.api.Test\n"));
    }

    return fixBuilder.build();
  }
}
