package tech.picnic.errorprone.documentation;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.isType;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.AnnotationMatcherUtils;
import com.google.errorprone.matchers.Matcher;
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
// XXX: This class doesn't support `@OnlineDocumentation` with a custom documentation URL. Consider
// extracting it and including it in the generated `RefasterRuleCollection`s. (But if we'd be
// accurate about this, then this extractor should also support the default URL construction logic
// in `AnnotatedCompositeCodeTransformer`.)
// XXX: Also extract information from the `@TypeMigration` annotation.
@AutoService(Extractor.class)
@SuppressWarnings("rawtypes" /* See https://github.com/google/auto/issues/870. */)
public record RefasterRuleCollectionExtractor() implements Extractor<RefasterRuleCollection> {
  private static final Matcher<Tree> BEFORE_TEMPLATE =
      hasAnnotation("com.google.errorprone.refaster.annotation.BeforeTemplate");
  private static final MultiMatcher<Tree, AnnotationTree> ONLINE_DOCUMENTATION =
      annotations(
          AT_LEAST_ONE, isType("tech.picnic.errorprone.refaster.annotation.OnlineDocumentation"));
  private static final MultiMatcher<Tree, AnnotationTree> DESCRIPTION =
      annotations(AT_LEAST_ONE, isType("tech.picnic.errorprone.refaster.annotation.Description"));
  private static final MultiMatcher<Tree, AnnotationTree> SEVERITY =
      annotations(AT_LEAST_ONE, isType("tech.picnic.errorprone.refaster.annotation.Severity"));

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
        .anyMatch(method -> BEFORE_TEMPLATE.matches(method, state));
  }

  private static String getDescription(ClassTree tree, VisitorState state) {
    MultiMatchResult<AnnotationTree> descriptionMatch = DESCRIPTION.multiMatchResult(tree, state);
    if (descriptionMatch.matches()) {
      return requireNonNull(
          ASTHelpers.constValue(
              AnnotationMatcherUtils.getArgument(descriptionMatch.onlyMatchingNode(), "value"),
              String.class),
          "@Description annotation without `value` argument");
    }

    // XXX: If we extract the rule description from the Javadoc, do we need the `@Description`
    // annotation at all?
    // XXX: Consider whether/how to further post-process the Javadoc.
    return requireNonNullElse(state.getElements().getDocComment(ASTHelpers.getSymbol(tree)), "")
        .strip();
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
