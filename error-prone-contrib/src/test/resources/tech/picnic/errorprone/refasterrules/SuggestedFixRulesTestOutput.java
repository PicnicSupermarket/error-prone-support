package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class SuggestedFixRulesTest implements RefasterRuleCollectionTestCase {
  SuggestedFix.Builder testSuggestedFixBuilderToBuilder() {
    return SuggestedFix.emptyFix().toBuilder();
  }

  SuggestedFix testSuggestedFixBuilderDelete() {
    return SuggestedFix.delete((Tree) null);
  }

  SuggestedFix testSuggestedFixBuilderReplaceTree() {
    return SuggestedFix.replace((Tree) null, "foo");
  }

  SuggestedFix testSuggestedFixBuilderReplaceStartEnd() {
    return SuggestedFix.replace(1, 2, "foo");
  }

  SuggestedFix testSuggestedFixBuilderReplaceTreeStartEnd() {
    return SuggestedFix.replace(null, "foo", 1, 2);
  }

  SuggestedFix testSuggestedFixBuilderSwap() {
    return SuggestedFix.swap((Tree) null, (ExpressionTree) null, (VisitorState) null);
  }

  SuggestedFix testSuggestedFixBuilderPrefixWith() {
    return SuggestedFix.prefixWith((Tree) null, "foo");
  }

  SuggestedFix testSuggestedFixBuilderPostfixWith() {
    return SuggestedFix.postfixWith((Tree) null, "foo");
  }
}
