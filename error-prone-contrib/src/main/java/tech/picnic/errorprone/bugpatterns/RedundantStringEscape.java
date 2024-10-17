package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.LiteralTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.LiteralTree;
import tech.picnic.errorprone.utils.SourceCode;

/** A {@link BugChecker} that flags string constants with extraneous escaping. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Inside string expressions single quotes do not need to be escaped",
    link = BUG_PATTERNS_BASE_URL + "RedundantStringEscape",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class RedundantStringEscape extends BugChecker implements LiteralTreeMatcher {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link RedundantStringEscape} instance. */
  public RedundantStringEscape() {}

  @Override
  public Description matchLiteral(LiteralTree tree, VisitorState state) {
    String constant = ASTHelpers.constValue(tree, String.class);
    if (constant == null || constant.indexOf('\'') < 0) {
      /* Fast path: this isn't a string constant with a single quote. */
      return Description.NO_MATCH;
    }

    String source = SourceCode.treeToString(tree, state);
    if (!containsBannedEscapeSequence(source)) {
      /* Semi-fast path: this expression doesn't contain an escaped single quote. */
      return Description.NO_MATCH;
    }

    /* Slow path: suggest dropping the escape characters. */
    return describeMatch(tree, SuggestedFix.replace(tree, dropRedundantEscapeSequences(source)));
  }

  /**
   * Tells whether the given string constant source expression contains an escaped single quote.
   *
   * @implNote As the input is a literal Java string expression, it will start and end with a double
   *     quote; as such any found backslash will not be the string's final character.
   */
  private static boolean containsBannedEscapeSequence(String source) {
    for (int p = source.indexOf('\\'); p != -1; p = source.indexOf('\\', p + 2)) {
      if (source.charAt(p + 1) == '\'') {
        return true;
      }
    }

    return false;
  }

  /**
   * Simplifies the given string constant source expression by dropping the backslash preceding an
   * escaped single quote.
   *
   * @implNote Note that this method does not delegate to {@link
   *     SourceCode#toStringConstantExpression}, as that operation may replace other Unicode
   *     characters with their associated escape sequence.
   * @implNote As the input is a literal Java string expression, it will start and end with a double
   *     quote; as such any found backslash will not be the string's final character.
   */
  private static String dropRedundantEscapeSequences(String source) {
    StringBuilder result = new StringBuilder();

    for (int p = 0; p < source.length(); p++) {
      char c = source.charAt(p);
      if (c != '\\' || source.charAt(p + 1) != '\'') {
        result.append(c);
      }
    }

    return result.toString();
  }
}
