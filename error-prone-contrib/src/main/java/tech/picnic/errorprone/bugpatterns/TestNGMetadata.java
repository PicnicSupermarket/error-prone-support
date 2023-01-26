package tech.picnic.errorprone.bugpatterns;

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
import org.jspecify.annotations.Nullable;

public class TestNGMetadata {
  private final ClassTree classTree;
  private @Nullable TestNGAnnotation classLevelAnnotation;
  private final Map<MethodTree, TestNGAnnotation> methodAnnotationMap = new HashMap<>();
  private final List<TestNGDataProvider> dataProviders = new ArrayList<>();

  public TestNGMetadata(ClassTree classTree) {
    this.classTree = classTree;
  }

  public Optional<TestNGAnnotation> getAnnotation(MethodTree methodTree) {
    return Optional.ofNullable(methodAnnotationMap.get(methodTree));
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

  public ImmutableList<TestNGDataProvider> getDataProviders() {
    return ImmutableList.copyOf(dataProviders);
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
      return arguments.keySet();
    }
  }

  public static class TestNGDataProvider {
    private final MethodTree methodTree;

    public TestNGDataProvider(MethodTree methodTree) {
      this.methodTree = methodTree;
    }

    public MethodTree getMethodTree() {
      return methodTree;
    }
  }
}
