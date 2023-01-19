package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.VisitorState;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

public class TestNGMetadata {
  private final ClassTree classTree;
  private @Nullable TestNGAnnotation classLevelAnnotation;
  private Map<MethodTree, TestNGAnnotation> methodAnnotationMap = new HashMap<>();
  private final List<TestNGDataProvider> dataProviders = new ArrayList<>();

  public TestNGMetadata(ClassTree classTree) {
    this.classTree = classTree;
  }

  public Optional<TestNGAnnotation> getAnnotation(MethodTree methodTree) {
    return Optional.ofNullable(methodAnnotationMap.get(methodTree));
  }

  public ImmutableSet<MethodTree> getTestsUsingValueFactory(
      TestNGDataProvider dataProvider, VisitorState state) {
    return methodAnnotationMap.entrySet().stream()
        .filter(entry -> entry.getValue().getArgumentNames().contains("dataProvider"))
        .filter(
            entry -> {
              ExpressionTree dataProviderNameTree =
                  entry.getValue().getArguments().get("dataProvider");
              String dataProviderName = SourceCode.treeToString(dataProviderNameTree, state);
              return dataProvider.valueFactoryName.equals(dataProviderName);
            })
        .map(Map.Entry::getKey)
        .collect(toImmutableSet());
  }

  public void addTestAnnotation(MethodTree methodTree, TestNGAnnotation annotation) {
    methodAnnotationMap.put(methodTree, annotation);
  }

  public void setClassLevelAnnotation(TestNGAnnotation classLevelAnnotation) {
    this.classLevelAnnotation = classLevelAnnotation;
  }

  public Optional<TestNGAnnotation> getClassLevelAnnotation() {
    return Optional.ofNullable(classLevelAnnotation);
  }

  public ClassTree getClassTree() {
    return classTree;
  }

  public void addDataProvider(MethodTree methodTree) {
    dataProviders.add(new TestNGDataProvider(methodTree));
  }

  public static class TestNGAnnotation {
    private final AnnotationTree annotationTree;
    private final ImmutableMap<String, ExpressionTree> arguments;

    public TestNGAnnotation(
        AnnotationTree annotationTree, ImmutableMap<String, ExpressionTree> arguments) {
      this.annotationTree = annotationTree;
      this.arguments = arguments;
    }

    public AnnotationTree getAnnotationTree() {
      return annotationTree;
    }

    public ImmutableMap<String, ExpressionTree> getArguments() {
      return arguments;
    }

    public ImmutableSet<String> getArgumentNames() {
      return arguments.keySet().stream().collect(toImmutableSet());
    }
  }

  public static class TestNGDataProvider {
    private final MethodTree methodTree;
    private final String valueFactoryName;

    public TestNGDataProvider(MethodTree methodTree) {
      this.methodTree = methodTree;
      this.valueFactoryName = methodTree.getName().toString();
    }
  }
}
