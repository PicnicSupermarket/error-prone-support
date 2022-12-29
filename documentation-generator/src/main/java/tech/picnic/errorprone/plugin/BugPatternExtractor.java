package tech.picnic.errorprone.plugin;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.util.TaskEvent;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import tech.picnic.errorprone.plugin.models.BugPatternData;

/**
 * A {@link DocumentationExtractor} that describes how to extract data from a {@code BugChecker}.
 */
public final class BugPatternExtractor implements DocumentationExtractor<BugPatternData> {
  /** Instantiates a new {@link BugPatternExtractor} instance. */
  public BugPatternExtractor() {}

  @Override
  public BugPatternData extractData(ClassTree tree, TaskEvent taskEvent) {
    ClassSymbol symbol = ASTHelpers.getSymbol(tree);
    BugPattern annotation = symbol.getAnnotation(BugPattern.class);

    return BugPatternData.create(
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
}
