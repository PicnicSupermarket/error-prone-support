package tech.picnic.errorprone.plugin;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.util.TaskEvent;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import tech.picnic.errorprone.plugin.models.BugPattern;

/**
 * A {@link DocumentationExtractor} that describes how to extract data from a {@code BugChecker}.
 */
final class BugPatternExtractor implements DocumentationExtractor<BugPattern> {
  @Override
  public BugPattern extract(ClassTree tree, TaskEvent taskEvent) {
    ClassSymbol symbol = ASTHelpers.getSymbol(tree);
    com.google.errorprone.BugPattern annotation =
        symbol.getAnnotation(com.google.errorprone.BugPattern.class);
    requireNonNull(annotation, "BugPattern annotation must be present");

    return BugPattern.create(
        symbol.getQualifiedName().toString(),
        annotation.name().isEmpty() ? tree.getSimpleName().toString() : annotation.name(),
        ImmutableList.copyOf(annotation.altNames()),
        annotation.link(),
        ImmutableList.copyOf(annotation.tags()),
        annotation.summary(),
        annotation.explanation(),
        annotation.severity(),
        annotation.disableable());
  }

  @Override
  public boolean canExtract(ClassTree tree) {
    return ASTHelpers.hasDirectAnnotationWithSimpleName(
        tree, com.google.errorprone.BugPattern.class.getSimpleName());
  }
}
