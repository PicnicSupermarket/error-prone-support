package tech.picnic.errorprone.testngjunit;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import java.util.Map;
import java.util.Optional;

/**
 * POJO containing data collected using {@link TestNGScanner} for use in {@link
 * TestNGJUnitMigration}.
 */
@AutoValue
public abstract class TestNGMetadata {
  abstract ClassTree getClassTree();

  abstract Optional<AnnotationMetadata> getClassLevelAnnotationMetadata();

  abstract ImmutableMap<MethodTree, AnnotationMetadata> getMethodAnnotations();

  /**
   * Get the {@code Test}s that are able to migratable.
   *
   * @return an {@link ImmutableMap} mapping the name of the data provider to its respective
   *     metadata.
   */
  public abstract ImmutableMap<String, DataProviderMetadata> getDataProviderMetadata();

  final ImmutableList<DataProviderMetadata> getDataProvidersInUse() {
    return getDataProviderMetadata().entrySet().stream()
        .filter(
            entry ->
                getAnnotations().stream()
                    .anyMatch(
                        annotation -> {
                          ExpressionTree dataProviderNameExpression =
                              annotation.getArguments().get("dataProvider");
                          if (dataProviderNameExpression == null) {
                            return false;
                          }

                          return ((LiteralTree) dataProviderNameExpression)
                              .getValue()
                              .equals(entry.getKey());
                        }))
        .map(Map.Entry::getValue)
        .collect(toImmutableList());
  }

  static Builder builder() {
    return new AutoValue_TestNGMetadata.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    private final ImmutableMap.Builder<MethodTree, AnnotationMetadata> methodAnnotationsBuilder =
        ImmutableMap.builder();

    private final ImmutableMap.Builder<String, DataProviderMetadata> dataProviderMetadataBuilder =
        ImmutableMap.builder();

    abstract Builder setClassTree(ClassTree value);

    abstract Optional<AnnotationMetadata> getClassLevelAnnotationMetadata();

    abstract Builder setClassLevelAnnotationMetadata(Optional<AnnotationMetadata> value);

    abstract Builder setMethodAnnotations(ImmutableMap<MethodTree, AnnotationMetadata> value);

    ImmutableMap.Builder<MethodTree, AnnotationMetadata> methodAnnotationsBuilder() {
      return methodAnnotationsBuilder;
    }

    abstract Builder setDataProviderMetadata(ImmutableMap<String, DataProviderMetadata> value);

    ImmutableMap.Builder<String, DataProviderMetadata> dataProviderMetadataBuilder() {
      return dataProviderMetadataBuilder;
    }

    abstract TestNGMetadata autoBuild();

    TestNGMetadata build() {
      setMethodAnnotations(methodAnnotationsBuilder.build());
      setDataProviderMetadata(dataProviderMetadataBuilder.build());
      return autoBuild();
    }
  }

  final ImmutableSet<AnnotationMetadata> getAnnotations() {
    return ImmutableSet.copyOf(getMethodAnnotations().values());
  }

  final Optional<AnnotationMetadata> getAnnotation(MethodTree methodTree) {
    return Optional.ofNullable(getMethodAnnotations().get(methodTree));
  }

  /**
   * POJO containing data for a specific {@code Test} annotation for use in {@link
   * TestNGJUnitMigration}.
   */
  @AutoValue
  public abstract static class AnnotationMetadata {
    /**
     * Get the {@link AnnotationTree} of this metadata instance.
     *
     * @return the annotation tree this metadata contains information on.
     */
    public abstract AnnotationTree getAnnotationTree();

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
   * POJO containing data for a specific {@code DataProvider} annotation for use in {@link
   * TestNGJUnitMigration}.
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
