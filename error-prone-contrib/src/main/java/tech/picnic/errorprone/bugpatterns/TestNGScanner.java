package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static tech.picnic.errorprone.bugpatterns.testmigrator.TestNGUtil.TESTNG_ANNOTATION;
import static tech.picnic.errorprone.bugpatterns.testmigrator.TestNGUtil.VALUE_FACTORY_METHOD;

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

public class TestNGScanner extends TreeScanner<Void, TestNGMetadata> {
  private final VisitorState state;
  private final ImmutableMap.Builder<ClassTree, TestNGMetadata> metadataBuilder =
      new ImmutableMap.Builder<>();

  public TestNGScanner(VisitorState state) {
    this.state = state;
  }

  @Override
  public Void visitClass(ClassTree tree, TestNGMetadata testNGMetadata) {
    TestNGMetadata meta = new TestNGMetadata(tree);
    getTestNGAnnotation(tree, state).ifPresent(meta::setClassLevelAnnotation);
    super.visitClass(tree, meta);
    metadataBuilder.put(tree, meta);

    return null;
  }

  @Override
  public Void visitMethod(MethodTree tree, TestNGMetadata testNGMetadata) {
    if (ASTHelpers.isGeneratedConstructor(tree)) {
      return super.visitMethod(tree, testNGMetadata);
    }

    if (VALUE_FACTORY_METHOD.matches(tree, state)) {
      testNGMetadata.addDataProvider(tree);
      return super.visitMethod(tree, testNGMetadata);
    }

    TestNGMetadata.TestNGAnnotation annotation =
        getTestNGAnnotation(tree, state)
            .orElse(testNGMetadata.getClassLevelAnnotation().orElse(null));
    if (annotation == null) {
      return super.visitMethod(tree, testNGMetadata);
    }
    testNGMetadata.addTestAnnotation(tree, annotation);
    return super.visitMethod(tree, testNGMetadata);
  }

  public ImmutableMap<ClassTree, TestNGMetadata> buildMetaDataTree() {
    return metadataBuilder.build();
  }

  private static Optional<TestNGMetadata.TestNGAnnotation> getTestNGAnnotation(
      Tree tree, VisitorState state) {
    return ASTHelpers.getAnnotations(tree).stream()
        .filter(annotation -> TESTNG_ANNOTATION.matches(annotation, state))
        .findFirst()
        .map(
            annotationTree ->
                new TestNGMetadata.TestNGAnnotation(
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
