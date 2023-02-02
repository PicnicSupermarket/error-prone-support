package tech.picnic.errorprone.bugpatterns.testngtojunit;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * POJO containing data collected using {@link TestNGScanner} for use in {@link
 * TestNGJUnitMigration}.
 */
public final class TestNGMetadata {
  private final ClassTree classTree;
  private AnnotationMetadata classLevelAnnotationMetadata;
  private final Map<MethodTree, AnnotationMetadata> methodAnnotations = new HashMap<>();
  private final List<DataProviderMetadata> dataProviderMetadata = new ArrayList<>();

  TestNGMetadata(ClassTree classTree) {
    this.classTree = classTree;
  }

  ImmutableSet<AnnotationMetadata> getAnnotations() {
    return ImmutableSet.copyOf(methodAnnotations.values());
  }

  Optional<AnnotationMetadata> getAnnotation(MethodTree methodTree) {
    return Optional.ofNullable(methodAnnotations.get(methodTree));
  }

  void addTestAnnotation(MethodTree methodTree, AnnotationMetadata annotationMetadata) {
    methodAnnotations.put(methodTree, annotationMetadata);
  }

  void setClassLevelAnnotation(AnnotationMetadata classLevelAnnotationMetadata) {
    this.classLevelAnnotationMetadata = classLevelAnnotationMetadata;
  }

  Optional<AnnotationMetadata> getClassLevelAnnotation() {
    return Optional.ofNullable(classLevelAnnotationMetadata);
  }

  ClassTree getClassTree() {
    return classTree;
  }

  void addDataProvider(MethodTree methodTree) {
    dataProviderMetadata.add(DataProviderMetadata.create(methodTree));
  }

  ImmutableList<DataProviderMetadata> getDataProviders() {
    return ImmutableList.copyOf(dataProviderMetadata);
  }

  /**
   * POJO containing data for a specific {@link org.testng.annotations.Test} annotation for use in
   * {@link TestNGJUnitMigration}.
   */
  @AutoValue
  public abstract static class AnnotationMetadata {
    abstract AnnotationTree getAnnotationTree();

    /**
     * A mapping for all arguments in the annotation to their value.
     *
     * @return an {@link ImmutableMap} mapping each annotation argument to their respective value.
     */
    public abstract ImmutableMap<String, ExpressionTree> getArguments();

    /**
     * Instantiate a new {@link AnnotationMetadata}.
     *
     * @param annotationTree the annotation tree
     * @param arguments the arguments in that annotation tree
     * @return the new {@link AnnotationMetadata} instance
     */
    public static AnnotationMetadata create(
        AnnotationTree annotationTree, ImmutableMap<String, ExpressionTree> arguments) {
      return new AutoValue_TestNGMetadata_AnnotationMetadata(annotationTree, arguments);
    }
  }

  /**
   * POJO containing data for a specific {@link org.testng.annotations.DataProvider} annotation for
   * use in {@link TestNGJUnitMigration}.
   */
  @AutoValue
  public abstract static class DataProviderMetadata {

    abstract MethodTree getMethodTree();

    abstract String getName();

    /**
     * Instantiate a new {@link DataProviderMetadata} instance.
     *
     * @param methodTree the value factory method tree
     * @return a new {@link DataProviderMetadata} instance
     */
    public static DataProviderMetadata create(MethodTree methodTree) {
      return new AutoValue_TestNGMetadata_DataProviderMetadata(
          methodTree, methodTree.getName().toString());
    }
  }
}
