package tech.picnic.errorprone.bugpatterns.testngtojunit;

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

public final class TestNGMetadata {
  private final ClassTree classTree;
  private TestNGMetadata.Annotation classLevelAnnotation;
  private final Map<MethodTree, Annotation> methodAnnotations = new HashMap<>();
  private final List<DataProvider> dataProviders = new ArrayList<>();

  public TestNGMetadata(ClassTree classTree) {
    this.classTree = classTree;
  }

  public ImmutableSet<Annotation> getAnnotations() {
    return ImmutableSet.copyOf(methodAnnotations.values());
  }

  public Optional<Annotation> getAnnotation(MethodTree methodTree) {
    return Optional.ofNullable(methodAnnotations.get(methodTree));
  }

  public void addTestAnnotation(MethodTree methodTree, Annotation annotation) {
    methodAnnotations.put(methodTree, annotation);
  }

  public void setClassLevelAnnotation(Annotation classLevelAnnotation) {
    this.classLevelAnnotation = classLevelAnnotation;
  }

  public Optional<Annotation> getClassLevelAnnotation() {
    return Optional.ofNullable(classLevelAnnotation);
  }

  public ClassTree getClassTree() {
    return classTree;
  }

  public void addDataProvider(MethodTree methodTree) {
    dataProviders.add(new DataProvider(methodTree));
  }

  public ImmutableList<DataProvider> getDataProviders() {
    return ImmutableList.copyOf(dataProviders);
  }

  // XXX: Hmm not sure if `Annotation` is the ideal name for this. It might be rather confusing IMO.
  // Now it's not clear this is our own object and what it actual does? Now it's used with
  // `TestNGMetaData.Annotation` however it should also be clear without having to prefix it with
  // the qualifier. Maybe adding the `MetaData` suffix to this would clarify a lot (also for
  // `DataProvider` class).
  // XXX: It would be nice to also use `@AutoValue` for this. This is kind of the "default" for
  // creating POJO's in EPS. For other examples, see the PR that is open that is about introducing
  // `documentation-support`.
  public static class Annotation {
    private final AnnotationTree annotationTree;
    private final ImmutableMap<String, ExpressionTree> arguments;

    public Annotation(
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

  public static class DataProvider {
    private final MethodTree methodTree;
    private final String name;

    public DataProvider(MethodTree methodTree) {
      this.methodTree = methodTree;
      this.name = methodTree.getName().toString();
    }

    public MethodTree getMethodTree() {
      return methodTree;
    }

    public String getName() {
      return name;
    }
  }
}
