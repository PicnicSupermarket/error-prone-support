package tech.picnic.errorprone.refaster.plugin;

import static com.google.common.base.Preconditions.checkState;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static tech.picnic.errorprone.refaster.annotation.OnlineDocumentation.NESTED_CLASS_URL_PLACEHOLDER;
import static tech.picnic.errorprone.refaster.annotation.OnlineDocumentation.TOP_LEVEL_CLASS_URL_PLACEHOLDER;

import com.google.auto.value.AutoValue;
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

// XXX: Can we find a better name for this class? `CompositeAnnotatedCodeTransformer`,
// `AugmentedCompositeCodeTransformer`, ...?
// XXX: Test this class directly. (Right now it's only indirectly tested through `RefasterTest`.)
@AutoValue
abstract class AnnotatedCompositeCodeTransformer implements CodeTransformer, Serializable {
  private static final long serialVersionUID = 1L;
  private static final Splitter CLASS_NAME_SPLITTER = Splitter.on('.').limit(2);

  abstract String packageName();

  abstract ImmutableList<CodeTransformer> transformers();

  @Override
  public abstract ImmutableClassToInstanceMap<Annotation> annotations();

  static AnnotatedCompositeCodeTransformer create(
      String packageName,
      ImmutableList<CodeTransformer> transformers,
      ImmutableClassToInstanceMap<Annotation> annotations) {
    return new AutoValue_AnnotatedCompositeCodeTransformer(packageName, transformers, annotations);
  }

  @Override
  public void apply(TreePath path, Context context, DescriptionListener listener) {
    for (CodeTransformer transformer : transformers()) {
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
            getLinkPattern(delegate, shortCheckName).orElse(null),
            overrideSeverity(getSeverity(delegate), context),
            getDescription(delegate))
        .addAllFixes(description.fixes)
        .build();
  }

  private String getShortCheckName(String fullCheckName) {
    if (packageName().isEmpty()) {
      return fullCheckName;
    }

    String prefix = packageName() + '.';
    checkState(
        fullCheckName.startsWith(prefix),
        "Refaster template class '%s' is not located in package '%s'",
        fullCheckName,
        packageName());

    return fullCheckName.substring(prefix.length());
  }

  private Optional<String> getLinkPattern(CodeTransformer delegate, String checkName) {
    Iterator<String> nameComponents = CLASS_NAME_SPLITTER.splitToStream(checkName).iterator();
    return getAnnotationValue(OnlineDocumentation.class, OnlineDocumentation::value, delegate)
        .map(
            urlPattern ->
                urlPattern.replace(TOP_LEVEL_CLASS_URL_PLACEHOLDER, nameComponents.next()))
        .map(
            urlPattern ->
                urlPattern.replace(
                    NESTED_CLASS_URL_PLACEHOLDER, Iterators.getNext(nameComponents, "")));
  }

  private SeverityLevel getSeverity(CodeTransformer delegate) {
    /*
     * The default severity should be kept in sync with the default severity of the
     * `tech.picnic.errorprone.refaster.runner.Refaster` bug checker. (The associated
     * `RefasterTest#severityAssignment` test verifies this invariant.)
     */
    return getAnnotationValue(Severity.class, Severity::value, delegate).orElse(SUGGESTION);
  }

  private String getDescription(CodeTransformer delegate) {
    return getAnnotationValue(
            tech.picnic.errorprone.refaster.annotation.Description.class,
            tech.picnic.errorprone.refaster.annotation.Description::value,
            delegate)
        .orElse("Refactoring opportunity");
  }

  private <A extends Annotation, T> Optional<T> getAnnotationValue(
      Class<A> annotation, Function<A, T> extractor, CodeTransformer delegate) {
    return getAnnotationValue(delegate, annotation)
        .or(() -> getAnnotationValue(this, annotation))
        .map(extractor);
  }

  private static <A extends Annotation> Optional<A> getAnnotationValue(
      CodeTransformer codeTransformer, Class<A> annotation) {
    return Optional.ofNullable(codeTransformer.annotations().getInstance(annotation));
  }

  private static SeverityLevel overrideSeverity(SeverityLevel severity, Context context) {
    ErrorProneOptions options = context.get(ErrorProneOptions.class);
    SeverityLevel minSeverity =
        ErrorProneFork.isSuggestionsAsWarningsEnabled(options) ? WARNING : SUGGESTION;
    SeverityLevel maxSeverity = options.isDropErrorsToWarnings() ? WARNING : ERROR;

    return Comparators.max(Comparators.min(severity, minSeverity), maxSeverity);
  }
}
