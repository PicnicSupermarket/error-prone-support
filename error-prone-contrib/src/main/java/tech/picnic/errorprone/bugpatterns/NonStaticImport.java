package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableTable;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import javax.inject.Inject;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags static imports of type members that should *not* be statically
 * imported.
 *
 * <p>See {@link StaticImportConfig} for how additional candidates can be configured, and how this
 * check interacts with {@link StaticImport}.
 */
// XXX: This check is closely linked to `StaticImport`. Consider merging the two.
// XXX: Add suppression support. If qualification of one more more identifiers is suppressed, then
// the associated static import should *not* be removed.
// XXX: Also introduce logic that disallows statically importing `ZoneOffset.ofHours` and other
// `ofXXX`-style methods.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Member should not be statically imported",
    link = BUG_PATTERNS_BASE_URL + "NonStaticImport",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = STYLE)
@SuppressWarnings({
  "java:S2160" /* Super class equality definition suffices. */,
  "z-key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"
})
public final class NonStaticImport extends BugChecker implements CompilationUnitTreeMatcher {
  private static final long serialVersionUID = 1L;

  private final StaticImportConfig config;

  /** Instantiates a default {@link NonStaticImport} instance. */
  public NonStaticImport() {
    this(new StaticImportConfig(ErrorProneFlags.empty()));
  }

  /**
   * Instantiates a customized {@link NonStaticImport}.
   *
   * @param config The candidate-matching configuration to apply.
   */
  @Inject
  NonStaticImport(StaticImportConfig config) {
    this.config = config;
  }

  @Override
  public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
    ImmutableTable<String, String, UndesiredStaticImport> undesiredStaticImports =
        getUndesiredStaticImports(tree, state);

    if (!undesiredStaticImports.isEmpty()) {
      replaceUndesiredStaticImportUsages(tree, undesiredStaticImports, state);

      for (UndesiredStaticImport staticImport : undesiredStaticImports.values()) {
        state.reportMatch(
            describeMatch(staticImport.importTree(), staticImport.fixBuilder().build()));
      }
    }

    /* Any violations have been flagged against the offending static import statement. */
    return Description.NO_MATCH;
  }

  private ImmutableTable<String, String, UndesiredStaticImport> getUndesiredStaticImports(
      CompilationUnitTree tree, VisitorState state) {
    ImmutableTable.Builder<String, String, UndesiredStaticImport> imports =
        ImmutableTable.builder();
    for (ImportTree importTree : tree.getImports()) {
      Tree qualifiedIdentifier = importTree.getQualifiedIdentifier();
      if (importTree.isStatic() && qualifiedIdentifier instanceof MemberSelectTree memberSelect) {
        String type = SourceCode.treeToString(memberSelect.getExpression(), state);
        String member = memberSelect.getIdentifier().toString();
        if (config.shouldNotBeStaticallyImported(type, member)) {
          imports.put(
              type,
              member,
              new UndesiredStaticImport(
                  importTree, SuggestedFix.builder().removeStaticImport(type + '.' + member)));
        }
      }
    }

    return imports.buildOrThrow();
  }

  private static void replaceUndesiredStaticImportUsages(
      CompilationUnitTree tree,
      ImmutableTable<String, String, UndesiredStaticImport> undesiredStaticImports,
      VisitorState state) {
    new TreeScanner<@Nullable Void, @Nullable Void>() {
      @Override
      public @Nullable Void visitIdentifier(IdentifierTree node, @Nullable Void unused) {
        Symbol symbol = ASTHelpers.getSymbol(node);
        if (symbol != null) {
          UndesiredStaticImport staticImport =
              undesiredStaticImports.get(
                  symbol.owner.getQualifiedName().toString(), symbol.name.toString());
          if (staticImport != null) {
            SuggestedFix.Builder fix = staticImport.fixBuilder();
            fix.prefixWith(node, SuggestedFixes.qualifyType(state, fix, symbol.owner) + '.');
          }
        }

        return super.visitIdentifier(node, null);
      }
    }.scan(tree, null);
  }

  private record UndesiredStaticImport(ImportTree importTree, SuggestedFix.Builder fixBuilder) {}
}
