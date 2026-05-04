package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.sun.source.tree.Tree;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link SuggestedFix}es. */
@OnlineDocumentation
final class SuggestedFixRules {
  private SuggestedFixRules() {}

  /** Prefer {@link SuggestedFix#toBuilder()} over more contrived alternatives. */
  static final class SuggestedFixToBuilder {
    @BeforeTemplate
    SuggestedFix.Builder before(SuggestedFix other) {
      return SuggestedFix.builder().merge(other);
    }

    @AfterTemplate
    SuggestedFix.Builder after(SuggestedFix other) {
      return other.toBuilder();
    }
  }

  /** Prefer {@link SuggestedFix#delete(Tree)} over more contrived alternatives. */
  static final class SuggestedFixDelete {
    @BeforeTemplate
    SuggestedFix before(Tree node) {
      return SuggestedFix.builder().delete(node).build();
    }

    @AfterTemplate
    SuggestedFix after(Tree node) {
      return SuggestedFix.delete(node);
    }
  }

  /** Prefer {@link SuggestedFix#replace(Tree, String)} over more contrived alternatives. */
  static final class SuggestedFixReplace2 {
    @BeforeTemplate
    SuggestedFix before(Tree tree, String replaceWith) {
      return SuggestedFix.builder().replace(tree, replaceWith).build();
    }

    @AfterTemplate
    SuggestedFix after(Tree tree, String replaceWith) {
      return SuggestedFix.replace(tree, replaceWith);
    }
  }

  /** Prefer {@link SuggestedFix#replace(int, int, String)} over more contrived alternatives. */
  static final class SuggestedFixReplace3 {
    @BeforeTemplate
    SuggestedFix before(int startPos, int endPos, String replaceWith) {
      return SuggestedFix.builder().replace(startPos, endPos, replaceWith).build();
    }

    @AfterTemplate
    SuggestedFix after(int startPos, int endPos, String replaceWith) {
      return SuggestedFix.replace(startPos, endPos, replaceWith);
    }
  }

  /**
   * Prefer {@link SuggestedFix#replace(Tree, String, int, int)} over more contrived alternatives.
   */
  static final class SuggestedFixReplace4 {
    @BeforeTemplate
    SuggestedFix before(
        Tree node, String replaceWith, int startPosAdjustment, int endPosAdjustment) {
      return SuggestedFix.builder()
          .replace(node, replaceWith, startPosAdjustment, endPosAdjustment)
          .build();
    }

    @AfterTemplate
    SuggestedFix after(
        Tree node, String replaceWith, int startPosAdjustment, int endPosAdjustment) {
      return SuggestedFix.replace(node, replaceWith, startPosAdjustment, endPosAdjustment);
    }
  }

  /**
   * Prefer {@link SuggestedFix#swap(Tree, Tree, VisitorState)} over more contrived alternatives.
   */
  static final class SuggestedFixSwap {
    @BeforeTemplate
    SuggestedFix before(Tree node1, Tree node2, VisitorState state) {
      return SuggestedFix.builder().swap(node1, node2, state).build();
    }

    @AfterTemplate
    SuggestedFix after(Tree node1, Tree node2, VisitorState state) {
      return SuggestedFix.swap(node1, node2, state);
    }
  }

  /** Prefer {@link SuggestedFix#prefixWith(Tree, String)} over more contrived alternatives. */
  static final class SuggestedFixPrefixWith {
    @BeforeTemplate
    SuggestedFix before(Tree node, String prefix) {
      return SuggestedFix.builder().prefixWith(node, prefix).build();
    }

    @AfterTemplate
    SuggestedFix after(Tree node, String prefix) {
      return SuggestedFix.prefixWith(node, prefix);
    }
  }

  /** Prefer {@link SuggestedFix#postfixWith(Tree, String)} over more contrived alternatives. */
  static final class SuggestedFixPostfixWith {
    @BeforeTemplate
    SuggestedFix before(Tree node, String postfix) {
      return SuggestedFix.builder().postfixWith(node, postfix).build();
    }

    @AfterTemplate
    SuggestedFix after(Tree node, String postfix) {
      return SuggestedFix.postfixWith(node, postfix);
    }
  }
}
