package tech.picnic.errorprone.testngjunit;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.sun.source.tree.Tree.Kind.NEW_ARRAY;
import static java.util.stream.Collectors.joining;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.ErrorProneToken;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.ReturnTree;
import com.sun.tools.javac.parser.Tokens.Comment;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import tech.picnic.errorprone.util.SourceCode;

// XXX: Can this one also implement a `Migrator`?
/** A helper class that migrates a TestNG {@code DataProvider} to a JUnit {@code MethodSource}. */
final class DataProviderMigrator {
  private DataProviderMigrator() {}
  /** This regular expression replaces matches instances of `this.getClass()` and `getClass()`. */
  private static final Pattern GET_CLASS =
      Pattern.compile("((?<!\\b\\.)|(\\bthis\\.))(getClass\\(\\))");

  /**
   * Tells whether the specified {@code DataProvider} can be migrated.
   *
   * @param methodTree The dataprovider methode tree.
   * @return {@code true} if the data provider can be migrated or else {@code false}.
   */
  static boolean canFix(MethodTree methodTree) {
    return getDataProviderReturnTree(getReturnTree(methodTree)).isPresent();
  }

  /**
   * Create the {@link SuggestedFix} required to migrate a TestNG {@code DataProvider} to a JUnit
   * {@code MethodSource}.
   *
   * @param classTree The class containing the data provider.
   * @param methodTree The data provider method.
   * @param state The {@link VisitorState}.
   * @return An {@link Optional} containing the created fix.
   */
  static Optional<SuggestedFix> createFix(
      ClassTree classTree, MethodTree methodTree, VisitorState state) {
    return tryMigrateDataProvider(methodTree, classTree, state);
  }

  private static Optional<SuggestedFix> tryMigrateDataProvider(
      MethodTree methodTree, ClassTree classTree, VisitorState state) {
    ReturnTree returnTree = getReturnTree(methodTree);

    return getDataProviderReturnTree(returnTree)
        .map(
            dataProviderReturnTree ->
                SuggestedFix.builder()
                    .addStaticImport("org.junit.jupiter.params.provider.Arguments.arguments")
                    .addImport("java.util.stream.Stream")
                    .addImport("org.junit.jupiter.params.provider.Arguments")
                    .delete(methodTree)
                    .postfixWith(
                        methodTree,
                        buildMethodSource(
                            classTree.getSimpleName().toString(),
                            methodTree.getName().toString(),
                            methodTree,
                            returnTree,
                            dataProviderReturnTree,
                            state))
                    .build());
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
        new StringBuilder()
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
    int startPos = ASTHelpers.getStartPosition(newArrayTree);
    int endPos = state.getEndPosition(newArrayTree);
    ImmutableMap<Integer, List<Comment>> comments =
        state.getOffsetTokens(startPos, endPos).stream()
            .collect(toImmutableMap(ErrorProneToken::pos, ErrorProneToken::comments));

    StringBuilder argumentsBuilder = new StringBuilder();
    argumentsBuilder.append(
        newArrayTree.getInitializers().stream()
            .map(
                expression ->
                    wrapTestValueWithArguments(
                        expression,
                        comments.getOrDefault(
                            ASTHelpers.getStartPosition(expression), ImmutableList.of()),
                        state))
            .collect(joining(",")));

    /*
     * This replaces all instances of `{,this.}getClass()` with the fully qualified class name to
     * retain functionality in static context.
     */
    return GET_CLASS
        .matcher(String.format("Stream.of(%s%n)", argumentsBuilder))
        .replaceAll(className + ".class");
  }

  /**
   * Wraps a value in {@code org.junit.jupiter.params.provider#arguments()}.
   *
   * <p>Drops curly braces from array initialisation values.
   */
  private static String wrapTestValueWithArguments(
      ExpressionTree tree, List<Comment> comments, VisitorState state) {
    String source = SourceCode.treeToString(tree, state);

    String argumentValue =
        tree.getKind() == NEW_ARRAY ? source.substring(1, source.length() - 1) : source;

    return String.format(
        "\t\t%s%n\t\targuments(%s)",
        comments.stream().map(Comment::getText).collect(joining("\n")), argumentValue);
  }
}
