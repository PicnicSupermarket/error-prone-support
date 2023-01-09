package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.util.ASTHelpers.getSymbol;
import static com.sun.tools.javac.code.Kinds.Kind.TYP;
import static tech.picnic.errorprone.bugpatterns.StaticImport.STATIC_IMPORT_CANDIDATE_MEMBERS;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/** A {@link BugChecker} that flags methods and constants that should not be statically imported. */
// XXX: Also introduce checks that disallows the following candidates:
// - `ZoneOffset.ofHours` and other `ofXXX`-style methods.
// - `java.time.Clock`.
// - Several other `java.time` classes.
// - Likely any of `*.{ZERO, ONE, MIX, MAX, MIN_VALUE, MAX_VALUE}`.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Identifier should not be statically imported",
    link = BUG_PATTERNS_BASE_URL + "BadStaticImport",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class BadStaticImport extends BugChecker implements BugChecker.IdentifierTreeMatcher {
  private static final long serialVersionUID = 1L;

  /**
   * Types whose members should not be statically imported, unless exempted by {@link
   * StaticImport#STATIC_IMPORT_CANDIDATE_MEMBERS}.
   *
   * <p>Types listed here should be mutually exclusive with {@link
   * StaticImport#STATIC_IMPORT_CANDIDATE_TYPES}
   */
  static final ImmutableSet<String> BAD_STATIC_IMPORT_CANDIDATE_TYPES =
      ImmutableSet.of("com.google.common.base.Strings", "java.time.Clock", "java.time.ZoneOffset");

  /**
   * Type members that should never be statically imported.
   *
   * <p>Identifiers listed by {@link #BAD_STATIC_IMPORT_CANDIDATE_IDENTIFIERS} should be omitted
   * from this collection.
   *
   * <p>This should be mutually exclusive with {@link StaticImport#STATIC_IMPORT_CANDIDATE_MEMBERS}
   */
  // XXX: Perhaps the set of exempted `java.util.Collections` methods is too strict. For now any
  // method name that could be considered "too vague" or could conceivably mean something else in a
  // specific context is left out.
  static final ImmutableSetMultimap<String, String> BAD_STATIC_IMPORT_CANDIDATE_MEMBERS =
      ImmutableSetMultimap.<String, String>builder()
          .put("com.google.common.base.Predicates", "contains")
          .put("com.mongodb.client.model.Filters", "empty")
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
          .put("java.util.Optional", "empty")
          .putAll("java.util.regex.Pattern", "compile", "matches", "quote")
          .put("org.springframework.http.MediaType", "ALL")
          .build();

  /**
   * Identifiers that should never be statically imported.
   *
   * <p>Identifiers listed by {@link StaticImport#STATIC_IMPORT_CANDIDATE_MEMBERS} should be
   * mutually exclusive with identifiers listed here.
   *
   * <p>This should be a superset of the identifiers flagged by {@link
   * com.google.errorprone.bugpatterns.BadImport}.
   */
  static final ImmutableSet<String> BAD_STATIC_IMPORT_CANDIDATE_IDENTIFIERS =
      ImmutableSet.of(
          "builder",
          "create",
          "copyOf",
          "from",
          "getDefaultInstance",
          "INSTANCE",
          "MIN",
          "MIN_VALUE",
          "MAX",
          "newBuilder",
          "of",
          "valueOf");

  /** Instantiates a new {@link BadStaticImport} instance. */
  public BadStaticImport() {}

  @Override
  public Description matchIdentifier(IdentifierTree tree, VisitorState state) {
    if (isMatch(tree, state)) {
      return getDescription(tree, state);
    }

    return Description.NO_MATCH;
  }

  private Description getDescription(IdentifierTree tree, VisitorState state) {
    Symbol symbol = ASTHelpers.getSymbol(tree);
    SuggestedFix.Builder builder =
        SuggestedFix.builder().removeStaticImport(getImportToRemove(symbol));
    String replacement =
        SuggestedFixes.qualifyType(state, builder, symbol.getEnclosingElement()) + ".";
    builder.prefixWith(tree, replacement);
    return describeMatch(tree, builder.build());
  }

  @SuppressWarnings("NullAway")
  private static String getImportToRemove(Symbol symbol) {
    return String.join(
        ".", symbol.getEnclosingElement().getQualifiedName(), symbol.getSimpleName());
  }

  private static boolean isMatch(IdentifierTree tree, VisitorState state) {
    Symbol symbol = ASTHelpers.getSymbol(tree);
    if (symbol == null) {
      return false;
    }

    Symbol enclosingSymbol = symbol.getEnclosingElement();
    if (enclosingSymbol == null || enclosingSymbol.kind != TYP) {
      return false;
    }

    String identifierName = symbol.getSimpleName().toString();
    if (isDefinedInThisFile(symbol, state.getPath().getCompilationUnit())) {
      return false;
    }
    if (!isIdentifierStaticallyImported(identifierName, state)) {
      return false;
    }

    String qualifiedTypeName = enclosingSymbol.getQualifiedName().toString();
    return !isExempted(qualifiedTypeName, identifierName)
        && isCandidate(qualifiedTypeName, identifierName);
  }

  private static boolean isDefinedInThisFile(Symbol symbol, CompilationUnitTree tree) {
    return tree.getTypeDecls().stream()
        .anyMatch(
            t -> {
              Symbol topLevelClass = getSymbol(t);
              return topLevelClass instanceof Symbol.ClassSymbol
                  && symbol.isEnclosedBy((Symbol.ClassSymbol) topLevelClass);
            });
  }

  private static boolean isCandidate(String qualifiedTypeName, String identifierName) {
    return BAD_STATIC_IMPORT_CANDIDATE_TYPES.contains(qualifiedTypeName)
        || BAD_STATIC_IMPORT_CANDIDATE_MEMBERS.containsEntry(qualifiedTypeName, identifierName)
        || BAD_STATIC_IMPORT_CANDIDATE_IDENTIFIERS.contains(identifierName);
  }

  private static boolean isExempted(String qualifiedTypeName, String identifierName) {
    return STATIC_IMPORT_CANDIDATE_MEMBERS.containsEntry(qualifiedTypeName, identifierName);
  }

  private static boolean isIdentifierStaticallyImported(String identifierName, VisitorState state) {
    return state.getPath().getCompilationUnit().getImports().stream()
        .filter(ImportTree::isStatic)
        .map(ImportTree::getQualifiedIdentifier)
        .map(tree -> getStaticImportIdentifier(tree, state))
        .anyMatch(identifierName::contentEquals);
  }

  private static CharSequence getStaticImportIdentifier(Tree tree, VisitorState state) {
    String source = SourceCode.treeToString(tree, state);
    return source.subSequence(source.lastIndexOf('.') + 1, source.length());
  }
}