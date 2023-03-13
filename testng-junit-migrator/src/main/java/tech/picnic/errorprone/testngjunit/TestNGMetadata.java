package tech.picnic.errorprone.testngjunit;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.isType;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * POJO containing data collected using {@link TestNGScanner} for use in {@link
 * TestNGJUnitMigration}.
 */
@AutoValue
abstract class TestNGMetadata {
  abstract ClassTree getClassTree();

  abstract Optional<AnnotationMetadata> getClassLevelAnnotationMetadata();

  abstract ImmutableMap<MethodTree, AnnotationMetadata> getMethodAnnotations();

  abstract ImmutableMap<MethodTree, SetupTeardownType> getSetupMethods();

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

    private final ImmutableMap.Builder<MethodTree, SetupTeardownType> setupMethodsBuilder =
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

    abstract Builder setSetupMethods(ImmutableMap<MethodTree, SetupTeardownType> value);

    ImmutableMap.Builder<MethodTree, SetupTeardownType> setupMethodsBuilder() {
      return setupMethodsBuilder;
    }

    abstract Builder setDataProviderMetadata(ImmutableMap<String, DataProviderMetadata> value);

    ImmutableMap.Builder<String, DataProviderMetadata> dataProviderMetadataBuilder() {
      return dataProviderMetadataBuilder;
    }

    abstract TestNGMetadata autoBuild();

    TestNGMetadata build() {
      setMethodAnnotations(methodAnnotationsBuilder.build());
      setSetupMethods(setupMethodsBuilder.build());
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

  @SuppressWarnings("ImmutableEnumChecker" /* Matcher instances are final. */)
  public enum SetupTeardownType {
    BEFORE_CLASS(
        "org.testng.annotations.BeforeClass",
        "org.junit.jupiter.api.BeforeAll",
        /* requiresStaticMethod= */ true),
    BEFORE_METHOD(
        "org.testng.annotations.BeforeMethod",
        "org.junit.jupiter.api.BeforeEach",
        /* requiresStaticMethod= */ false),
    AFTER_CLASS(
        "org.testng.annotations.AfterClass",
        "org.junit.jupiter.api.AfterAll",
        /* requiresStaticMethod= */ true),
    AFTER_METHOD(
        "org.testng.annotations.AfterMethod",
        "org.junit.jupiter.api.AfterEach",
        /* requiresStaticMethod= */ false);

    private final Matcher<AnnotationTree> annotationMatcher;
    private final Matcher<MethodTree> methodTreeMatcher;
    private final String junitAnnotationClass;
    private final boolean requiresStaticMethod;

    SetupTeardownType(
        String testNGAnnotationClass, String junitAnnotationClass, boolean requiresStaticMethod) {
      this.annotationMatcher = isType(testNGAnnotationClass);
      this.methodTreeMatcher = hasAnnotation(testNGAnnotationClass);
      this.junitAnnotationClass = junitAnnotationClass;
      this.requiresStaticMethod = requiresStaticMethod;
    }

    static Optional<SetupTeardownType> matchType(MethodTree methodTree, VisitorState state) {
      return Arrays.stream(values())
          .filter(value -> value.methodTreeMatcher.matches(methodTree, state))
          .findFirst();
    }

    Matcher<AnnotationTree> getAnnotationMatcher() {
      return annotationMatcher;
    }

    String getJunitAnnotationClass() {
      return junitAnnotationClass;
    }

    boolean requiresStaticMethod() {
      return requiresStaticMethod;
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
