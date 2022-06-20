package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.REFACTORING;
import static com.google.errorprone.matchers.Matchers.isType;
import static com.sun.source.tree.Tree.Kind.NEW_ARRAY;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.ErrorProneToken;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.ReturnTree;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.util.Name;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} which flags TestNG {@link org.testng.annotations.DataProvider} methods and
 * provides an equivalent Jupiter {@link org.junit.jupiter.params.ParameterizedTest} replacement.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Migrate TestNG DataProvider to Jupiter argument streams",
    linkType = NONE,
    tags = REFACTORING,
    severity = ERROR)
public final class TestNGDataProvider extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<AnnotationTree> TESTNG_DATAPROVIDER_ANNOTATION =
      isType("org.testng.annotations.DataProvider");

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    Optional<? extends AnnotationTree> dataProviderAnnotation =
        ASTHelpers.getAnnotations(tree).stream()
            .filter(annotation -> TESTNG_DATAPROVIDER_ANNOTATION.matches(annotation, state))
            .findFirst();
    if (dataProviderAnnotation.isEmpty()) {
      return Description.NO_MATCH;
    }

    String methodName = tree.getName().toString();
    Name migratedName = state.getName(methodName + "Junit");
    ClassTree classTree = state.findEnclosing(ClassTree.class);
    if (classTree == null
        || isMethodAlreadyMigratedInEnclosingClass(ASTHelpers.getSymbol(classTree), migratedName)) {
      return Description.NO_MATCH;
    }

    ReturnTree returnTree = getReturnTree(tree);
    Optional<NewArrayTree> returnArrayTree = getDataProviderReturnTree(returnTree);
    if (returnArrayTree.isEmpty()) {
      return Description.NO_MATCH;
    }

    return describeMatch(
        dataProviderAnnotation.get(),
        SuggestedFix.builder()
            .addStaticImport("org.junit.jupiter.params.provider.Arguments.arguments")
            .addImport("java.util.stream.Stream")
            .addImport("org.junit.jupiter.params.provider.Arguments")
            .merge(
                SuggestedFix.postfixWith(
                    tree,
                    buildMethodSource(
                        classTree.getSimpleName().toString(),
                        migratedName.toString(),
                        tree,
                        returnTree,
                        returnArrayTree.orElseThrow(),
                        state)))
            .build());
  }

  private static boolean isMethodAlreadyMigratedInEnclosingClass(
      ClassSymbol enclosingClassSymbol, Name methodName) {
    return enclosingClassSymbol.members().getSymbolsByName(methodName).iterator().hasNext();
  }

  private static ReturnTree getReturnTree(MethodTree methodTree) {
    return methodTree.getBody().getStatements().stream()
        .filter(ReturnTree.class::isInstance)
        .findFirst()
        .map(ReturnTree.class::cast)
        .orElseThrow();
  }

  private static Optional<NewArrayTree> getDataProviderReturnTree(ReturnTree returnTree) {
    if (returnTree.getExpression().getKind() != NEW_ARRAY
        || ((NewArrayTree) returnTree.getExpression()).getInitializers().isEmpty()) {
      return Optional.empty();
    }

    return Optional.of((NewArrayTree) returnTree.getExpression());
  }

  private static String buildMethodSource(
      String className,
      String name,
      MethodTree methodTree,
      ReturnTree returnTree,
      NewArrayTree newArrayTree,
      VisitorState state) {
    StringBuilder sourceBuilder =
        new StringBuilder(
                "@SuppressWarnings(\"UnusedMethod\" /* This is an intermediate state for the JUnit migration. */)\n")
            .append("  private static Stream<Arguments> ")
            .append(name)
            .append(" () ");

    if (!methodTree.getThrows().isEmpty()) {
      sourceBuilder
          .append(" throws ")
          .append(
              methodTree.getThrows().stream()
                  .filter(IdentifierTree.class::isInstance)
                  .map(IdentifierTree.class::cast)
                  .map(identifierTree -> identifierTree.getName().toString())
                  .collect(joining(", ")));
    }

    return sourceBuilder
        .append(" {\n")
        .append(extractMethodBodyWithoutReturnStatement(methodTree, returnTree, state))
        .append(" return ")
        .append(buildArgumentStream(className, newArrayTree, state))
        .append(";\n}")
        .toString();
  }

  private static String extractMethodBodyWithoutReturnStatement(
      MethodTree methodTree, ReturnTree returnTree, VisitorState state) {
    String body = SourceCode.treeToString(methodTree.getBody(), state);
    return body.substring(2, body.indexOf(SourceCode.treeToString(returnTree, state)) - 1);
  }

  private static String buildArgumentStream(
      String className, NewArrayTree newArrayTree, VisitorState state) {
    StringBuilder argumentsBuilder = new StringBuilder();

    int startPos = ASTHelpers.getStartPosition(newArrayTree);
    int endPos = state.getEndPosition(newArrayTree);
    Map<Integer, List<Comment>> comments =
        state.getOffsetTokens(startPos, endPos).stream()
            .collect(
                toMap(ErrorProneToken::pos, ErrorProneToken::comments, (a, b) -> b, HashMap::new));
    argumentsBuilder.append(
        newArrayTree.getInitializers().stream()
            .map(
                expression ->
                    buildArguments(
                        expression,
                        comments.getOrDefault(
                            ASTHelpers.getStartPosition(expression), ImmutableList.of()),
                        state))
            .collect(joining(",\n")));

    // This regex expression replaces all instances of "this.getClass()" or "getClass()"
    // with the fully qualified class name to retain functionality in static context.
    return String.format("Stream.of(\n%s\n  )", argumentsBuilder)
        .replaceAll("((?<!\\b\\.)|(\\bthis\\.))(getClass\\(\\))", className + ".class");
  }

  private static String buildArguments(
      ExpressionTree expressionTree, List<Comment> comments, VisitorState state) {
    if (expressionTree.getKind() == NEW_ARRAY) {
      return buildArgumentsFromArray(((NewArrayTree) expressionTree), comments, state);
    } else {
      return buildArgumentsFromExpression(expressionTree, comments, state);
    }
  }

  private static String buildArgumentsFromExpression(
      ExpressionTree expressionTree, List<Comment> comments, VisitorState state) {
    return String.format(
        "\t\t%s\n\t\targuments(%s)",
        comments.stream().map(Comment::getText).collect(joining("\n")),
        SourceCode.treeToString(expressionTree, state));
  }

  private static String buildArgumentsFromArray(
      NewArrayTree argumentArray, List<Comment> comments, VisitorState state) {
    String argSource = SourceCode.treeToString(argumentArray, state);
    return String.format(
        "\t\t%s\n\t\targuments(%s)",
        comments.stream().map(Comment::getText).collect(joining("\n")),
        argSource.substring(1, argSource.length() - 1));
  }
}
