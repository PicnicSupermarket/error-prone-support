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
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import java.util.Optional;
import javax.lang.model.element.Modifier;
import tech.picnic.errorprone.bugpatterns.util.ConflictDetection;

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
      trySuggestTestMethodRename(tree, fixBuilder, state);
    }

    return fixBuilder.isEmpty() ? Description.NO_MATCH : describeMatch(tree, fixBuilder.build());
  }

  private void trySuggestTestMethodRename(
      MethodTree tree, SuggestedFix.Builder fixBuilder, VisitorState state) {
    MethodSymbol symbol = ASTHelpers.getSymbol(tree);
    tryCanonicalizeMethodName(symbol)
        .ifPresent(
            newName ->
                ConflictDetection.findMethodRenameBlocker(symbol, newName, state)
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

  private static Optional<String> tryCanonicalizeMethodName(MethodSymbol symbol) {
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
