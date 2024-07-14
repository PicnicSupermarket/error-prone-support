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
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreeScanner;
import java.util.Map.Entry;
import java.util.Optional;
import javax.inject.Inject;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.testngjunit.TestNgMetadata.AnnotationMetadata;
import tech.picnic.errorprone.testngjunit.TestNgMetadata.DataProviderMetadata;
import tech.picnic.errorprone.testngjunit.TestNgMetadata.SetupTeardownType;

/**
 * A {@link BugChecker} that migrates TestNG unit tests to JUnit 5.
 *
 * <p>Supported TestNG annotation attributes are:
 *
 * <ul>
 *   <li>{@code dataProvider}
 *   <li>{@code description}
 *   <li>{@code isEnabled}
 *   <li>{@code expectedExceptions}
 *   <li>{@code priority}
 *   <li>{@code groups}
 * </ul>
 *
 * This migration will also take care of any setup/teardown methods.
 *
 * <p>Note: As the {@code @BeforeAll} and {@code @AfterAll} methods in JUnit are required to be
 * static, this <em>might</em> introduce breaking changes.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Migrate TestNG tests to their JUnit equivalent",
    linkType = NONE,
    tags = REFACTORING,
    severity = ERROR)
public final class TestNGJUnitMigration extends BugChecker implements CompilationUnitTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String CONSERVATIVE_MIGRATION_MODE_FLAG =
      "TestNGJUnitMigration:ConservativeMode";
  private static final String BEHAVIOR_PRESERVING_MODE_FLAG =
      "TestNGJUnitMigration:BehaviorPreserving";

  private final boolean conservativeMode;
  private final boolean strictBehaviorPreserving;

  /**
   * Instantiates a new {@link TestNGJUnitMigration} instance. This will default to the aggressive
   * migration mode.
   */
  public TestNGJUnitMigration() {
    this(ErrorProneFlags.empty());
  }

  /**
   * Instantiates a new {@link TestNGJUnitMigration} with the specified {@link ErrorProneFlags}.
   *
   * @param flags The Error Prone flags used to set the migration mode.
   */
  @Inject
  TestNGJUnitMigration(ErrorProneFlags flags) {
    conservativeMode = flags.getBoolean(CONSERVATIVE_MIGRATION_MODE_FLAG).orElse(false);
    strictBehaviorPreserving = flags.getBoolean(BEHAVIOR_PRESERVING_MODE_FLAG).orElse(false);
  }

  @Override
  public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
    TestNGScanner scanner = new TestNGScanner(state);
    ImmutableMap<ClassTree, TestNgMetadata> classMetaData = scanner.collectMetadataForClasses(tree);

    new TreeScanner<@Nullable Void, TestNgMetadata>() {
      @Override
      public @Nullable Void visitClass(ClassTree node, TestNgMetadata testNgMetadata) {
        TestNgMetadata metadata = classMetaData.get(node);
        if (metadata == null) {
          return super.visitClass(node, testNgMetadata);
        }

        for (DataProviderMetadata dataProviderMetadata : metadata.getDataProvidersInUse()) {
          // XXX: Make this configurable.
          MethodTree methodTree = dataProviderMetadata.getMethodTree();
          AnnotationTree dpAnnotation =
              ASTHelpers.getAnnotationWithSimpleName(
                  ASTHelpers.getAnnotations(methodTree), "DataProvider");
          if (dpAnnotation != null) {
            state.reportMatch(describeMatch(dpAnnotation, SuggestedFix.delete(dpAnnotation)));
          }
          //          DataProviderMigrator.createFix(
          //                  metadata.getClassTree(), dataProviderMetadata.getMethodTree(), state)
          //              .ifPresent(
          //                  fix ->
          //                      state.reportMatch(
          //                          describeMatch(
          //                              dataProviderMetadata.getMethodTree(),
          //
          // fix.toBuilder().removeStaticImport("org.testng.Assert.*").build())));
        }

        for (Entry<MethodTree, SetupTeardownType> entry : metadata.getSetupTeardown().entrySet()) {
          SetupTeardownMethodMigrator.createFix(entry.getKey(), entry.getValue(), state)
              .ifPresent(fix -> state.reportMatch(describeMatch(entry.getKey(), fix)));
        }

        super.visitClass(node, metadata);
        return null;
      }

      @Override
      public @Nullable Void visitMethod(MethodTree tree, TestNgMetadata metadata) {
        /* Make sure ALL Tests in the class can be migrated. */
        if (conservativeMode && !canMigrateAllTestsInClass(metadata, state)) {
          return super.visitMethod(tree, metadata);
        }

        metadata
            .getAnnotation(tree)
            .ifPresent(
                annotation -> {
                  SuggestedFix.Builder fixBuilder = SuggestedFix.builder();
                  buildAttributeFixes(metadata, annotation, tree, state).forEach(fixBuilder::merge);

                  fixBuilder.merge(migrateAnnotation(annotation, tree));
                  state.reportMatch(describeMatch(tree, fixBuilder.build()));
                });
        return super.visitMethod(tree, metadata);
      }
    }.scan(tree, null);

    /* All suggested fixes are already directly reported to the `VisitorState`. */
    return Description.NO_MATCH;
  }

  private static ImmutableList<SuggestedFix> buildAttributeFixes(
      TestNgMetadata metadata,
      AnnotationMetadata annotationMetadata,
      MethodTree methodTree,
      VisitorState state) {
    return annotationMetadata.getAttributes().entrySet().stream()
        .flatMap(
            entry ->
                trySuggestFix(metadata, annotationMetadata, entry.getKey(), methodTree, state)
                    .stream())
        .collect(toImmutableList());
  }

  private static boolean canMigrateTest(
      MethodTree methodTree,
      TestNgMetadata metadata,
      AnnotationMetadata annotationMetadata,
      VisitorState state) {
    ImmutableList<TestAnnotationAttribute> attributes =
        annotationMetadata.getAttributes().keySet().stream()
            .map(TestAnnotationAttribute::fromString)
            .flatMap(Optional::stream)
            .collect(toImmutableList());
    return (annotationMetadata.getAttributes().isEmpty() || !attributes.isEmpty())
        && attributes.stream()
            .allMatch(
                kind ->
                    kind.getAttributeMigrator()
                        .migrate(metadata, annotationMetadata, methodTree, state)
                        .isPresent());
  }

  private static boolean canMigrateAllTestsInClass(TestNgMetadata metadata, VisitorState state) {
    return metadata.getMethodAnnotations().entrySet().stream()
        .allMatch(entry -> canMigrateTest(entry.getKey(), metadata, entry.getValue(), state));
  }

  private static Optional<SuggestedFix> trySuggestFix(
      TestNgMetadata metadata,
      AnnotationMetadata annotation,
      String attributeName,
      MethodTree methodTree,
      VisitorState state) {
    return TestAnnotationAttribute.fromString(attributeName)
        .map(TestAnnotationAttribute::getAttributeMigrator)
        .flatMap(migrator -> migrator.migrate(metadata, annotation, methodTree, state))
        .or(
            () ->
                UnsupportedAttributeMigrator.migrate(annotation, methodTree, attributeName, state));
  }

  private static SuggestedFix migrateAnnotation(
      AnnotationMetadata annotationMetadata, MethodTree methodTree) {
    SuggestedFix.Builder fixBuilder =
        SuggestedFix.builder().delete(annotationMetadata.getAnnotationTree());
    if (!annotationMetadata.getAttributes().containsKey("dataProvider")
        && !annotationMetadata.getAttributes().containsKey("expectedExceptions")) {
      fixBuilder.prefixWith(methodTree, "@org.junit.jupiter.api.Test\n    ");
    }

    return fixBuilder.build();
  }
}
