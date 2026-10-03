package tech.picnic.errorprone.experimental.bugpatterns;

import static com.google.common.base.Preconditions.checkState;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static java.util.Objects.requireNonNull;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.BlockTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.util.Position;

/**
 * XXX: Docs: BugChecker that flags blocks that start with a new line. Link corresponding checkstyle
 * rule. XXX: Rename to something more generic, maybe "WhitespaceChecker"?
 */
// XXX: Check whitespace in-between members. Enforce different clustering based on the
// members modifiers, e.g. empty line between static and non-static fields.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "XXX",
    link = BUG_PATTERNS_BASE_URL + "BlockStartWhitespace",
    linkType = CUSTOM,
    severity = WARNING,
    tags = STYLE)
public final class BlockStartWhitespace extends BugChecker
    implements BlockTreeMatcher, ClassTreeMatcher {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link BlockStartWhitespace} instance. */
  public BlockStartWhitespace() {}

  @Override
  public Description matchBlock(BlockTree blockTree, VisitorState state) {
    return match(blockTree, state);
  }

  @Override
  public Description matchClass(ClassTree classTree, VisitorState state) {
    return match(classTree, state);
  }

  private Description match(Tree tree, VisitorState state) {
    String source = requireNonNull(state.getSourceForNode(tree), "Source code");

    @Var boolean isInBlock = false;
    @Var int newLineCount = 0;
    @Var int lastNewLinePos = -1;
    // Given a `BlockTree` or a `ClassTree`, find the first `{` marking the start of their body.
    // Comments between the type's first modifier - like `private` - and its body containing
    // `{` break this logic, but doesn't make the code _less_ idiomatic. Using EP Tokens might
    // be an alternative, with them comments at the start of the body require attention.
    for (int pos = 0; pos < source.length(); pos++) {
      // Ignore everything until an opening brace.
      if (!isInBlock) {
        if (source.charAt(pos) == '{') {
          isInBlock = true;
        }
        continue;
      }
      // Count new lines. With `String#startWith` we avoid constructing a new string for each
      // character in the string.
      if (source.startsWith(System.lineSeparator(), pos)) {
        newLineCount++;
        lastNewLinePos = pos;
        continue;
      }
      // Ignore whitespace.
      if (source.charAt(pos) == ' ' || source.charAt(pos) == '\t') {
        continue;
      }
      break;
    }

    if (newLineCount < 2) {
      return Description.NO_MATCH;
    }

    int offset = ASTHelpers.getStartPosition(tree);
    checkState(offset != Position.NOPOS, "tree start position");
    SuggestedFix suggestedFix =
        SuggestedFix.replace(source.indexOf('{') + 1 + offset, lastNewLinePos + offset, "");
    return describeMatch(tree, suggestedFix);
  }
}
