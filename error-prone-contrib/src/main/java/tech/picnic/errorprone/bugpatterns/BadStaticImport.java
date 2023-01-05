package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symbol;

@AutoService(BugChecker.class)
@BugPattern(
    summary = "Identifier should be statically imported",
    link = BUG_PATTERNS_BASE_URL + "StaticImport",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
  public class BadStaticImport extends BugChecker implements BugChecker.MethodInvocationTreeMatcher, BugChecker.IdentifierTreeMatcher {
  private static final long serialVersionUID = 1L;

  @VisibleForTesting
  static final ImmutableSet<String> BAD_STATIC_IMPORT_CANDIDATE_TYPES =
      ImmutableSet.of("com.google.common.base.Strings", "java.time.Clock");

  @VisibleForTesting
  static final ImmutableSetMultimap<String, String> BAD_STATIC_IMPORT_CANDIDATE_MEMBERS =
      ImmutableSetMultimap.<String, String>builder()
          .put("java.util.Locale", "ROOT")
          .put("java.util.Optional", "empty")
          .build();

  @VisibleForTesting
  static final ImmutableSet<String> BAD_STATIC_IMPORT_CANDIDATE_IDENTIFIERS =
      ImmutableSet.of("builder", "copyOf", "MIN", "MAX");

  public BadStaticImport() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    ExpressionTree expr = tree.getMethodSelect();
    switch (expr.getKind()) {
      case IDENTIFIER:
        return matchIdentifier((IdentifierTree) tree.getMethodSelect(), state);
      default:
        return Description.NO_MATCH;
    }
  }

  @Override
  public Description matchIdentifier(IdentifierTree tree, VisitorState state) {
    Symbol symbol = ASTHelpers.getSymbol(tree);
    if (isCandidate(symbol)) {
      return getDescription(tree, state, symbol);
    }

    return Description.NO_MATCH;
  }

  private Description getDescription(IdentifierTree tree, VisitorState state, Symbol symbol) {
    SuggestedFix.Builder builder =
        SuggestedFix.builder().removeStaticImport(getImportToRemove(symbol));
    String replacement =
        SuggestedFixes.qualifyType(state, builder, symbol.getEnclosingElement()) + ".";
    builder.prefixWith(tree, replacement);
    return describeMatch(tree, builder.build());
  }

  private String getImportToRemove(Symbol symbol) {
    return String.format(
        "%s.%s",
        symbol.getEnclosingElement().getQualifiedName().toString(),
        symbol.getSimpleName().toString());
  }

  private static boolean isCandidate(Symbol symbol) {
    String qualifiedName = symbol.getEnclosingElement().getQualifiedName().toString();
    return BAD_STATIC_IMPORT_CANDIDATE_TYPES.contains(qualifiedName)
        || BAD_STATIC_IMPORT_CANDIDATE_MEMBERS.containsEntry(
            qualifiedName, symbol.getSimpleName().toString())
        || BAD_STATIC_IMPORT_CANDIDATE_IDENTIFIERS.contains(symbol.getSimpleName().toString());
  }
}
