package tech.picnic.errorprone.refaster.plugin;

import static com.google.common.base.Preconditions.checkState;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static tech.picnic.errorprone.refaster.annotation.OnlineDocumentation.NESTED_CLASS_URL_PLACEHOLDER;
import static tech.picnic.errorprone.refaster.annotation.OnlineDocumentation.TOP_LEVEL_CLASS_URL_PLACEHOLDER;

import com.google.common.base.Splitter;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.DescriptionListener;
import com.google.errorprone.ErrorProneOptions;
import com.google.errorprone.matchers.Description;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.util.Context;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.Severity;

// XXX: Can we find a better name for thi class?
// XXX: Use `@AutoValue`?
// XXX: Test this class directly. (Right now it's only indirectly tested through `RefasterTest`.)
final class AnnotatedCompositeCodeTransformer implements CodeTransformer, Serializable {
  private static final long serialVersionUID = 1L;
  private static final Splitter CLASS_NAME_SPLITTER = Splitter.on('.').limit(2);

  private final String packageName;
  private final ImmutableList<CodeTransformer> transformers;
  private final ImmutableClassToInstanceMap<Annotation> annotations;

  AnnotatedCompositeCodeTransformer(
      String packageName,
      ImmutableList<CodeTransformer> transformers,
      ImmutableClassToInstanceMap<Annotation> annotations) {
    this.packageName = packageName;
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
          description ->
              listener.onDescribed(augmentDescription(description, transformer, context)));
    }
  }

  private Description augmentDescription(
      Description description, CodeTransformer delegate, Context context) {
    String shortCheckName = getShortCheckName(description.checkName);
    return Description.builder(
            description.position,
            shortCheckName,
            getLinkPattern(delegate, shortCheckName),
            overrideSeverity(getSeverity(delegate), context),
            getDescription(delegate))
        .addAllFixes(description.fixes)
        .build();
  }

  private String getShortCheckName(String fullCheckName) {
    if (packageName.isEmpty()) {
      return fullCheckName;
    }

    String prefix = packageName + '.';
    checkState(
        fullCheckName.startsWith(prefix),
        "Refaster template class '%s' is not located in package '%s'",
        fullCheckName,
        packageName);

    return fullCheckName.substring(prefix.length());
  }

  private String getLinkPattern(CodeTransformer delegate, String checkName) {
    String urlPattern =
        getAnnotationValue(OnlineDocumentation.class, OnlineDocumentation::value, delegate, "");

    Iterator<String> nameComponents = CLASS_NAME_SPLITTER.splitToStream(checkName).iterator();
    return urlPattern
        .replace(TOP_LEVEL_CLASS_URL_PLACEHOLDER, nameComponents.next())
        .replace(NESTED_CLASS_URL_PLACEHOLDER, Iterators.getNext(nameComponents, ""));
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

  private static SeverityLevel overrideSeverity(SeverityLevel severity, Context context) {
    // XXX: Respect `-XepAllSuggestionsAsWarnings` when using the Picnic Error Prone Fork!
    SeverityLevel minSeverity = SUGGESTION;
    SeverityLevel maxSeverity =
        context.get(ErrorProneOptions.class).isDropErrorsToWarnings() ? WARNING : ERROR;

    return Comparators.max(Comparators.min(severity, minSeverity), maxSeverity);
  }
}
