package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.ErrorProneComment;
import com.google.errorprone.util.ErrorProneComment.ErrorProneCommentStyle;
import com.google.errorprone.util.ErrorProneToken;
import com.google.errorprone.util.ErrorProneTokens;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import org.jspecify.annotations.Nullable;

/**
 * A {@link BugChecker} that flags non-Javadoc comments placed directly before a Javadoc comment.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Non-Javadoc comments should be placed after the Javadoc comment",
    link = BUG_PATTERNS_BASE_URL + "CommentOrder",
    linkType = CUSTOM,
    severity = WARNING,
    tags = STYLE)
public final class CommentOrder extends BugChecker implements CompilationUnitTreeMatcher {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link CommentOrder} instance. */
  public CommentOrder() {}

  @Override
  public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
    CharSequence source = state.getSourceCode();
    if (source == null) {
      return Description.NO_MATCH;
    }

    String sourceString = source.toString();
    ImmutableList<ErrorProneToken> tokens = ErrorProneTokens.getTokens(sourceString, state.context);

    ImmutableMap<Integer, SuggestedFix> violations = findViolations(tokens, sourceString);

    if (!violations.isEmpty()) {
      new TreeScanner<@Nullable Void, @Nullable Void>() {
        @Override
        public @Nullable Void visitClass(ClassTree node, @Nullable Void unused) {
          reportIfViolation(node);
          return super.visitClass(node, null);
        }

        @Override
        public @Nullable Void visitMethod(MethodTree node, @Nullable Void unused) {
          reportIfViolation(node);
          return super.visitMethod(node, null);
        }

        @Override
        public @Nullable Void visitVariable(VariableTree node, @Nullable Void unused) {
          reportIfViolation(node);
          return super.visitVariable(node, null);
        }

        private void reportIfViolation(Tree node) {
          SuggestedFix fix = violations.get(ASTHelpers.getStartPosition(node));
          if (fix != null) {
            state.reportMatch(describeMatch(node, fix));
          }
        }
      }.scan(tree, null);
    }

    return Description.NO_MATCH;
  }

  private static ImmutableMap<Integer, SuggestedFix> findViolations(
      ImmutableList<ErrorProneToken> tokens, String source) {
    ImmutableMap.Builder<Integer, SuggestedFix> violations = ImmutableMap.builder();

    for (ErrorProneToken token : tokens) {
      ImmutableList<ErrorProneComment> comments = token.comments();
      if (comments.size() < 2) {
        continue;
      }

      for (int i = 0; i < comments.size(); i++) {
        ErrorProneComment comment = comments.get(i);
        if (!isJavadoc(comment)) {
          continue;
        }

        @Var int firstNonJavadocIndex = i;
        for (int j = i - 1; j >= 0; j--) {
          ErrorProneComment prev = comments.get(j);
          ErrorProneComment next = comments.get(j + 1);
          if (isJavadoc(prev) || hasBlankLineBetween(prev, next, source)) {
            break;
          }
          firstNonJavadocIndex = j;
        }

        if (firstNonJavadocIndex == i) {
          continue;
        }

        ErrorProneComment firstNonJavadoc = comments.get(firstNonJavadocIndex);
        ErrorProneComment lastNonJavadoc = comments.get(i - 1);

        String nonJavadocSource =
            source.substring(firstNonJavadoc.getPos(), lastNonJavadoc.getEndPos());
        String javadocSource = source.substring(comment.getPos(), comment.getEndPos());
        String separatingWhitespace =
            source.substring(lastNonJavadoc.getEndPos(), comment.getPos());

        violations.put(
            token.pos(),
            SuggestedFix.replace(
                firstNonJavadoc.getPos(),
                comment.getEndPos(),
                javadocSource + separatingWhitespace + nonJavadocSource));
        break;
      }
    }

    return violations.buildOrThrow();
  }

  private static boolean isJavadoc(ErrorProneComment comment) {
    return comment.getStyle() == ErrorProneCommentStyle.JAVADOC_BLOCK
        || comment.getStyle() == ErrorProneCommentStyle.JAVADOC_LINE;
  }

  private static boolean hasBlankLineBetween(
      ErrorProneComment first, ErrorProneComment second, String source) {
    return CharMatcher.is('\n').countIn(source.substring(first.getEndPos(), second.getPos())) >= 2;
  }
}
