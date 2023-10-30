package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static tech.picnic.errorprone.bugpatterns.StaticImport.STATIC_IMPORT_CANDIDATE_MEMBERS;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.errorprone.BugPattern;
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
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} that flags static imports of type members that should *not* be statically
 * imported.
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
public final class NonStaticImport extends BugChecker implements CompilationUnitTreeMatcher {
  private static final long serialVersionUID = 1L;

  /**
   * Types whose members should not be statically imported, unless exempted by {@link
   * StaticImport#STATIC_IMPORT_CANDIDATE_MEMBERS}.
   *
   * <p>Types listed here should be mutually exclusive with {@link
   * StaticImport#STATIC_IMPORT_CANDIDATE_TYPES}.
   */
  @VisibleForTesting
  static final ImmutableSet<String> NON_STATIC_IMPORT_CANDIDATE_TYPES =
      ImmutableSet.of(
          "com.google.common.base.Strings",
          "com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode",
          "java.time.Clock",
          "java.time.ZoneOffset");

  /**
   * Type members that should never be statically imported.
   *
   * <p>Please note that:
   *
   * <ul>
   *   <li>Types listed by {@link #NON_STATIC_IMPORT_CANDIDATE_TYPES} and members listed by {@link
   *       #NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS} should be omitted from this collection.
   *   <li>This collection should be mutually exclusive with {@link
   *       StaticImport#STATIC_IMPORT_CANDIDATE_MEMBERS}.
   * </ul>
   */
  // XXX: Perhaps the set of exempted `java.util.Collections` methods is too strict. For now any
  // method name that could be considered "too vague" or could conceivably mean something else in a
  // specific context is left out.
  static final ImmutableSetMultimap<String, String> NON_STATIC_IMPORT_CANDIDATE_MEMBERS =
      ImmutableSetMultimap.<String, String>builder()
          .put("com.google.common.base.Predicates", "contains")
          .putAll(
              "java.util.Collections",
              "addAll",
              "copy",
              "fill",
              "list",
              "max",
              "min",
              "nCopies",
              "rotate",
              "sort",
              "swap")
          .put("java.util.Locale", "ROOT")
          .putAll("java.util.regex.Pattern", "compile", "matches", "quote")
          .put("org.springframework.http.MediaType", "ALL")
          .build();

  /**
   * Identifiers that should never be statically imported.
   *
   * <p>Please note that:
   *
   * <ul>
   *   <li>Identifiers listed by {@link StaticImport#STATIC_IMPORT_CANDIDATE_MEMBERS} should be
   *       mutually exclusive with identifiers listed here.
   *   <li>This list should contain a superset of the identifiers flagged by {@link
   *       com.google.errorprone.bugpatterns.BadImport}.
   * </ul>
   */
  static final ImmutableSet<String> NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS =
      ImmutableSet.of(
          "builder",
          "copyOf",
          "create",
          "empty",
          "from",
          "getDefaultInstance",
          "INSTANCE",
          "MAX",
          "MAX_VALUE",
          "MIN",
          "MIN_VALUE",
          "newBuilder",
          "newInstance",
          "of",
          "ONE",
          "parse",
          "valueOf",
          "ZERO");

  /** Instantiates a new {@link NonStaticImport} instance. */
  public NonStaticImport() {}

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

  private static ImmutableTable<String, String, UndesiredStaticImport> getUndesiredStaticImports(
      CompilationUnitTree tree, VisitorState state) {
    ImmutableTable.Builder<String, String, UndesiredStaticImport> imports =
        ImmutableTable.builder();
    for (ImportTree importTree : tree.getImports()) {
      Tree qualifiedIdentifier = importTree.getQualifiedIdentifier();
      if (importTree.isStatic() && qualifiedIdentifier instanceof MemberSelectTree) {
        MemberSelectTree memberSelectTree = (MemberSelectTree) qualifiedIdentifier;
        String type = SourceCode.treeToString(memberSelectTree.getExpression(), state);
        String member = memberSelectTree.getIdentifier().toString();
        if (shouldNotBeStaticallyImported(type, member)) {
          imports.put(
              type,
              member,
              new AutoValue_NonStaticImport_UndesiredStaticImport(
                  importTree, SuggestedFix.builder().removeStaticImport(type + '.' + member)));
        }
      }
    }

    return imports.build();
  }

  private static boolean shouldNotBeStaticallyImported(String type, String member) {
    return (NON_STATIC_IMPORT_CANDIDATE_TYPES.contains(type)
            && !STATIC_IMPORT_CANDIDATE_MEMBERS.containsEntry(type, member))
        || NON_STATIC_IMPORT_CANDIDATE_MEMBERS.containsEntry(type, member)
        || NON_STATIC_IMPORT_CANDIDATE_IDENTIFIERS.contains(member);
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

        return super.visitIdentifier(node, unused);
      }
    }.scan(tree, null);
  }

  @AutoValue
  abstract static class UndesiredStaticImport {
    abstract ImportTree importTree();

    abstract SuggestedFix.Builder fixBuilder();
  }
}
