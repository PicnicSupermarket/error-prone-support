package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class SuggestedFixRulesTest implements RefasterRuleCollectionTestCase {
  SuggestedFix.Builder testSuggestedFixBuilderToBuilder() {
    return SuggestedFix.builder().merge(SuggestedFix.emptyFix());
  }

  SuggestedFix testSuggestedFixBuilderDelete() {
    return SuggestedFix.builder().delete((Tree) null).build();
  }

  SuggestedFix testSuggestedFixBuilderReplaceTree() {
    return SuggestedFix.builder().replace((Tree) null, "foo").build();
  }

  SuggestedFix testSuggestedFixBuilderReplaceStartEnd() {
    return SuggestedFix.builder().replace(1, 2, "foo").build();
  }

  SuggestedFix testSuggestedFixBuilderReplaceTreeStartEnd() {
    return SuggestedFix.builder().replace(null, "foo", 1, 2).build();
  }

  SuggestedFix testSuggestedFixBuilderSwap() {
    return SuggestedFix.builder()
        .swap((Tree) null, (ExpressionTree) null, (VisitorState) null)
        .build();
  }

  SuggestedFix testSuggestedFixBuilderPrefixWith() {
    return SuggestedFix.builder().prefixWith((Tree) null, "foo").build();
  }

  SuggestedFix testSuggestedFixBuilderPostfixWith() {
    return SuggestedFix.builder().postfixWith((Tree) null, "foo").build();
  }
}
