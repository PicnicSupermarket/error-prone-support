package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.enclosingClass;
import static com.google.errorprone.matchers.Matchers.hasModifier;
import static com.google.errorprone.matchers.Matchers.isType;
import static com.google.errorprone.matchers.Matchers.not;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static tech.picnic.errorprone.bugpatterns.util.ConflictDetection.findMethodRenameBlocker;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.bugpatterns.util.MoreJUnitMatchers.HAS_METHOD_SOURCE;
import static tech.picnic.errorprone.bugpatterns.util.MoreJUnitMatchers.TEST_METHOD;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.parser.Tokens.TokenKind;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import tech.picnic.errorprone.bugpatterns.util.MoreASTHelpers;
import tech.picnic.errorprone.bugpatterns.util.MoreJUnitMatchers;

/**
 * A {@link BugChecker} that flags non-canonical JUnit factory method declarations.
 *
 * <p>At Picnic, we consider a JUnit factory method canonical if it
 *
 * <ul>
 *   <li>has the same name as the test method it provides test cases for, but with a `TestCases`
 *       suffix, and
 *   <li>has a comment which connects the return statement to the names of the parameters in the
 *       corresponding test method.
 * </ul>
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "JUnit factory method declaration can likely be improved",
    link = BUG_PATTERNS_BASE_URL + "JUnitFactoryMethodDeclaration",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = STYLE)
public final class JUnitFactoryMethodDeclaration extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;

  private static final Matcher<MethodTree> HAS_UNMODIFIABLE_SIGNATURE =
      anyOf(
          annotations(AT_LEAST_ONE, isType("java.lang.Override")),
          allOf(
              not(hasModifier(FINAL)),
              not(hasModifier(PRIVATE)),
              enclosingClass(hasModifier(ABSTRACT))));

  /** Instantiates a new {@link JUnitFactoryMethodDeclaration} instance. */
  public JUnitFactoryMethodDeclaration() {}

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (!TEST_METHOD.matches(tree, state) || !HAS_METHOD_SOURCE.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    AnnotationTree methodSourceAnnotation =
        ASTHelpers.getAnnotationWithSimpleName(
            tree.getModifiers().getAnnotations(), "MethodSource");

    if (methodSourceAnnotation == null) {
      return Description.NO_MATCH;
    }

    Optional<String> factoryMethodName =
        MoreJUnitMatchers.extractSingleFactoryMethodName(methodSourceAnnotation);

    if (factoryMethodName.isEmpty()) {
      /* If a test has multiple factory methods, not all of them can be given the desired name. */
      return Description.NO_MATCH;
    }

    ImmutableList<MethodTree> factoryMethods =
        Optional.ofNullable(state.findEnclosing(ClassTree.class))
            .map(
                enclosingClass ->
                    MoreASTHelpers.findMethods(factoryMethodName.orElseThrow(), state))
            .stream()
            .flatMap(Collection::stream)
            .filter(methodTree -> methodTree.getParameters().isEmpty())
            .collect(toImmutableList());

    if (factoryMethods.size() != 1) {
      /* If we cannot reliably find the factory method, err on the side of not proposing any fixes. */
      return Description.NO_MATCH;
    }

    ImmutableList<Description> fixes =
        getSuggestedFixes(
            tree, methodSourceAnnotation, Iterables.getOnlyElement(factoryMethods), state);

    /* Even though we match on the test method, none of the fixes apply to it directly, so we report
    the fixes separately using `VisitorState::reportMatch`. */
    fixes.forEach(state::reportMatch);
    return Description.NO_MATCH;
  }

  private ImmutableList<Description> getSuggestedFixes(
      MethodTree tree,
      AnnotationTree methodSourceAnnotation,
      MethodTree factoryMethod,
      VisitorState state) {
    ImmutableList<Description> factoryMethodNameFixes =
        getFactoryMethodNameFixes(tree, methodSourceAnnotation, factoryMethod, state);

    ImmutableList<Description> commentFixes =
        getReturnStatementCommentFixes(tree, factoryMethod, state);

    return ImmutableList.<Description>builder()
        .addAll(factoryMethodNameFixes)
        .addAll(commentFixes)
        .build();
  }

  private ImmutableList<Description> getFactoryMethodNameFixes(
      MethodTree tree,
      AnnotationTree methodSourceAnnotation,
      MethodTree factoryMethod,
      VisitorState state) {
    String expectedFactoryMethodName = tree.getName() + "TestCases";

    if (HAS_UNMODIFIABLE_SIGNATURE.matches(factoryMethod, state)
        || factoryMethod.getName().toString().equals(expectedFactoryMethodName)) {
      return ImmutableList.of();
    }

    Optional<String> blocker = findMethodRenameBlocker(expectedFactoryMethodName, state);
    if (blocker.isPresent()) {
      reportMethodRenameBlocker(
          factoryMethod, blocker.orElseThrow(), expectedFactoryMethodName, state);
      return ImmutableList.of();
    } else {
      return ImmutableList.of(
          buildDescription(methodSourceAnnotation)
              .setMessage(
                  String.format(
                      "The test cases should be supplied by a method named `%s`",
                      expectedFactoryMethodName))
              .addFix(
                  SuggestedFixes.updateAnnotationArgumentValues(
                          methodSourceAnnotation,
                          state,
                          "value",
                          ImmutableList.of("\"" + expectedFactoryMethodName + "\""))
                      .build())
              .build(),
          buildDescription(factoryMethod)
              .setMessage(
                  String.format(
                      "The test cases should be supplied by a method named `%s`",
                      expectedFactoryMethodName))
              .addFix(SuggestedFixes.renameMethod(factoryMethod, expectedFactoryMethodName, state))
              .build());
    }
  }

  private void reportMethodRenameBlocker(
      MethodTree tree, String reason, String suggestedName, VisitorState state) {
    state.reportMatch(
        buildDescription(tree)
            .setMessage(
                String.format(
                    "The test cases should be supplied by a method named `%s` (but note that %s)",
                    suggestedName, reason))
            .build());
  }

  private ImmutableList<Description> getReturnStatementCommentFixes(
      MethodTree testMethod, MethodTree factoryMethod, VisitorState state) {
    ImmutableList<String> parameterNames =
        testMethod.getParameters().stream()
            .map(VariableTree::getName)
            .map(Object::toString)
            .collect(toImmutableList());

    String expectedComment = parameterNames.stream().collect(joining(", ", "/* { ", " } */"));

    List<? extends StatementTree> statements = factoryMethod.getBody().getStatements();

    Stream<? extends StatementTree> returnStatementsNeedingComment =
        Streams.mapWithIndex(statements.stream(), IndexedStatement::new)
            .filter(indexedStatement -> indexedStatement.getStatement().getKind() == Kind.RETURN)
            .filter(
                indexedStatement ->
                    !hasExpectedComment(
                        factoryMethod,
                        expectedComment,
                        statements,
                        indexedStatement.getIndex(),
                        state))
            .map(IndexedStatement::getStatement);

    return returnStatementsNeedingComment
        .map(
            s ->
                buildDescription(s)
                    .setMessage(
                        "The return statement should be prefixed by a comment giving the names of the test case parameters")
                    .addFix(SuggestedFix.prefixWith(s, expectedComment + "\n"))
                    .build())
        .collect(toImmutableList());
  }

  private static boolean hasExpectedComment(
      MethodTree factoryMethod,
      String expectedComment,
      List<? extends StatementTree> statements,
      long statementIndex,
      VisitorState state) {
    int startPosition =
        statementIndex > 0
            ? state.getEndPosition(statements.get((int) statementIndex - 1))
            : ASTHelpers.getStartPosition(factoryMethod);
    int endPosition = state.getEndPosition(statements.get((int) statementIndex));

    ImmutableList<Comment> comments =
        extractReturnStatementComments(startPosition, endPosition, state);

    return comments.stream()
        .map(Comment::getText)
        .anyMatch(comment -> comment.equals(expectedComment));
  }

  private static ImmutableList<Comment> extractReturnStatementComments(
      int startPosition, int endPosition, VisitorState state) {
    return state.getOffsetTokens(startPosition, endPosition).stream()
        .filter(t -> t.kind() == TokenKind.RETURN)
        .flatMap(errorProneToken -> errorProneToken.comments().stream())
        .collect(toImmutableList());
  }

  private static final class IndexedStatement {
    private final StatementTree statement;
    private final long index;

    private IndexedStatement(StatementTree statement, long index) {
      this.statement = statement;
      this.index = index;
    }

    public StatementTree getStatement() {
      return statement;
    }

    public long getIndex() {
      return index;
    }
  }
}
