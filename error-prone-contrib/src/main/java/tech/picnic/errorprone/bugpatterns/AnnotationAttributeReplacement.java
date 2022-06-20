package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.REFACTORING;
import static com.google.errorprone.matchers.Matchers.isType;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.AnnotationMatcherUtils;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
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
  private static final ImmutableMap<
          Matcher<AnnotationTree>,
          ImmutableMap<String, BiFunction<AnnotationTree, VisitorState, SuggestedFix>>>
      ANNOTATION_ATTRIBUTE_REPLACEMENTS =
          ImmutableMap.of(
              isType("org.testng.annotations.Test"),
              ImmutableMap.of(
                  "singleThreaded",
                  (annotation, state) ->
                      SuggestedFix.prefixWith(
                          annotation,
                          "// XXX: Removed argument `singleThreaded = true`, as this cannot be migrated to JUnit!\n"),
                  "priority",
                  (annotation, state) -> {
                    ClassTree classTree = state.findEnclosing(ClassTree.class);
                    if (classTree == null) {
                      return null;
                    }

                    return SuggestedFix.builder()
                        .merge(
                            replaceWithAnnotation(
                                    annotation, "priority", "org.junit.jupiter.api.Order", state)
                                .orElse(null))
                        .merge(
                            SuggestedFix.prefixWith(
                                classTree,
                                "@TestMethodOrder(MethodOrderer.OrderAnnotation.class)\n"))
                        .addImport("org.junit.jupiter.api.TestMethodOrder")
                        .addImport("org.junit.jupiter.api.MethodOrderer")
                        .build();
                  },
                  "description",
                  (annotation, state) ->
                      replaceWithAnnotation(
                              annotation, "description", "org.junit.jupiter.api.DisplayName", state)
                          .orElse(null)));

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    ImmutableList<AnnotationTree> annotations =
        ASTHelpers.getAnnotations(tree).stream().collect(toImmutableList());

    if (ANNOTATION_ATTRIBUTE_REPLACEMENTS.keySet().stream()
        .noneMatch(
            matcher ->
                annotations.stream().anyMatch(annotation -> matcher.matches(annotation, state)))) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder builder = SuggestedFix.builder();
    ANNOTATION_ATTRIBUTE_REPLACEMENTS.forEach(
        (matcher, replacements) -> {
          annotations.forEach(
              annotation -> {
                if (!matcher.matches(annotation, state)) {
                  return;
                }

                Map<String, AssignmentTree> arguments = getAnnotationExpressions(annotation, state);
                if (arguments == null
                    || arguments.keySet().stream().noneMatch(replacements::containsKey)) {
                  return;
                }

                replacements.forEach(
                    (name, fixer) -> {
                      if (!arguments.containsKey(name)) {
                        return;
                      }
                      SuggestedFix fix = fixer.apply(annotation, state);
                      if (fix == null) {
                        return;
                      }

                      arguments.remove(name);
                      builder.merge(fix);
                    });

                StringBuilder replacement = new StringBuilder();
                replacement.append(
                    String.format(
                        "@%s", SourceCode.treeToString(annotation.getAnnotationType(), state)));
                if (!arguments.isEmpty()) {
                  replacement.append(
                      String.format(
                          "(%s)",
                          arguments.values().stream()
                              .map(assignmentTree -> SourceCode.treeToString(assignmentTree, state))
                              .collect(joining(","))));
                }

                builder.merge(SuggestedFix.replace(annotation, replacement.toString()));
              });
        });

    return builder.isEmpty() ? Description.NO_MATCH : describeMatch(tree, builder.build());
  }

  private static Optional<SuggestedFix> replaceWithAnnotation(
      AnnotationTree annotation, String name, String replacement, VisitorState state) {
    ExpressionTree argumentExpression = AnnotationMatcherUtils.getArgument(annotation, name);
    if (argumentExpression == null) {
      return Optional.empty();
    }

    return Optional.of(
        SuggestedFix.builder()
            .postfixWith(
                annotation,
                String.format(
                    "\n@%s(%s)", replacement, SourceCode.treeToString(argumentExpression, state)))
            .build());
  }

  private static Map<String, AssignmentTree> getAnnotationExpressions(
      AnnotationTree tree, VisitorState state) {
    return tree.getArguments().stream()
        .filter(AssignmentTree.class::isInstance)
        .map(AssignmentTree.class::cast)
        .collect(
            toMap(
                assignment -> SourceCode.treeToString(assignment.getVariable(), state),
                identity(),
                (a, b) -> a,
                HashMap::new));
  }
}
