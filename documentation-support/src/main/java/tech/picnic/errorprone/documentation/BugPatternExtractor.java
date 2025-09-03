package tech.picnic.errorprone.documentation;

import static com.google.common.base.Verify.verify;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Objects.requireNonNull;

import com.google.auto.common.AnnotationMirrors;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import java.util.Optional;
import javax.lang.model.element.AnnotationValue;
import tech.picnic.errorprone.documentation.ProjectInfo.BugPatternInfo;

/**
 * An {@link Extractor} that describes how to extract data from a {@code @BugPattern} annotation.
 */
@AutoService(Extractor.class)
@SuppressWarnings("rawtypes" /* See https://github.com/google/auto/issues/870. */)
public record BugPatternExtractor() implements Extractor<BugPatternInfo> {
  @Override
  public String identifier() {
    return "bugpattern";
  }

  @Override
  public Optional<BugPatternInfo> tryExtract(ClassTree tree, VisitorState state) {
    ClassSymbol symbol = ASTHelpers.getSymbol(tree);
    BugPattern annotation = symbol.getAnnotation(BugPattern.class);
    if (annotation == null) {
      return Optional.empty();
    }

    return Optional.of(
        new BugPatternInfo(
            state.getPath().getCompilationUnit().getSourceFile().toUri(),
            symbol.getQualifiedName().toString(),
            annotation.name().isEmpty() ? tree.getSimpleName().toString() : annotation.name(),
            ImmutableList.copyOf(annotation.altNames()),
            annotation.link(),
            ImmutableList.copyOf(annotation.tags()),
            annotation.summary(),
            annotation.explanation(),
            annotation.severity(),
            annotation.disableable(),
            annotation.documentSuppression()
                ? getSuppressionAnnotations(tree)
                : ImmutableList.of()));
  }

  /**
   * Returns the fully-qualified class names of suppression annotations specified by the {@link
   * BugPattern} annotation located on the given tree.
   *
   * @implNote This method cannot simply invoke {@link BugPattern#suppressionAnnotations()}, as that
   *     will yield an "Attempt to access Class objects for TypeMirrors" exception.
   */
  private static ImmutableList<String> getSuppressionAnnotations(ClassTree tree) {
    AnnotationTree annotationTree =
        ASTHelpers.getAnnotationWithSimpleName(
            ASTHelpers.getAnnotations(tree), BugPattern.class.getSimpleName());
    requireNonNull(annotationTree, "BugPattern annotation must be present");

    Attribute.Array types =
        doCast(
            AnnotationMirrors.getAnnotationValue(
                ASTHelpers.getAnnotationMirror(annotationTree), "suppressionAnnotations"),
            Attribute.Array.class);

    return types.getValue().stream()
        .map(v -> doCast(v, Attribute.Class.class).classType.toString())
        .collect(toImmutableList());
  }

  @SuppressWarnings("unchecked" /* Type safety is validated, but a caller responsibility. */)
  private static <T extends AnnotationValue> T doCast(AnnotationValue value, Class<T> target) {
    verify(target.isInstance(value), "Value '%s' is not of type '%s'", value, target);
    return (T) value;
  }
}
