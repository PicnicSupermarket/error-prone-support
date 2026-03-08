package tech.picnic.errorprone.documentation;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.isType;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.AnnotationMatcherUtils;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.matchers.MultiMatcher.MultiMatchResult;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import java.util.Optional;
import tech.picnic.errorprone.documentation.ProjectInfo.RefasterRuleCollection;
import tech.picnic.errorprone.documentation.ProjectInfo.RefasterRuleCollection.Rule;

/**
 * An {@link Extractor} that describes how to extract data from Refaster rule collection classes
 * annotated with {@code @OnlineDocumentation}.
 */
// XXX: This class doesn't support `@OnlineDocumentation` with a custom documentation URL or
// Refaster rules that are directly annotated with `@OnlineDocumentation`. Generalize this logic
// if/when either of those use cases arises.
// XXX: Also extract information from the `@TypeMigration` annotation.
@AutoService(Extractor.class)
@SuppressWarnings("rawtypes" /* See https://github.com/google/auto/issues/870. */)
public record RefasterRuleCollectionExtractor() implements Extractor<RefasterRuleCollection> {
  private static final MultiMatcher<Tree, AnnotationTree> DESCRIPTION =
      annotations(AT_LEAST_ONE, isType("tech.picnic.errorprone.refaster.annotation.Description"));
  private static final MultiMatcher<Tree, AnnotationTree> ONLINE_DOCUMENTATION =
      annotations(
          AT_LEAST_ONE, isType("tech.picnic.errorprone.refaster.annotation.OnlineDocumentation"));
  private static final MultiMatcher<Tree, AnnotationTree> SEVERITY =
      annotations(AT_LEAST_ONE, isType("tech.picnic.errorprone.refaster.annotation.Severity"));
  private static final String BEFORE_TEMPLATE =
      "com.google.errorprone.refaster.annotation.BeforeTemplate";

  @Override
  public String identifier() {
    return "refaster-rule-collection";
  }

  @Override
  public Optional<RefasterRuleCollection> tryExtract(ClassTree tree, VisitorState state) {
    MultiMatchResult<AnnotationTree> hasOnlineDocumentation =
        ONLINE_DOCUMENTATION.multiMatchResult(tree, state);
    if (!hasOnlineDocumentation.matches()) {
      return Optional.empty();
    }

    if (!hasOnlineDocumentation.onlyMatchingNode().getArguments().isEmpty()) {
      /* This is a rule that is meant to be hosted at a custom location: skip it. */
      return Optional.empty();
    }

    return Optional.of(
        new RefasterRuleCollection(
            state.getPath().getCompilationUnit().getSourceFile().toUri(),
            tree.getSimpleName().toString(),
            getDescription(tree, state),
            getRules(tree, state)));
  }

  private static ImmutableList<Rule> getRules(ClassTree tree, VisitorState state) {
    return tree.getMembers().stream()
        .filter(ClassTree.class::isInstance)
        .map(ClassTree.class::cast)
        .filter(innerClass -> isRefasterRule(innerClass, state))
        .map(
            rule ->
                new Rule(
                    rule.getSimpleName().toString(),
                    getDescription(rule, state),
                    getSeverity(rule, state)))
        .collect(toImmutableList());
  }

  private static boolean isRefasterRule(ClassTree classTree, VisitorState state) {
    return classTree.getMembers().stream()
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast)
        .anyMatch(method -> ASTHelpers.hasAnnotation(method, BEFORE_TEMPLATE, state));
  }

  private static String getDescription(ClassTree tree, VisitorState state) {
    MultiMatchResult<AnnotationTree> descriptionMatch = DESCRIPTION.multiMatchResult(tree, state);
    if (descriptionMatch.matches()) {
      String value =
          ASTHelpers.constValue(
              AnnotationMatcherUtils.getArgument(descriptionMatch.onlyMatchingNode(), "value"),
              String.class);
      if (value != null) {
        return value;
      }
    }

    String docComment = state.getElements().getDocComment(ASTHelpers.getSymbol(tree));
    return docComment != null ? docComment.strip() : "";
  }

  private static SeverityLevel getSeverity(ClassTree tree, VisitorState state) {
    MultiMatchResult<AnnotationTree> severityMatch = SEVERITY.multiMatchResult(tree, state);
    if (severityMatch.matches()) {
      ExpressionTree value =
          AnnotationMatcherUtils.getArgument(severityMatch.onlyMatchingNode(), "value");
      Symbol symbol = ASTHelpers.getSymbol(value);
      if (symbol != null) {
        return SeverityLevel.valueOf(symbol.getSimpleName().toString());
      }
    }

    return SUGGESTION;
  }
}
