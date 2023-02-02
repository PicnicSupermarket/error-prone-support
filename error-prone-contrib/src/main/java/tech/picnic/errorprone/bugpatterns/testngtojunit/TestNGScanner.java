package tech.picnic.errorprone.bugpatterns.testngtojunit;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static tech.picnic.errorprone.bugpatterns.testngtojunit.TestNGMatchers.TESTNG_TEST_ANNOTATION;
import static tech.picnic.errorprone.bugpatterns.testngtojunit.TestNGMatchers.TESTNG_VALUE_FACTORY_METHOD;

import com.google.common.collect.ImmutableMap;
import com.google.errorprone.VisitorState;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * A {@link TreeScanner} which will scan a {@link com.sun.source.tree.CompilationUnitTree} and
 * collect data required for the migration from each class in the compilation unit. <br>
 * This data can be retrieved using {@link #buildMetaDataForEachClassTree()}.
 */
final class TestNGScanner extends TreeScanner<@Nullable Void, TestNGMetadata> {
  private final VisitorState state;
  private final ImmutableMap.Builder<ClassTree, TestNGMetadata> metadataBuilder =
      ImmutableMap.builder();

  TestNGScanner(VisitorState state) {
    this.state = state;
  }

  @Override
  public @Nullable Void visitClass(ClassTree tree, TestNGMetadata testNGMetadata) {
    TestNGMetadata meta = new TestNGMetadata(tree);
    getTestNGAnnotation(tree, state).ifPresent(meta::setClassLevelAnnotation);
    super.visitClass(tree, meta);
    metadataBuilder.put(tree, meta);

    return null;
  }

  @Override
  public @Nullable Void visitMethod(MethodTree tree, TestNGMetadata testNGMetadata) {
    if (ASTHelpers.isGeneratedConstructor(tree)) {
      return super.visitMethod(tree, testNGMetadata);
    }

    if (TESTNG_VALUE_FACTORY_METHOD.matches(tree, state)) {
      testNGMetadata.addDataProvider(tree);
      return super.visitMethod(tree, testNGMetadata);
    }

    getTestNGAnnotation(tree, state)
        .or(testNGMetadata::getClassLevelAnnotation)
        .ifPresent(annotation -> testNGMetadata.addTestAnnotation(tree, annotation));

    return super.visitMethod(tree, testNGMetadata);
  }

  public ImmutableMap<ClassTree, TestNGMetadata> buildMetaDataForEachClassTree() {
    return metadataBuilder.build();
  }

  private static Optional<TestNGMetadata.AnnotationMetadata> getTestNGAnnotation(
      Tree tree, VisitorState state) {
    // XXX: I think there is a method to get a specific annotation from a tree. Not sure if that is
    // usable here though.
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
