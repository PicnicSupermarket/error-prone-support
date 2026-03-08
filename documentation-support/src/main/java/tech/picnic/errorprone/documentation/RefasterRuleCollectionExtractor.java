package tech.picnic.errorprone.documentation;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.isType;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.matchers.MultiMatcher.MultiMatchResult;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
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
  private static final MultiMatcher<Tree, AnnotationTree> ONLINE_DOCUMENTATION =
      annotations(
          AT_LEAST_ONE, isType("tech.picnic.errorprone.refaster.annotation.OnlineDocumentation"));
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

  // XXX: Infer the severity of each rule from the `@Severity` annotation if present.
  private static ImmutableList<Rule> getRules(ClassTree tree, VisitorState state) {
    return tree.getMembers().stream()
        .filter(ClassTree.class::isInstance)
        .map(ClassTree.class::cast)
        .filter(innerClass -> isRefasterRule(innerClass, state))
        .map(
            rule ->
                new Rule(rule.getSimpleName().toString(), getDescription(rule, state), SUGGESTION))
        .collect(toImmutableList());
  }

  private static boolean isRefasterRule(ClassTree classTree, VisitorState state) {
    return classTree.getMembers().stream()
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast)
        .anyMatch(method -> ASTHelpers.hasAnnotation(method, BEFORE_TEMPLATE, state));
  }

  // XXX: Derive the description from the `@Description` annotation if present.
  private static String getDescription(ClassTree element, VisitorState state) {
    String docComment = state.getElements().getDocComment(ASTHelpers.getSymbol(element));
    return docComment != null ? docComment.strip() : "";
  }
}
