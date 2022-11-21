package tech.picnic.errorprone.bugpatterns.util;

import static com.sun.tools.javac.util.Position.NOPOS;

import com.google.common.base.CharMatcher;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;

/**
 * A collection of Error Prone utility methods for dealing with the source code representation of
 * AST nodes.
 */
public final class SourceCode {
  /** The complement of {@link CharMatcher#whitespace()}. */
  private static final CharMatcher NON_WHITESPACE_MATCHER = CharMatcher.whitespace().negate();

  private SourceCode() {}

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
}
