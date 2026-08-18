package tech.picnic.errorprone.documentation;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.isType;
import static java.util.function.Predicate.not;

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
    /*
     * Only top-level rule collection classes are supported. XXX: This guard yields an unkillable
     * mutant, as extractors are currently only ever handed top-level classes; it is retained to
     * make the assumption explicit.
     */
    if (ASTHelpers.findEnclosingNode(state.getPath(), ClassTree.class) != null) {
      return Optional.empty();
    }

    MultiMatchResult<AnnotationTree> hasOnlineDocumentation =
        ONLINE_DOCUMENTATION.multiMatchResult(tree, state);
    if (!hasOnlineDocumentation.matches()) {
      return Optional.empty();
    }

    if (!hasOnlineDocumentation.onlyMatchingNode().getArguments().isEmpty()) {
      /* This is a rule that is meant to be hosted at a custom location: skip it. */
      return Optional.empty();
    }

    /*
     * A rule collection's Javadoc describes the collection as a whole, so unlike its
     * `@Description` it is not inherited by the rules it contains.
     */
    Optional<String> annotatedDescription = getAnnotatedDescription(tree, state);
    return Optional.of(
        new RefasterRuleCollection(
            state.getPath().getCompilationUnit().getSourceFile().toUri(),
            tree.getSimpleName().toString(),
            annotatedDescription.or(() -> getJavadoc(tree, state)).orElse(""),
            getRules(
                tree,
                state,
                annotatedDescription.orElse(""),
                getSeverity(tree, state).orElse(SUGGESTION))));
  }

  private static ImmutableList<Rule> getRules(
      ClassTree tree,
      VisitorState state,
      String inheritedDescription,
      SeverityLevel inheritedSeverity) {
    return tree.getMembers().stream()
        .filter(ClassTree.class::isInstance)
        .map(ClassTree.class::cast)
        .filter(innerClass -> isRefasterRule(innerClass, state))
        .map(
            rule ->
                new Rule(
                    rule.getSimpleName().toString(),
                    getAnnotatedDescription(rule, state)
                        .or(() -> getJavadoc(rule, state))
                        .orElse(inheritedDescription),
                    getSeverity(rule, state).orElse(inheritedSeverity)))
        .collect(toImmutableList());
  }

  private static boolean isRefasterRule(ClassTree classTree, VisitorState state) {
    return classTree.getMembers().stream()
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast)
        .anyMatch(method -> BEFORE_TEMPLATE.matches(method, state));
  }

  // XXX: If we extract the rule description from the Javadoc, do we need the `@Description`
  // annotation at all? (Only the latter is inherited from the enclosing rule collection, but if
  // desired we could implement similar logic for Javadoc as well.)
  private static Optional<String> getAnnotatedDescription(ClassTree tree, VisitorState state) {
    return getAnnotationValue(DESCRIPTION, tree, state)
        .map(value -> ASTHelpers.constValue(value, String.class));
  }

  // XXX: Consider whether/how to further post-process the Javadoc.
  private static Optional<String> getJavadoc(ClassTree tree, VisitorState state) {
    return Optional.ofNullable(state.getElements().getDocComment(ASTHelpers.getSymbol(tree)))
        .map(String::strip)
        .filter(not(String::isEmpty));
  }

  private static Optional<SeverityLevel> getSeverity(ClassTree tree, VisitorState state) {
    return getAnnotationValue(SEVERITY, tree, state)
        .map(ASTHelpers::getSymbol)
        .map(symbol -> SeverityLevel.valueOf(symbol.getSimpleName().toString()));
  }

  private static Optional<ExpressionTree> getAnnotationValue(
      MultiMatcher<Tree, AnnotationTree> matcher, ClassTree tree, VisitorState state) {
    MultiMatchResult<AnnotationTree> matchResult = matcher.multiMatchResult(tree, state);
    return matchResult.matches()
        ? Optional.ofNullable(
            AnnotationMatcherUtils.getArgument(matchResult.onlyMatchingNode(), "value"))
        : Optional.empty();
  }
}
