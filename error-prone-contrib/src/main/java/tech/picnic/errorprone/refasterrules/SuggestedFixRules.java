package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.sun.source.tree.Tree;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link SuggestedFix}es. */
@OnlineDocumentation
final class SuggestedFixRules {
  private SuggestedFixRules() {}

  /** Prefer {@link SuggestedFix#delete(Tree)} over more contrived alternatives. */
  static final class SuggestedFixDelete {
    @BeforeTemplate
    SuggestedFix before(Tree tree) {
      return SuggestedFix.builder().delete(tree).build();
    }

    @AfterTemplate
    SuggestedFix after(Tree tree) {
      return SuggestedFix.delete(tree);
    }
  }

  /** Prefer {@link SuggestedFix#replace(Tree, String)}} over more contrived alternatives. */
  static final class SuggestedFixReplaceTree {
    @BeforeTemplate
    SuggestedFix before(Tree tree, String replaceWith) {
      return SuggestedFix.builder().replace(tree, replaceWith).build();
    }

    @AfterTemplate
    SuggestedFix after(Tree tree, String replaceWith) {
      return SuggestedFix.replace(tree, replaceWith);
    }
  }

  /** Prefer {@link SuggestedFix#replace(int, int, String)}} over more contrived alternatives. */
  static final class SuggestedFixReplaceStartEnd {
    @BeforeTemplate
    SuggestedFix before(int start, int end, String replaceWith) {
      return SuggestedFix.builder().replace(start, end, replaceWith).build();
    }

    @AfterTemplate
    SuggestedFix after(int start, int end, String replaceWith) {
      return SuggestedFix.replace(start, end, replaceWith);
    }
  }

  /**
   * Prefer {@link SuggestedFix#replace(Tree, String, int, int)}} over more contrived alternatives.
   */
  static final class SuggestedFixReplaceTreeStartEnd {
    @BeforeTemplate
    SuggestedFix before(Tree tree, String replaceWith, int start, int end) {
      return SuggestedFix.builder().replace(tree, replaceWith, start, end).build();
    }

    @AfterTemplate
    SuggestedFix after(Tree tree, String replaceWith, int start, int end) {
      return SuggestedFix.replace(tree, replaceWith, start, end);
    }
  }

  /** Prefer {@link SuggestedFix#swap(Tree, Tree)} over more contrived alternatives. */
  static final class SuggestedFixSwap {
    @BeforeTemplate
    SuggestedFix before(Tree tree1, Tree tree2) {
      return SuggestedFix.builder().swap(tree1, tree2).build();
    }

    @AfterTemplate
    SuggestedFix after(Tree tree1, Tree tree2) {
      return SuggestedFix.swap(tree1, tree2);
    }
  }

  /** Prefer {@link SuggestedFix#prefixWith(Tree, String)} over more contrived alternatives. */
  static final class SuggestedFixPrefixWith {
    @BeforeTemplate
    SuggestedFix before(Tree tree, String prefix) {
      return SuggestedFix.builder().prefixWith(tree, prefix).build();
    }

    @AfterTemplate
    SuggestedFix after(Tree tree, String prefix) {
      return SuggestedFix.prefixWith(tree, prefix);
    }
  }

  /** Prefer {@link SuggestedFix#postfixWith(Tree, String)}} over more contrived alternatives. */
  static final class SuggestedFixPostfixWith {
    @BeforeTemplate
    SuggestedFix before(Tree tree, String postfix) {
      return SuggestedFix.builder().postfixWith(tree, postfix).build();
    }

    @AfterTemplate
    SuggestedFix after(Tree tree, String postfix) {
      return SuggestedFix.postfixWith(tree, postfix);
    }
  }
}
