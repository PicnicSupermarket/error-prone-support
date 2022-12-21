package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.enclosingClass;
import static com.google.errorprone.matchers.Matchers.hasModifier;
import static com.google.errorprone.matchers.Matchers.not;
import static java.util.function.Predicate.not;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.bugpatterns.util.JavaKeywords.isValidIdentifier;
import static tech.picnic.errorprone.bugpatterns.util.MoreJUnitMatchers.SETUP_OR_TEARDOWN_METHOD;
import static tech.picnic.errorprone.bugpatterns.util.MoreJUnitMatchers.TEST_METHOD;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
import java.util.Optional;
import javax.lang.model.element.Modifier;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/** A {@link BugChecker} that flags non-canonical JUnit method declarations. */
// XXX: Consider introducing a class-level check that enforces that test classes:
// 1. Are named `*Test` or `Abstract*TestCase`.
// 2. If not `abstract`, are package-private and don't have public methods and subclasses.
// 3. Only have private fields.
// XXX: If implemented, the current logic could flag only `private` JUnit methods.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "JUnit method declaration can likely be improved",
    link = BUG_PATTERNS_BASE_URL + "JUnitMethodDeclaration",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class JUnitMethodDeclaration extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String TEST_PREFIX = "test";
  private static final ImmutableSet<Modifier> ILLEGAL_MODIFIERS =
      Sets.immutableEnumSet(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC);
  private static final Matcher<MethodTree> IS_LIKELY_OVERRIDDEN =
      allOf(
          not(hasModifier(Modifier.FINAL)),
          not(hasModifier(Modifier.PRIVATE)),
          enclosingClass(hasModifier(Modifier.ABSTRACT)));

  /** Instantiates a new {@link JUnitMethodDeclaration} instance. */
  public JUnitMethodDeclaration() {}

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (IS_LIKELY_OVERRIDDEN.matches(tree, state) || isOverride(tree, state)) {
      return Description.NO_MATCH;
    }

    boolean isTestMethod = TEST_METHOD.matches(tree, state);
    if (!isTestMethod && !SETUP_OR_TEARDOWN_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder fixBuilder = SuggestedFix.builder();
    SuggestedFixes.removeModifiers(tree.getModifiers(), state, ILLEGAL_MODIFIERS)
        .ifPresent(fixBuilder::merge);

    if (isTestMethod) {
      suggestTestMethodRenameIfApplicable(tree, fixBuilder, state);
    }

    return fixBuilder.isEmpty() ? Description.NO_MATCH : describeMatch(tree, fixBuilder.build());
  }

  private void suggestTestMethodRenameIfApplicable(
      MethodTree tree, SuggestedFix.Builder fixBuilder, VisitorState state) {
    MethodSymbol symbol = ASTHelpers.getSymbol(tree);
    tryCanonicalizeMethodName(symbol)
        .ifPresent(
            newName ->
                findMethodRenameBlocker(symbol, newName, state)
                    .ifPresentOrElse(
                        blocker -> reportMethodRenameBlocker(tree, blocker, state),
                        () -> fixBuilder.merge(SuggestedFixes.renameMethod(tree, newName, state))));
  }

  private void reportMethodRenameBlocker(MethodTree tree, String reason, VisitorState state) {
    state.reportMatch(
        buildDescription(tree)
            .setMessage(
                String.format(
                    "This method's name should not redundantly start with `%s` (but note that %s)",
                    TEST_PREFIX, reason))
            .build());
  }

  /**
   * If applicable, returns a human-readable argument against assigning the given name to an
   * existing method.
   *
   * <p>This method implements imperfect heuristics. Things it currently does not consider include
   * the following:
   *
   * <ul>
   *   <li>Whether the rename would merely introduce a method overload, rather than clashing with an
   *       existing method declaration.
   *   <li>Whether the rename would cause a method in a superclass to be overridden.
   *   <li>Whether the rename would in fact clash with a static import. (It could be that a static
   *       import of the same name is only referenced from lexical scopes in which the method under
   *       consideration cannot be referenced directly.)
   * </ul>
   */
  private static Optional<String> findMethodRenameBlocker(
      MethodSymbol method, String newName, VisitorState state) {
    if (isExistingMethodName(method.owner.type, newName, state)) {
      return Optional.of(
          String.format(
              "a method named `%s` is already defined in this class or a supertype", newName));
    }

    if (isSimpleNameStaticallyImported(newName, state)) {
      return Optional.of(String.format("`%s` is already statically imported", newName));
    }

    if (!isValidIdentifier(newName)) {
      return Optional.of(String.format("`%s` is not a valid identifier", newName));
    }

    return Optional.empty();
  }

  private static boolean isExistingMethodName(Type clazz, String name, VisitorState state) {
    return ASTHelpers.matchingMethods(state.getName(name), method -> true, clazz, state.getTypes())
        .findAny()
        .isPresent();
  }

  private static boolean isSimpleNameStaticallyImported(String simpleName, VisitorState state) {
    return state.getPath().getCompilationUnit().getImports().stream()
        .filter(ImportTree::isStatic)
        .map(ImportTree::getQualifiedIdentifier)
        .map(tree -> getStaticImportSimpleName(tree, state))
        .anyMatch(simpleName::contentEquals);
  }

  private static CharSequence getStaticImportSimpleName(Tree tree, VisitorState state) {
    String source = SourceCode.treeToString(tree, state);
    return source.subSequence(source.lastIndexOf('.') + 1, source.length());
  }

  private static Optional<String> tryCanonicalizeMethodName(Symbol symbol) {
    return Optional.of(symbol.getQualifiedName().toString())
        .filter(name -> name.startsWith(TEST_PREFIX))
        .map(name -> name.substring(TEST_PREFIX.length()))
        .filter(not(String::isEmpty))
        .map(name -> Character.toLowerCase(name.charAt(0)) + name.substring(1))
        .filter(name -> !Character.isDigit(name.charAt(0)));
  }

  private static boolean isOverride(MethodTree tree, VisitorState state) {
    return ASTHelpers.streamSuperMethods(ASTHelpers.getSymbol(tree), state.getTypes())
        .findAny()
        .isPresent();
  }
}
