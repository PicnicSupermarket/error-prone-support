package tech.picnic.errorprone.documentation;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import java.util.List;
import java.util.Optional;
import javax.lang.model.element.Element;
import tech.picnic.errorprone.documentation.ProjectInfo.RefasterRuleCollection;
import tech.picnic.errorprone.documentation.ProjectInfo.RefasterRuleCollection.Rule;

/**
 * An {@link Extractor} that describes how to extract data from classes annotated with
 * {@code @OnlineDocumentation} that contain Refaster rules.
 */
// XXX: Also extract information from the `@TypeMigration` annotation.
@AutoService(Extractor.class)
@SuppressWarnings("rawtypes" /* See https://github.com/google/auto/issues/870. */)
public record RefasterRuleCollectionExtractor() implements Extractor<RefasterRuleCollection> {
  private static final String ONLINE_DOCUMENTATION_SIMPLE_NAME = "OnlineDocumentation";
  private static final String BEFORE_TEMPLATE_SIMPLE_NAME = "BeforeTemplate";
  private static final String AFTER_TEMPLATE_SIMPLE_NAME = "AfterTemplate";
  // XXX: Avoid duplication with `OnlineDocumentation`.
  private static final String DEFAULT_URL_PATTERN =
      "https://error-prone.picnic.tech/refasterrules/${topLevelClassName}#${nestedClassName}";
  private static final String TOP_LEVEL_CLASS_PLACEHOLDER = "${topLevelClassName}";
  private static final String NESTED_CLASS_PLACEHOLDER = "${nestedClassName}";

  @Override
  public String identifier() {
    return "refaster-rule-collection";
  }

  @Override
  public Optional<RefasterRuleCollection> tryExtract(ClassTree tree, VisitorState state) {
    AnnotationTree annotation =
        ASTHelpers.getAnnotationWithSimpleName(
            ASTHelpers.getAnnotations(tree), ONLINE_DOCUMENTATION_SIMPLE_NAME);
    if (annotation == null) {
      return Optional.empty();
    }

    String name = tree.getSimpleName().toString();
    String urlPattern = getUrlPattern(annotation);
    ImmutableList<Rule> rules = extractRules(tree, name, urlPattern, state);

    return Optional.of(
        new RefasterRuleCollection(
            state.getPath().getCompilationUnit().getSourceFile().toUri(),
            name,
            getDocComment(ASTHelpers.getSymbol(tree), state),
            resolveLink(urlPattern, name, ""),
            rules));
  }

  private static String getUrlPattern(AnnotationTree annotation) {
    List<? extends ExpressionTree> args = annotation.getArguments();
    if (args.isEmpty()) {
      return DEFAULT_URL_PATTERN;
    }

    @Var ExpressionTree arg = args.getFirst();
    if (arg instanceof AssignmentTree assignment) {
      arg = assignment.getExpression();
    }
    String value = ASTHelpers.constValue(arg, String.class);
    return value != null ? value : DEFAULT_URL_PATTERN;
  }

  private static String getDocComment(Element element, VisitorState state) {
    String docComment = state.getElements().getDocComment(element);
    return docComment != null ? docComment.strip() : "";
  }

  private static ImmutableList<Rule> extractRules(
      ClassTree tree, String topLevelName, String urlPattern, VisitorState state) {
    return tree.getMembers().stream()
        .filter(ClassTree.class::isInstance)
        .map(ClassTree.class::cast)
        .filter(RefasterRuleCollectionExtractor::isRefasterRule)
        .map(
            innerClass -> {
              String ruleName = innerClass.getSimpleName().toString();
              return new Rule(
                  ruleName,
                  getDocComment(ASTHelpers.getSymbol(innerClass), state),
                  resolveLink(urlPattern, topLevelName, ruleName),
                  SUGGESTION);
            })
        .collect(toImmutableList());
  }

  private static boolean isRefasterRule(ClassTree classTree) {
    return classTree.getMembers().stream()
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast)
        .anyMatch(RefasterRuleCollectionExtractor::hasRefasterAnnotation);
  }

  private static boolean hasRefasterAnnotation(MethodTree method) {
    return ASTHelpers.getAnnotationWithSimpleName(
                method.getModifiers().getAnnotations(), BEFORE_TEMPLATE_SIMPLE_NAME)
            != null
        || ASTHelpers.getAnnotationWithSimpleName(
                method.getModifiers().getAnnotations(), AFTER_TEMPLATE_SIMPLE_NAME)
            != null;
  }

  private static String resolveLink(String urlPattern, String topLevelName, String nestedName) {
    return urlPattern
        .replace(TOP_LEVEL_CLASS_PLACEHOLDER, topLevelName)
        .replace(NESTED_CLASS_PLACEHOLDER, nestedName);
  }
}
