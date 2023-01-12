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
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.parser.Tokens.TokenKind;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import tech.picnic.errorprone.bugpatterns.util.MoreASTHelpers;
import tech.picnic.errorprone.bugpatterns.util.MoreJUnitMatchers;

/**
 * A {@link BugChecker} that flags non-canonical JUnit factory method declarations.
 *
 * <p>At Picnic, we consider a JUnit factory method canonical if it:
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
              not(hasModifier(Modifier.FINAL)),
              not(hasModifier(Modifier.PRIVATE)),
              enclosingClass(hasModifier(Modifier.ABSTRACT))));

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

    Optional<ImmutableList<MethodTree>> factoryMethods =
        MoreJUnitMatchers.extractSingleFactoryMethodName(methodSourceAnnotation)
            .map(name -> MoreASTHelpers.findMethods(name, state));

    if (factoryMethods.isEmpty() || factoryMethods.orElseThrow().size() != 1) {
      return Description.NO_MATCH;
    }

    MethodTree factoryMethod = Iterables.getOnlyElement(factoryMethods.orElseThrow());
    ImmutableList<Description> descriptions =
        ImmutableList.<Description>builder()
            .addAll(
                getFactoryMethodNameFixes(
                    tree.getName(), methodSourceAnnotation, factoryMethod, state))
            .addAll(getReturnStatementCommentFixes(tree, factoryMethod, state))
            .build();

    descriptions.forEach(state::reportMatch);
    return Description.NO_MATCH;
  }

  private ImmutableList<Description> getFactoryMethodNameFixes(
      Name methodName,
      AnnotationTree methodSourceAnnotation,
      MethodTree factoryMethod,
      VisitorState state) {
    String expectedFactoryMethodName = methodName + "TestCases";

    if (HAS_UNMODIFIABLE_SIGNATURE.matches(factoryMethod, state)
        || factoryMethod.getName().toString().equals(expectedFactoryMethodName)) {
      return ImmutableList.of();
    }

    Optional<String> blocker =
        findMethodRenameBlocker(
            ASTHelpers.getSymbol(factoryMethod), expectedFactoryMethodName, state);
    if (blocker.isPresent()) {
      reportMethodRenameBlocker(
          factoryMethod, blocker.orElseThrow(), expectedFactoryMethodName, state);
      return ImmutableList.of();
    }

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
