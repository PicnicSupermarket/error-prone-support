package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.REFACTORING;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.Replacement;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.JCTree;
import java.util.Optional;
import java.util.Set;
import tech.picnic.errorprone.bugpatterns.util.AnnotationAttributeMatcher;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} that replaces a predefined list of annotation attributes with its own
 * separate annotation.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Replace annotation attributes with an annotation",
    linkType = NONE,
    tags = REFACTORING,
    severity = ERROR)
public final class AnnotationAttributeReplacement extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;

  private static final ImmutableMap<AnnotationAttributeMatcher, AnnotationAttributeReplacer>
      ANNOTATION_ATTRIBUTE_REPLACEMENT =
          ImmutableMap.<AnnotationAttributeMatcher, AnnotationAttributeReplacer>builder()
              .put(
                  singleArgumentMatcher("org.testng.annotations.Test#singleThreaded"),
                  (annotation, argument, state) ->
                      Optional.of(
                          SuggestedFix.builder()
                              .merge(removeAnnotationArgument(annotation, argument, state))
                              .merge(
                                  SuggestedFix.prefixWith(
                                      annotation,
                                      "// XXX: Removed argument `singleThreaded = true`, as this cannot be migrated to JUnit!\n"))
                              .build()))
              .put(
                  singleArgumentMatcher("org.testng.annotations.Test#priority"),
                  (annotation, argument, state) -> {
                    ClassTree classTree = state.findEnclosing(ClassTree.class);
                    if (classTree == null) {
                      return Optional.empty();
                    }

                    if (argument.getKind() != Tree.Kind.ASSIGNMENT) {
                      return Optional.empty();
                    }
                    AssignmentTree assignmentTree = (AssignmentTree) argument;

                    return Optional.of(
                        SuggestedFix.builder()
                            .merge(removeAnnotationArgument(annotation, argument, state))
                            .merge(
                                SuggestedFix.postfixWith(
                                    annotation,
                                    String.format(
                                        "\n@org.junit.jupiter.api.Order(%s)",
                                        SourceCode.treeToString(
                                            assignmentTree.getExpression(), state))))
                            .merge(
                                SuggestedFix.prefixWith(
                                    classTree,
                                    "@TestMethodOrder(MethodOrderer.OrderAnnotation.class)\n"))
                            .addImport("org.junit.jupiter.api.TestMethodOrder")
                            .addImport("org.junit.jupiter.api.MethodOrderer")
                            .build());
                  })
              .put(
                  singleArgumentMatcher("org.testng.annotations.Test#description"),
                  (annotation, argument, state) ->
                      Optional.of(argument)
                          .filter(AssignmentTree.class::isInstance)
                          .map(AssignmentTree.class::cast)
                          .map(
                              assignmentTree ->
                                  SuggestedFix.builder()
                                      .merge(removeAnnotationArgument(annotation, argument, state))
                                      .merge(
                                          SuggestedFix.postfixWith(
                                              annotation,
                                              String.format(
                                                  "\n@org.junit.jupiter.api.DisplayName(%s)",
                                                  SourceCode.treeToString(
                                                      assignmentTree.getExpression(), state))))
                                      .build()))
              .put(
                  singleArgumentMatcher("org.testng.annotations.Test#groups"),
                  (annotation, argument, state) ->
                      Optional.of(argument)
                          .filter(AssignmentTree.class::isInstance)
                          .map(AssignmentTree.class::cast)
                          .map(
                              assignmentTree ->
                                  SuggestedFix.builder()
                                      .merge(removeAnnotationArgument(annotation, argument, state))
                                      .merge(
                                          SuggestedFix.postfixWith(
                                              annotation,
                                              String.format(
                                                  "\n@org.junit.jupiter.api.Tag(%s)",
                                                  SourceCode.treeToString(
                                                      assignmentTree.getExpression(), state))))
                                      .build()))
              .build();

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    ImmutableList<AnnotationTree> annotations =
        ASTHelpers.getAnnotations(tree).stream().collect(toImmutableList());

    SuggestedFix.Builder builder = SuggestedFix.builder();
    annotations.forEach(
        annotation -> {
          ANNOTATION_ATTRIBUTE_REPLACEMENT.forEach(
              (matcher, fixBuilder) ->
                  matcher
                      .extractMatchingArguments(annotation)
                      .forEach(
                          argument ->
                              fixBuilder
                                  .buildFix(annotation, argument, state)
                                  .ifPresent(builder::merge)));

          tryRemoveTrailingParenthesis(annotation, builder.build(), state)
              .ifPresent(builder::merge);
        });

    return builder.isEmpty() ? Description.NO_MATCH : describeMatch(tree, builder.build());
  }

  private static Optional<SuggestedFix> tryRemoveTrailingParenthesis(
      AnnotationTree annotation, SuggestedFix fix, VisitorState state) {
    JCTree.JCCompilationUnit compileUnit =
        ((JCTree.JCCompilationUnit) state.findEnclosing(CompilationUnitTree.class));
    if (compileUnit == null) {
      return Optional.empty();
    }

    Set<Replacement> replacements = fix.getReplacements(compileUnit.endPositions);
    String annotationSource = SourceCode.treeToString(annotation, state).replace(", ", ",");
    String annotationArguments =
        annotationSource.substring(
            annotationSource.indexOf("(") + 1, annotationSource.length() - 1);
    int argumentReplacementLength = replacements.stream().mapToInt(Replacement::length).sum();
    if (argumentReplacementLength != annotationArguments.length()) {
      return Optional.empty();
    }

    return replacements.stream()
        .filter(replacement -> replacement.length() != 0)
        .map(Replacement::startPosition)
        .reduce(Integer::min)
        .flatMap(
            min ->
                replacements.stream()
                    .filter(replacement -> replacement.length() != 0)
                    .map(Replacement::endPosition)
                    .reduce(Integer::max)
                    .map(
                        max ->
                            SuggestedFix.builder()
                                .merge(SuggestedFix.replace(min - 1, min, ""))
                                .merge(SuggestedFix.replace(max, max + 1, ""))
                                .build()));
  }

  private static SuggestedFix removeAnnotationArgument(
      AnnotationTree annotation, ExpressionTree argument, VisitorState state) {
    String annotationSource = SourceCode.treeToString(annotation, state);
    String argumentSource = SourceCode.treeToString(argument, state);
    int argumentSourceIndex = annotationSource.indexOf(argumentSource);
    boolean endsWithComma =
        annotationSource
            .substring(
                argumentSourceIndex + argumentSource.length(),
                argumentSourceIndex + argumentSource.length() + 1)
            .equals(",");
    return SuggestedFix.builder().replace(argument, "", 0, endsWithComma ? 1 : 0).build();
  }

  private static AnnotationAttributeMatcher singleArgumentMatcher(String fullyQualifiedArgument) {
    return AnnotationAttributeMatcher.create(
        Optional.of(ImmutableList.of(fullyQualifiedArgument)), ImmutableList.of());
  }

  @FunctionalInterface
  interface AnnotationAttributeReplacer {
    Optional<SuggestedFix> buildFix(
        AnnotationTree annotation, ExpressionTree argument, VisitorState state);
  }
}
