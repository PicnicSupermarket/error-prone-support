package tech.picnic.errorprone.utils;

import static com.sun.tools.javac.parser.Tokens.TokenKind.RPAREN;
import static com.sun.tools.javac.util.Position.NOPOS;
import static java.util.stream.Collectors.joining;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.ErrorProneToken;
import com.google.errorprone.util.ErrorProneTokens;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.Position;
import java.util.List;
import java.util.Optional;
import javax.lang.model.SourceVersion;

/**
 * A collection of Error Prone utility methods for dealing with the source code representation of
 * AST nodes.
 */
public final class SourceCode {
  /** The complement of {@link CharMatcher#whitespace()}. */
  private static final CharMatcher NON_WHITESPACE_MATCHER = CharMatcher.whitespace().negate();

  /** The Java syntax that indicates the start and end of a text block. */
  public static final String TEXT_BLOCK_DELIMITER = "\"\"\"";

  /**
   * The string separating lines in a Java text block, independently of the platform on which the
   * code is compiled.
   */
  public static final String TEXT_BLOCK_LINE_SEPARATOR = "\n";

  private SourceCode() {}

  /**
   * Tells whether the given string is a valid identifier in the Java language.
   *
   * @param str The string of interest.
   * @return {@code true} if the given string is a valid identifier in the Java language.
   * @see <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-3.html#jls-3.8">JDK 17 JLS
   *     section 3.8: Identifiers</a>
   */
  public static boolean isValidIdentifier(String str) {
    return str.indexOf('.') < 0 && SourceVersion.isName(str);
  }

  /**
   * Tells whether the given expression is a text block.
   *
   * @param tree The AST node of interest.
   * @param state A {@link VisitorState} describing the context in which the given {@link
   *     ExpressionTree} is found.
   * @return {@code true} iff the given expression is a text block.
   */
  // XXX: Add tests!
  public static boolean isTextBlock(ExpressionTree tree, VisitorState state) {
    if (tree.getKind() != Tree.Kind.STRING_LITERAL) {
      return false;
    }

    /* If the source code is unavailable then we assume that this literal is _not_ a text block. */
    String src = state.getSourceForNode(tree);
    return src != null && src.startsWith(TEXT_BLOCK_DELIMITER);
  }

  /**
   * Returns a string representation of the given {@link Tree}, preferring the original source code
   * (if available) over its prettified representation.
   *
   * @param tree The AST node of interest.
   * @param state A {@link VisitorState} describing the context in which the given {@link Tree} is
   *     found.
   * @return A non-{@code null} string.
   */
  public static String treeToString(Tree tree, VisitorState state) {
    String src = state.getSourceForNode(tree);
    return src != null ? src : tree.toString();
  }

  /**
   * Returns a Java string constant expression (i.e., a quoted string) representing the given input.
   *
   * @param value The value of interest.
   * @param state A {@link VisitorState} describing the context in which the given {@link Tree} is
   *     found.
   * @return A non-{@code null} string.
   * @apiNote This method differs from {@link com.sun.tools.javac.util.Constants#format(Object)} in
   *     that it does not superfluously escape single quote characters. It is different from {@link
   *     VisitorState#getConstantExpression(Object)} in that it is more performant and accepts any
   *     {@link CharSequence} instance.
   */
  // XXX: Drop this method if https://github.com/google/error-prone/pull/4586 is merged and released
  // with the proposed `CharSequence` compatibility change.
  public static String toStringConstantExpression(Object value, VisitorState state) {
    return state.getConstantExpression(value instanceof CharSequence ? value.toString() : value);
  }

  /**
   * Constructs a multi-line string by joining the given source code lines, if they all represent
   * compile-time constants.
   *
   * @implNote Lines are always joined with {@value #TEXT_BLOCK_LINE_SEPARATOR}, independent of the
   *     platform on which this method is executed. This ensures consistent line separation in case
   *     some of the lines are multi-line text blocks.
   * @param sourceLines The source code lines of interest.
   * @return A non-empty optional iff all source code lines represent compile-time constants.
   */
  // XXX: Test!
  // XXX: This method doesn't add a trailing newline for the benefit of one caller, but that's
  // perhaps a bit awkward.
  public static Optional<String> joinConstantSourceCodeLines(
      List<? extends ExpressionTree> sourceLines) {
    StringBuilder source = new StringBuilder();

    for (ExpressionTree sourceLine : sourceLines) {
      if (!source.isEmpty()) {
        source.append(TEXT_BLOCK_LINE_SEPARATOR);
      }

      String value = ASTHelpers.constValue(sourceLine, String.class);
      if (value == null) {
        return Optional.empty();
      }

      source.append(value);
    }

    return Optional.of(source.toString());
  }

  /**
   * Creates a {@link SuggestedFix} for the deletion of the given {@link Tree}, including any
   * whitespace that follows it.
   *
   * <p>Removing trailing whitespace may prevent the introduction of an empty line at the start of a
   * code block; such empty lines are not removed when formatting the code using Google Java Format.
   *
   * @param tree The AST node of interest.
   * @param state A {@link VisitorState} describing the context in which the given {@link Tree} is
   *     found.
   * @return A non-{@code null} {@link SuggestedFix} similar to one produced by {@link
   *     SuggestedFix#delete(Tree)}.
   */
  public static SuggestedFix deleteWithTrailingWhitespace(Tree tree, VisitorState state) {
    CharSequence sourceCode = state.getSourceCode();
    int endPos = state.getEndPosition(tree);
    if (sourceCode == null || endPos == NOPOS) {
      /* We can't identify the trailing whitespace; delete just the tree. */
      return SuggestedFix.delete(tree);
    }

    int whitespaceEndPos = NON_WHITESPACE_MATCHER.indexIn(sourceCode, endPos);
    return SuggestedFix.replace(
        ((DiagnosticPosition) tree).getStartPosition(),
        whitespaceEndPos == -1 ? sourceCode.length() : whitespaceEndPos,
        "");
  }

  /**
   * Creates a {@link SuggestedFix} for the replacement of the given {@link MethodInvocationTree}
   * with just the arguments to the method invocation, effectively "unwrapping" the method
   * invocation.
   *
   * <p>For example, given the method invocation {@code foo.bar(1, 2, 3)}, this method will return a
   * {@link SuggestedFix} that replaces the method invocation with {@code 1, 2, 3}.
   *
   * <p>This method aims to preserve the original formatting of the method invocation, including
   * whitespace and comments.
   *
   * @param tree The AST node to be unwrapped.
   * @param state A {@link VisitorState} describing the context in which the given {@link
   *     MethodInvocationTree} is found.
   * @return A non-{@code null} {@link SuggestedFix}.
   */
  public static SuggestedFix unwrapMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    CharSequence sourceCode = state.getSourceCode();
    int startPosition = state.getEndPosition(tree.getMethodSelect());
    int endPosition = state.getEndPosition(tree);

    if (sourceCode == null || startPosition == Position.NOPOS || endPosition == Position.NOPOS) {
      return unwrapMethodInvocationDroppingWhitespaceAndComments(tree, state);
    }

    ImmutableList<ErrorProneToken> tokens =
        ErrorProneTokens.getTokens(
            sourceCode.subSequence(startPosition, endPosition).toString(), state.context);

    Optional<Integer> leftParenPosition =
        tokens.stream().findFirst().map(t -> startPosition + t.endPos());
    Optional<Integer> rightParenPosition =
        Streams.findLast(tokens.stream().filter(t -> t.kind() == RPAREN))
            .map(t -> startPosition + t.pos());
    if (leftParenPosition.isEmpty() || rightParenPosition.isEmpty()) {
      return unwrapMethodInvocationDroppingWhitespaceAndComments(tree, state);
    }

    return SuggestedFix.replace(
        tree,
        sourceCode
            .subSequence(leftParenPosition.orElseThrow(), rightParenPosition.orElseThrow())
            .toString());
  }

  @VisibleForTesting
  static SuggestedFix unwrapMethodInvocationDroppingWhitespaceAndComments(
      MethodInvocationTree tree, VisitorState state) {
    return SuggestedFix.replace(
        tree,
        tree.getArguments().stream().map(arg -> treeToString(arg, state)).collect(joining(", ")));
  }
}
