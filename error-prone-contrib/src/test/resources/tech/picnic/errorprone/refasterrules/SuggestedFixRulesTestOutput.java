package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class SuggestedFixRulesTest implements RefasterRuleCollectionTestCase {
  SuggestedFix.Builder testSuggestedFixToBuilder() {
    return SuggestedFix.emptyFix().toBuilder();
  }

  SuggestedFix testSuggestedFixDelete() {
    return SuggestedFix.delete((Tree) null);
  }

  SuggestedFix testSuggestedFixReplace2() {
    return SuggestedFix.replace((Tree) null, "foo");
  }

  SuggestedFix testSuggestedFixReplace3() {
    return SuggestedFix.replace(1, 2, "foo");
  }

  SuggestedFix testSuggestedFixReplace4() {
    return SuggestedFix.replace(null, "foo", 1, 2);
  }

  SuggestedFix testSuggestedFixSwap() {
    return SuggestedFix.swap((Tree) null, (ExpressionTree) null, (VisitorState) null);
  }

  SuggestedFix testSuggestedFixPrefixWith() {
    return SuggestedFix.prefixWith((Tree) null, "foo");
  }

  SuggestedFix testSuggestedFixPostfixWith() {
    return SuggestedFix.postfixWith((Tree) null, "foo");
  }
}
