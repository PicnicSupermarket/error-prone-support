package tech.picnic.errorprone.refasterrules;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class SuggestFixRulesTest implements RefasterRuleCollectionTestCase {
  SuggestedFix testSuggestedFixDelete() {
    return SuggestedFix.builder().delete(null).build();
  }

  SuggestedFix testSuggestedFixReplaceTree() {
    return SuggestedFix.builder().replace(null, "foo").build();
  }

  SuggestedFix testSuggestedFixReplaceStartEnd() {
    return SuggestedFix.builder().replace(1, 2, "foo").build();
  }

  SuggestedFix testSuggestedFixReplaceTreeStartEnd() {
    return SuggestedFix.builder().replace(null, "foo", 1, 2).build();
  }

  SuggestedFix testSuggestedFixSwap() {
    return SuggestedFix.builder().swap((Tree) null, (ExpressionTree) null).build();
  }

  SuggestedFix testSuggestedFixPrefixWith() {
    return SuggestedFix.builder().prefixWith(null, "foo").build();
  }

  SuggestedFix testSuggestedFixPostfixWith() {
    return SuggestedFix.builder().postfixWith(null, "foo").build();
  }
}
