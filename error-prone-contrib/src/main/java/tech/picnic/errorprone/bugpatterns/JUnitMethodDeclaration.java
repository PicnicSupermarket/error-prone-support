package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.enclosingClass;
import static com.google.errorprone.matchers.Matchers.hasModifier;
import static com.google.errorprone.matchers.Matchers.isType;
import static java.util.function.Predicate.not;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.bugpatterns.util.JavaKeywords.isReservedKeyword;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.predicates.TypePredicate;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import java.util.Optional;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
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
      ImmutableSet.of(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC);
  private static final Matcher<MethodTree> HAS_UNMODIFIABLE_SIGNATURE =
      anyOf(
          annotations(AT_LEAST_ONE, isType("java.lang.Override")),
          allOf(
              Matchers.not(hasModifier(Modifier.FINAL)),
              Matchers.not(hasModifier(Modifier.PRIVATE)),
              enclosingClass(hasModifier(Modifier.ABSTRACT))));
  private static final MultiMatcher<MethodTree, AnnotationTree> TEST_METHOD =
      annotations(
          AT_LEAST_ONE,
          anyOf(
              isType("org.junit.jupiter.api.Test"),
              hasMetaAnnotation("org.junit.jupiter.api.TestTemplate")));
  private static final MultiMatcher<MethodTree, AnnotationTree> SETUP_OR_TEARDOWN_METHOD =
      annotations(
          AT_LEAST_ONE,
          anyOf(
              isType("org.junit.jupiter.api.AfterAll"),
              isType("org.junit.jupiter.api.AfterEach"),
              isType("org.junit.jupiter.api.BeforeAll"),
              isType("org.junit.jupiter.api.BeforeEach")));

  /** Instantiates a new {@link JUnitMethodDeclaration} instance. */
  public JUnitMethodDeclaration() {}

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (HAS_UNMODIFIABLE_SIGNATURE.matches(tree, state)) {
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
    tryCanonicalizeMethodName(tree)
        .ifPresent(
            newName ->
                findMethodRenameBlocker(newName, state)
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
  private static Optional<String> findMethodRenameBlocker(String methodName, VisitorState state) {
    if (isMethodInEnclosingClass(methodName, state)) {
      return Optional.of(
          String.format("a method named `%s` already exists in this class", methodName));
    }

    if (isSimpleNameStaticallyImported(methodName, state)) {
      return Optional.of(String.format("`%s` is already statically imported", methodName));
    }

    if (isReservedKeyword(methodName)) {
      return Optional.of(String.format("`%s` is a reserved keyword", methodName));
    }

    return Optional.empty();
  }

  private static boolean isMethodInEnclosingClass(String methodName, VisitorState state) {
    return state.findEnclosing(ClassTree.class).getMembers().stream()
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast)
        .map(MethodTree::getName)
        .map(Name::toString)
        .anyMatch(methodName::equals);
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

  private static Optional<String> tryCanonicalizeMethodName(MethodTree tree) {
    return Optional.of(ASTHelpers.getSymbol(tree).getQualifiedName().toString())
        .filter(name -> name.startsWith(TEST_PREFIX))
        .map(name -> name.substring(TEST_PREFIX.length()))
        .filter(not(String::isEmpty))
        .map(name -> Character.toLowerCase(name.charAt(0)) + name.substring(1))
        .filter(name -> !Character.isDigit(name.charAt(0)));
  }

  // XXX: Move to a `MoreMatchers` utility class.
  private static Matcher<AnnotationTree> hasMetaAnnotation(String annotationClassName) {
    TypePredicate typePredicate = hasAnnotation(annotationClassName);
    return (tree, state) -> {
      Symbol sym = ASTHelpers.getSymbol(tree);
      return sym != null && typePredicate.apply(sym.type, state);
    };
  }

  // XXX: Move to a `MoreTypePredicates` utility class.
  private static TypePredicate hasAnnotation(String annotationClassName) {
    return (type, state) -> ASTHelpers.hasAnnotation(type.tsym, annotationClassName, state);
  }
}
