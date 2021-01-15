package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.isType;
import static java.util.function.Predicate.not;

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
import com.google.errorprone.matchers.MultiMatcher;
import com.google.errorprone.predicates.TypePredicate;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Symbol;
import java.util.Optional;
import javax.lang.model.element.Modifier;

/** A {@link BugChecker} which flags non-canonical JUnit method declarations. */
// XXX: Consider introducing a class-level check which enforces that test classes:
// 1. Are named `*Test` or `Abstract*TestCase`.
// 2. If not `abstract`, don't have public methods and subclasses.
// 3. Only have private fields.
// XXX: If implemented, the current logic could flag only `private` JUnit methods.
@AutoService(BugChecker.class)
@BugPattern(
    name = "JUnitMethodDeclaration",
    summary = "JUnit method declaration can likely be improved",
    linkType = BugPattern.LinkType.NONE,
    severity = BugPattern.SeverityLevel.SUGGESTION,
    tags = BugPattern.StandardTags.SIMPLIFICATION)
public final class JUnitMethodDeclarationCheck extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String TEST_PREFIX = "test";
  private static final ImmutableSet<Modifier> ILLEGAL_MODIFIERS =
      ImmutableSet.of(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC);
  private static final MultiMatcher<MethodTree, AnnotationTree> IS_OVERRIDE_METHOD =
      annotations(AT_LEAST_ONE, isType("java.lang.Override"));
  private static final MultiMatcher<MethodTree, AnnotationTree> IS_TEST_METHOD =
      annotations(
          AT_LEAST_ONE,
          anyOf(
              isType("org.junit.jupiter.api.Test"),
              hasMetaAnnotation("org.junit.jupiter.api.TestTemplate")));
  private static final MultiMatcher<MethodTree, AnnotationTree> IS_SETUP_OR_TEARDOWN_METHOD =
      annotations(
          AT_LEAST_ONE,
          anyOf(
              isType("org.junit.jupiter.api.AfterAll"),
              isType("org.junit.jupiter.api.AfterEach"),
              isType("org.junit.jupiter.api.BeforeAll"),
              isType("org.junit.jupiter.api.BeforeEach")));

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    // XXX: Perhaps we should also skip analysis of non-`private` non-`final` methods in abstract
    // classes?
    if (IS_OVERRIDE_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    boolean isTestMethod = IS_TEST_METHOD.matches(tree, state);
    if (!isTestMethod && !IS_SETUP_OR_TEARDOWN_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder builder = SuggestedFix.builder();
    SuggestedFixes.removeModifiers(tree.getModifiers(), state, ILLEGAL_MODIFIERS)
        .ifPresent(builder::merge);

    if (isTestMethod) {
      // XXX: In theory this rename could clash with an existing method or static import. In that
      // case we should emit a warning without a suggested replacement.
      tryCanonicalizeMethodName(tree, state).ifPresent(builder::merge);
    }

    return builder.isEmpty() ? Description.NO_MATCH : describeMatch(tree, builder.build());
  }

  private static Optional<SuggestedFix> tryCanonicalizeMethodName(
      MethodTree tree, VisitorState state) {
    return Optional.ofNullable(ASTHelpers.getSymbol(tree))
        .map(sym -> sym.getQualifiedName().toString())
        .filter(name -> name.startsWith(TEST_PREFIX))
        .map(name -> name.substring(TEST_PREFIX.length()))
        .filter(not(String::isEmpty))
        .map(name -> Character.toLowerCase(name.charAt(0)) + name.substring(1))
        .filter(name -> !Character.isDigit(name.charAt(0)))
        .map(name -> SuggestedFixes.renameMethod(tree, name, state));
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
