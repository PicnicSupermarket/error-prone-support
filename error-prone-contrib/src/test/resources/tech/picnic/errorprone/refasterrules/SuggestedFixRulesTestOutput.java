package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class SuggestFixRulesTest implements RefasterRuleCollectionTestCase {
  SuggestedFix testSuggestedFixReplaceTree() {
    return Suggestions.replace(null, "foo");
  }

  SuggestedFix testSuggestedFixReplaceStartEnd() {
    return Suggestions.replace(1, 2, "foo");
  }

  SuggestedFix testSuggestedFixReplaceTreeStartEnd() {
    return Suggestions.replace(null, "foo", 1, 2);
  }

  SuggestedFix testSuggestedFixSwap() {
    return Suggestions.swap((Tree) null, (ExpressionTree) null);
  }

  SuggestedFix testSuggestedFixPrefixWith() {
    return Suggestions.prefixWith(null, "foo");
  }

  SuggestedFix testSuggestedFixPostfixWith() {
    return Suggestions.postfixWith(null, "foo");
  }

  SuggestedFix testSuggestedFixDelete() {
    return SuggestedFix.delete((Tree) null);
  }
}
