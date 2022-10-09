package tech.picnic.errorprone.refaster;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.auto.value.AutoAnnotation;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.CodeTransformer;
import com.google.errorprone.DescriptionListener;
import com.google.errorprone.ErrorProneOptions;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.matchers.Description;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.Severity;

// XXX: Test the `ErrorProneOptions`-based severity override logic. (Right now that logic is tested
// through `RefasterTest`, but ideally it is covered by tests in this class, closer to the code that
// implements the relevant logic.)
final class AnnotatedCompositeCodeTransformerTest {
  private static final DiagnosticPosition DUMMY_POSITION = mock(DiagnosticPosition.class);
  private static final Fix DUMMY_FIX = mock(Fix.class);
  private static final TreePath DUMMY_PATH = mock(TreePath.class);
  private static final String DEFAULT_PACKAGE = "";
  private static final String CUSTOM_PACKAGE = "com.example";
  private static final String SIMPLE_CLASS_NAME = "MyRefasterRule";

  private static Stream<Arguments> applyTestCases() {
    /* { context, packageName, ruleName compositeAnnotations, delegateAnnotations, expectedDescription } */
    return Stream.of(
        arguments(
            context(),
            DEFAULT_PACKAGE,
            SIMPLE_CLASS_NAME,
            ImmutableSet.of(),
            ImmutableSet.of(),
            description(
                SIMPLE_CLASS_NAME, Optional.empty(), SUGGESTION, "Refactoring opportunity")),
        arguments(
            context(),
            CUSTOM_PACKAGE,
            CUSTOM_PACKAGE + '.' + SIMPLE_CLASS_NAME,
            ImmutableSet.of(
                descriptionAnnotation("Composite description"),
                documentationAnnotation("https://example.com"),
                severityAnnotation(ERROR)),
            ImmutableSet.of(),
            description(
                SIMPLE_CLASS_NAME,
                Optional.of("https://example.com"),
                ERROR,
                "Composite description")),
        arguments(
            context(),
            DEFAULT_PACKAGE,
            SIMPLE_CLASS_NAME,
            ImmutableSet.of(),
            ImmutableSet.of(
                descriptionAnnotation("Rule description"),
                documentationAnnotation("https://example.com/rule/${topLevelClassName}"),
                severityAnnotation(WARNING)),
            description(
                SIMPLE_CLASS_NAME,
                Optional.of("https://example.com/rule/" + SIMPLE_CLASS_NAME),
                WARNING,
                "Rule description")),
        arguments(
            context(),
            CUSTOM_PACKAGE,
            CUSTOM_PACKAGE + '.' + SIMPLE_CLASS_NAME + ".SomeInnerClass.NestedEvenDeeper",
            ImmutableSet.of(
                descriptionAnnotation("Some description"),
                documentationAnnotation("https://example.com"),
                severityAnnotation(ERROR)),
            ImmutableSet.of(
                descriptionAnnotation("Overriding description"),
                documentationAnnotation(
                    "https://example.com/rule/${topLevelClassName}/${nestedClassName}"),
                severityAnnotation(SUGGESTION)),
            description(
                SIMPLE_CLASS_NAME + ".SomeInnerClass.NestedEvenDeeper",
                Optional.of(
                    "https://example.com/rule/"
                        + SIMPLE_CLASS_NAME
                        + "/SomeInnerClass.NestedEvenDeeper"),
                SUGGESTION,
                "Overriding description")));
  }

  @MethodSource("applyTestCases")
  @ParameterizedTest
  void apply(
      Context context,
      String packageName,
      String ruleName,
      ImmutableSet<? extends Annotation> compositeAnnotations,
      ImmutableSet<? extends Annotation> delegateAnnotations,
      Description expectedDescription) {
    CodeTransformer codeTransformer =
        AnnotatedCompositeCodeTransformer.create(
            packageName,
            ImmutableList.of(
                delegateCodeTransformer(
                    delegateAnnotations, context, refasterDescription(ruleName))),
            indexAnnotations(compositeAnnotations));

    List<Description> collected = new ArrayList<>();
    codeTransformer.apply(DUMMY_PATH, context, collected::add);
    assertThat(collected)
        .satisfiesExactly(
            actual -> {
              assertThat(actual.position).isEqualTo(expectedDescription.position);
              assertThat(actual.checkName).isEqualTo(expectedDescription.checkName);
              assertThat(actual.fixes).containsExactlyElementsOf(expectedDescription.fixes);
              assertThat(actual.getLink()).isEqualTo(expectedDescription.getLink());
              assertThat(actual.getRawMessage()).isEqualTo(expectedDescription.getRawMessage());
            });
  }

  private static ImmutableClassToInstanceMap<Annotation> indexAnnotations(
      ImmutableSet<? extends Annotation> annotations) {
    return ImmutableClassToInstanceMap.copyOf(
        Maps.uniqueIndex(annotations, Annotation::annotationType));
  }

  private static CodeTransformer delegateCodeTransformer(
      ImmutableSet<? extends Annotation> annotations,
      Context expectedContext,
      Description returnedDescription) {
    CodeTransformer codeTransformer = mock(CodeTransformer.class);

    when(codeTransformer.annotations()).thenReturn(indexAnnotations(annotations));
    doAnswer(
            inv -> {
              inv.<DescriptionListener>getArgument(2).onDescribed(returnedDescription);
              return null;
            })
        .when(codeTransformer)
        .apply(eq(DUMMY_PATH), eq(expectedContext), notNull());

    return codeTransformer;
  }

  /**
   * Returns a {@link Description} with some default values as produced by {@link
   * com.google.errorprone.refaster.RefasterScanner}.
   */
  private static Description refasterDescription(String name) {
    return description(name, Optional.of(""), WARNING, "");
  }

  private static Description description(
      String name, Optional<String> link, SeverityLevel severityLevel, String message) {
    return Description.builder(DUMMY_POSITION, name, link.orElse(null), severityLevel, message)
        .addFix(DUMMY_FIX)
        .build();
  }

  private static Context context() {
    Context context = mock(Context.class);
    when(context.get(ErrorProneOptions.class))
        .thenReturn(ErrorProneOptions.processArgs(ImmutableList.of()));
    return context;
  }

  @AutoAnnotation
  private static tech.picnic.errorprone.refaster.annotation.Description descriptionAnnotation(
      String value) {
    return new AutoAnnotation_AnnotatedCompositeCodeTransformerTest_descriptionAnnotation(value);
  }

  @AutoAnnotation
  private static OnlineDocumentation documentationAnnotation(String value) {
    return new AutoAnnotation_AnnotatedCompositeCodeTransformerTest_documentationAnnotation(value);
  }

  @AutoAnnotation
  private static Severity severityAnnotation(SeverityLevel value) {
    return new AutoAnnotation_AnnotatedCompositeCodeTransformerTest_severityAnnotation(value);
  }
}
