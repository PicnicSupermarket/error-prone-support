package tech.picnic.errorprone.experimental.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static java.util.Objects.requireNonNull;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.base.VerifyException;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.BlockTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;

/**
 * XXX: Docs: BugChecker that flags blocks that start with a new line. Link corresponding checkstyle
 * rule. XXX: Rename to something more generic, maybe "WhitespaceChecker"?
 */
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

  @SuppressWarnings("LoopOverCharArray")
  private Description match(Tree tree, VisitorState state) {
    String source = requireNonNull(state.getSourceForNode(tree), "Source code");

    @Var boolean isInBlock = false;
    @Var int newLineCount = 0;
    @Var int lastNewLinePos = -1;
    source_loop:
    for (int pos = 0; pos < source.length(); pos++) {
      if (!isInBlock) {
        if (source.charAt(pos) == '{') {
          isInBlock = true;
        }
        continue;
      }
      switch (source.charAt(pos)) {
        case '\n' -> {
          // TODO: Windows? 🤢
          newLineCount++;
          lastNewLinePos = pos;
          continue;
        }
        case ' ', '\t' -> {
          continue;
        }
        default -> {
          break source_loop;
        }
      }
    }

    if (newLineCount < 2) {
      return Description.NO_MATCH;
    }

    if (lastNewLinePos == -1) {
      throw new VerifyException("Big oof (lastNewLinePos == -1)");
    }

    // TODO: Do we the source with the correctly formatted one, or do we replace the redundant
    // whitespace with nothing? Colliding replacements and such.
    // SuggestedFix suggestedFix = SuggestedFix.replace(source.indexOf('{') + 1, lastNewLinePos,
    // "");
    String replacement =
        source.substring(0, source.indexOf('{') + 1) + source.substring(lastNewLinePos);
    SuggestedFix suggestedFix = SuggestedFix.replace(tree, replacement);
    return describeMatch(tree, suggestedFix);
  }
}
