package tech.picnic.errorprone.testngjunit;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static tech.picnic.errorprone.testngjunit.TestNGMatchers.TESTNG_TEST_ANNOTATION;
import static tech.picnic.errorprone.testngjunit.TestNGMatchers.TESTNG_VALUE_FACTORY_METHOD;

import com.google.common.collect.ImmutableMap;
import com.google.errorprone.VisitorState;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * A {@link TreeScanner} which will scan a {@link com.sun.source.tree.CompilationUnitTree} and
 * collect data required for the migration from each class in the compilation unit. <br>
 * This data can be retrieved using {@link #collectMetadataForEachClass(CompilationUnitTree)}
 */
final class TestNGScanner extends TreeScanner<@Nullable Void, TestNGMetadata.Builder> {
  private final VisitorState state;
  private final ImmutableMap.Builder<ClassTree, TestNGMetadata> metadataBuilder =
      ImmutableMap.builder();

  TestNGScanner(VisitorState state) {
    this.state = state;
  }

  @Override
  public @Nullable Void visitClass(ClassTree tree, TestNGMetadata.Builder unused) {
    TestNGMetadata.Builder builder = TestNGMetadata.builder();
    builder.setClassTree(tree);
    builder.setClassLevelAnnotationMetadata(getTestNGAnnotation(tree, state));
    super.visitClass(tree, builder);
    metadataBuilder.put(tree, builder.build());

    return null;
  }

  @Override
  public @Nullable Void visitMethod(MethodTree tree, TestNGMetadata.Builder builder) {
    if (ASTHelpers.isGeneratedConstructor(tree)) {
      return super.visitMethod(tree, builder);
    }

    DataProviderMigrator migrator = new DataProviderMigrator();
    if (TESTNG_VALUE_FACTORY_METHOD.matches(tree, state) && migrator.canFix(tree)) {
      builder
          .dataProviderMetadataBuilder()
          .put(tree.getName().toString(), TestNGMetadata.DataProviderMetadata.create(tree));
      return super.visitMethod(tree, builder);
    }

    getTestNGAnnotation(tree, state)
        .or(builder::getClassLevelAnnotationMetadata)
        .ifPresent(annotation -> builder.methodAnnotationsBuilder().put(tree, annotation));

    return super.visitMethod(tree, builder);
  }

  public ImmutableMap<ClassTree, TestNGMetadata> collectMetadataForEachClass(
      CompilationUnitTree tree) {
    scan(tree, null);
    return metadataBuilder.build();
  }

  private static Optional<TestNGMetadata.AnnotationMetadata> getTestNGAnnotation(
      Tree tree, VisitorState state) {
    return ASTHelpers.getAnnotations(tree).stream()
        .filter(annotation -> TESTNG_TEST_ANNOTATION.matches(annotation, state))
        .findFirst()
        .map(
            annotationTree ->
                TestNGMetadata.AnnotationMetadata.create(
                    annotationTree,
                    annotationTree.getArguments().stream()
                        .filter(AssignmentTree.class::isInstance)
                        .map(AssignmentTree.class::cast)
                        .collect(
                            toImmutableMap(
                                assignment ->
                                    ((IdentifierTree) assignment.getVariable())
                                        .getName()
                                        .toString(),
                                AssignmentTree::getExpression))));
  }
}
