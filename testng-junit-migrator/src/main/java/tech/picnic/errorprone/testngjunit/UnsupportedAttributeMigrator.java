package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import tech.picnic.errorprone.util.SourceCode;

/** A migrator that leaves a comment for attributes that aren't supported in the migration. */
@Immutable
final class UnsupportedAttributeMigrator {
  SuggestedFix createFix(
      String attributeName, MethodTree methodTree, ExpressionTree dataValue, VisitorState state) {
    return SuggestedFix.prefixWith(
        methodTree,
        String.format(
            "// XXX: Attribute `%s` is not supported, value: `%s`%n",
            attributeName, SourceCode.treeToString(dataValue, state)));
  }
}
