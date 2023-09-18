package tech.picnic.errorprone.bugpatterns.util;

import static com.sun.source.tree.Tree.Kind.*;
import static com.sun.tools.javac.parser.Tokens.TokenKind.RPAREN;
import static java.util.stream.Collectors.joining;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.ErrorProneToken;
import com.google.errorprone.util.ErrorProneTokens;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.Position;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * A collection of Error Prone utility methods for dealing with the source code representation of
 * AST nodes.
 */
public final class SourceCode {
  /** The complement of {@link CharMatcher#whitespace()}. */
  private static final CharMatcher NON_WHITESPACE_MATCHER = CharMatcher.whitespace().negate();

  // XXX: Document.
  // XXX: Once we raise the baseline to JDK 17+, review whether `BINDING_PATTERN` and
  // `GUARDED_PATTERN` should be added.
  // XXX: Review which JDK >17 tree kinds should be added.
  private static final ImmutableSet<Tree.Kind> TREE_KINDS_WITHOUT_EXPLICIT_SYNTAX =
      Sets.immutableEnumSet(COMPILATION_UNIT, MODIFIERS, ERRONEOUS, OTHER);

  private SourceCode() {}

  // XXX: Document that that the test is relative to the state's current path.
  public static boolean isAccurateSourceLikelyAvailable(VisitorState state) {
    return new IsAccurateSourceLikelyAvailable().test(state);
  }

  // XXX: Move this class down or to a separate file.
  // XXX: Cache data in `VisitorState.config`.
  private static final class IsAccurateSourceLikelyAvailable
      extends TreeScanner<@Nullable Boolean, VisitorState> implements Predicate<VisitorState> {
    @Override
    public boolean test(VisitorState state) {
      return isValidAncestorSourceContainment(state) && isValidDescendantSourceContainment(state);
    }

    @Override
    public @Nullable Boolean scan(@Nullable Tree tree, VisitorState parentState) {
      if (tree == null) {
        return Boolean.TRUE;
      }

      VisitorState childState = parentState.withPath(new TreePath(parentState.getPath(), tree));
      return isValidSourceContainment(childState) ? super.scan(tree, childState) : Boolean.FALSE;
    }

    @Override
    public @Nullable Boolean reduce(@Nullable Boolean a, @Nullable Boolean b) {
      return !(Boolean.FALSE.equals(a) || Boolean.FALSE.equals(b));
    }

    private boolean isValidDescendantSourceContainment(VisitorState state) {
      return !Boolean.FALSE.equals(super.scan(state.getPath().getLeaf(), state));
    }

    private static boolean isValidAncestorSourceContainment(VisitorState state) {
      return Stream.iterate(state.getPath(), Objects::nonNull, TreePath::getParentPath)
          .allMatch(path -> isValidSourceContainment(state.withPath(path)));
    }

    private static boolean isValidSourceContainment(VisitorState state) {
      Tree tree = state.getPath().getLeaf();
      TreePath parentPath = state.getPath().getParentPath();
      if (parentPath == null) {
        return getSourceRange(tree, state) != null;
      }

      Tree enclosingTree = parentPath.getLeaf();
      @Nullable Range<Integer> enclosingSourceRange = getSourceRange(enclosingTree, state);
      @Nullable Range<Integer> sourceRange = getSourceRange(tree, state);
      if (enclosingSourceRange == null) {
        return sourceRange == null || mayBeImplicit(enclosingTree, state);
      }

      if (sourceRange == null) {
        return mayBeImplicit(tree, state);
      }

      return enclosingSourceRange.encloses(sourceRange)
          && (TREE_KINDS_WITHOUT_EXPLICIT_SYNTAX.contains(enclosingTree.getKind())
              || !enclosingSourceRange.equals(sourceRange));
    }

    private static boolean mayBeImplicit(Tree tree, VisitorState state) {
      if (tree instanceof ModifiersTree) {
        ModifiersTree modifiers = (ModifiersTree) tree;
        return modifiers.getAnnotations().isEmpty() && modifiers.getFlags().isEmpty();
      }

      if (ASTHelpers.isGeneratedConstructor(state.findEnclosing(MethodTree.class))) {
        return true;
      }

      AnnotationTree annotation = state.findEnclosing(AnnotationTree.class);
      if (annotation != null && annotation.getArguments().size() == 1) {
        Symbol symbol =
            tree instanceof AssignmentTree
                ? ASTHelpers.getSymbol(((AssignmentTree) tree).getVariable())
                : tree instanceof IdentifierTree ? ASTHelpers.getSymbol(tree) : null;

        /* The `value` attribute may be omitted from single-argument annotation declarations. */
        return symbol != null
            && symbol.kind == Kinds.Kind.MTH
            && symbol.name.contentEquals("value");
      }

      // XXX: Many branches aren't covered yet.
      return isSuperInvocation(tree)
          || (tree instanceof IdentifierTree
              && isSuperInvocation(state.getPath().getParentPath().getLeaf()));
    }

    private static boolean isSuperInvocation(Tree tree) {
      if (tree instanceof ExpressionStatementTree) {
        return isSuperInvocation(((ExpressionStatementTree) tree).getExpression());
      }

      if (!(tree instanceof MethodInvocationTree)) {
        return false;
      }

      MethodInvocationTree invocation = (MethodInvocationTree) tree;
      return invocation.getArguments().isEmpty()
          && ASTHelpers.isSuper(invocation.getMethodSelect());
    }

    private static @Nullable Range<Integer> getSourceRange(Tree tree, VisitorState state) {
      int startPosition = ASTHelpers.getStartPosition(tree);
      int endPosition = state.getEndPosition(tree);
      return startPosition == Position.NOPOS || endPosition == Position.NOPOS
          ? null
          : Range.closedOpen(startPosition, endPosition);
    }
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
    if (sourceCode == null || endPos == Position.NOPOS) {
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
