package tech.picnic.errorprone.refasterrules.output;

import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class SuggestedFixRulesTest implements RefasterRuleCollectionTestCase {
  SuggestedFix testSuggestedFixDelete() {
    return SuggestedFix.delete(null);
  }

  SuggestedFix testSuggestedFixReplaceTree() {
    return SuggestedFix.replace(null, "foo");
  }

  SuggestedFix testSuggestedFixReplaceStartEnd() {
    return SuggestedFix.replace(1, 2, "foo");
  }

  SuggestedFix testSuggestedFixReplaceTreeStartEnd() {
    return SuggestedFix.replace(null, "foo", 1, 2);
  }

  SuggestedFix testSuggestedFixSwap() {
    return SuggestedFix.swap((Tree) null, (ExpressionTree) null);
  }

  SuggestedFix testSuggestedFixPrefixWith() {
    return SuggestedFix.prefixWith(null, "foo");
  }

  SuggestedFix testSuggestedFixPostfixWith() {
    return SuggestedFix.postfixWith(null, "foo");
  }
}
