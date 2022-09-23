package tech.picnic.errorprone.refaster.plugin;

import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;

import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.DescriptionListener;
import com.google.errorprone.matchers.Description;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.util.Context;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.function.Function;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.Severity;

// XXX: Rename? Use `@AutoValue`?
// XXX: Should the name _also_ be derived from an annotation? Upshot is limited.
// XXX: ^ Name could be dropped if we derive it from `description.checkName`, with the assumption
// that package and class names follow idiomatic Java naming convention. Kinda icky...
final class AnnotatedCompositeCodeTransformer implements CodeTransformer, Serializable {
  private static final long serialVersionUID = 1L;

  private final String name;
  private final ImmutableList<CodeTransformer> transformers;
  private final ImmutableClassToInstanceMap<Annotation> annotations;

  AnnotatedCompositeCodeTransformer(
      String name,
      ImmutableList<CodeTransformer> transformers,
      ImmutableClassToInstanceMap<Annotation> annotations) {
    this.name = name;
    this.transformers = transformers;
    this.annotations = annotations;
  }

  @Override
  public ImmutableClassToInstanceMap<Annotation> annotations() {
    return annotations;
  }

  @Override
  public void apply(TreePath path, Context context, DescriptionListener listener) {
    for (CodeTransformer transformer : transformers) {
      transformer.apply(
          path,
          context,
          description -> listener.onDescribed(augmentDescription(description, transformer)));
    }
  }

  private Description augmentDescription(Description description, CodeTransformer delegate) {
    // XXX: Replace only the first `$`.
    // XXX: Test this.
    return Description.builder(
            description.position,
            description.checkName,
            String.format(getLinkPattern(delegate), name.replace('$', '#')),
            getSeverity(delegate),
            getDescription(delegate))
        .addAllFixes(description.fixes)
        .build();
  }

  private String getLinkPattern(CodeTransformer delegate) {
    return getAnnotationValue(OnlineDocumentation.class, OnlineDocumentation::value, delegate, "");
  }

  private SeverityLevel getSeverity(CodeTransformer delegate) {
    return getAnnotationValue(Severity.class, Severity::value, delegate, SUGGESTION);
  }

  private String getDescription(CodeTransformer delegate) {
    return getAnnotationValue(
        tech.picnic.errorprone.refaster.annotation.Description.class,
        tech.picnic.errorprone.refaster.annotation.Description::value,
        delegate,
        "Refactoring opportunity");
  }

  private <A extends Annotation, T> T getAnnotationValue(
      Class<A> annotation, Function<A, T> extractor, CodeTransformer delegate, T defaultValue) {
    return getAnnotationValue(delegate, annotation)
        .or(() -> getAnnotationValue(this, annotation))
        .map(extractor)
        .orElse(defaultValue);
  }

  private static <A extends Annotation> Optional<A> getAnnotationValue(
      CodeTransformer codeTransformer, Class<A> annotation) {
    return Optional.ofNullable(codeTransformer.annotations().getInstance(annotation));
  }
}
