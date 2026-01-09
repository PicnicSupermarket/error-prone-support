package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class SuggestedFixRulesTest implements RefasterRuleCollectionTestCase {
  SuggestedFix.Builder testSuggestedFixToBuilder() {
    return SuggestedFix.builder().merge(SuggestedFix.emptyFix());
  }

  SuggestedFix testSuggestedFixDelete() {
    return SuggestedFix.builder().delete((Tree) null).build();
  }

  SuggestedFix testSuggestedFixReplaceTree() {
    return SuggestedFix.builder().replace((Tree) null, "foo").build();
  }

  SuggestedFix testSuggestedFixReplaceStartEnd() {
    return SuggestedFix.builder().replace(1, 2, "foo").build();
  }

  SuggestedFix testSuggestedFixReplaceTreeStartEnd() {
    return SuggestedFix.builder().replace(null, "foo", 1, 2).build();
  }

  SuggestedFix testSuggestedFixSwap() {
    return SuggestedFix.builder()
        .swap((Tree) null, (ExpressionTree) null, (VisitorState) null)
        .build();
  }

  SuggestedFix testSuggestedFixPrefixWith() {
    return SuggestedFix.builder().prefixWith((Tree) null, "foo").build();
  }

  SuggestedFix testSuggestedFixPostfixWith() {
    return SuggestedFix.builder().postfixWith((Tree) null, "foo").build();
  }
}
