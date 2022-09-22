package tech.picnic.errorprone.refaster;

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
import tech.picnic.errorprone.refaster.annotation.TemplateCollection;

// XXX: Move? Rename? Use `@AutoValue`?
// XXX: This is a bit of an implementation detail. Alternative idea: move this to `refaster-runner`,
// and have `refaster-compiler` depend on `refaster-runner`. (Or the other way around?)
// XXX: If we go this route, do we need `CodeTransformer#annotations()` at all? We can track the
// meta-data we're interested in in a custom format (like we already do for the name).
// XXX: Or should the name _also_ be derived from an annotation? Upshot is limited.
// XXX: ^ Name could be dropped if we derive it from `description.checkName`, with the assumption
// that package and class names follow idiomatic Java naming convention. Kinda icky...
public final class AnnotatedCompositeCodeTransformer implements CodeTransformer, Serializable {
  private static final long serialVersionUID = 1L;

  private final String name;
  private final ImmutableList<CodeTransformer> transformers;
  private final ImmutableClassToInstanceMap<Annotation> annotations;

  public AnnotatedCompositeCodeTransformer(
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
    String linkPattern =
        extract(AnnotatedCompositeCodeTransformer::getLinkPattern, delegate, this).orElse("");
    SeverityLevel severityLevel =
        extract(AnnotatedCompositeCodeTransformer::getSeverity, delegate, this)
            .orElse(SeverityLevel.SUGGESTION);
    String message =
        extract(AnnotatedCompositeCodeTransformer::getDescription, delegate, this)
            .orElse("Refactoring opportunity");

    // XXX: Replace only the first `$`.
    // XXX: Test this.
    return Description.builder(
            description.position,
            description.checkName,
            String.format(linkPattern, name.replace('$', '#')),
            severityLevel,
            message)
        .addAllFixes(description.fixes)
        .build();
  }

  // XXX: Deduplicate the code below.

  private static <S, T> Optional<T> extract(Function<S, Optional<T>> extractor, S first, S second) {
    return extractor.apply(first).or(() -> extractor.apply(second));
  }

  private static Optional<String> getLinkPattern(CodeTransformer codeTransformer) {
    return Optional.ofNullable(codeTransformer.annotations().getInstance(TemplateCollection.class))
        .map(TemplateCollection::linkPattern);
  }

  private static Optional<SeverityLevel> getSeverity(CodeTransformer codeTransformer) {
    return Optional.ofNullable(codeTransformer.annotations().getInstance(TemplateCollection.class))
        .map(TemplateCollection::severity);
  }

  private static Optional<String> getDescription(CodeTransformer codeTransformer) {
    return Optional.ofNullable(codeTransformer.annotations().getInstance(TemplateCollection.class))
        .map(TemplateCollection::description);
  }
}
