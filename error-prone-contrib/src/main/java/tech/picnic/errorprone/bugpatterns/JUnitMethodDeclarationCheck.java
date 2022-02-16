package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.annotations;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.isType;
import static java.util.function.Predicate.not;
import static tech.picnic.errorprone.bugpatterns.JavaKeywords.isJavaKeyword;

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
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import java.util.Objects;
import java.util.Optional;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;

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
    linkType = NONE,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class JUnitMethodDeclarationCheck extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final String TEST_PREFIX = "test";
  private static final ImmutableSet<Modifier> ILLEGAL_MODIFIERS =
      ImmutableSet.of(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC);
  private static final MultiMatcher<MethodTree, AnnotationTree> OVERRIDE_METHOD =
      annotations(AT_LEAST_ONE, isType("java.lang.Override"));
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

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    // XXX: Perhaps we should also skip analysis of non-`private` non-`final` methods in abstract
    // classes?
    if (OVERRIDE_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    boolean isTestMethod = TEST_METHOD.matches(tree, state);
    if (!isTestMethod && !SETUP_OR_TEARDOWN_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder builder = SuggestedFix.builder();
    SuggestedFixes.removeModifiers(tree.getModifiers(), state, ILLEGAL_MODIFIERS)
        .ifPresent(builder::merge);

    if (isTestMethod) {
      tryCanonicalizeMethodName(tree)
          .filter(methodName -> isValidMethodName(tree, methodName, state))
          .ifPresent(
              methodName -> builder.merge(SuggestedFixes.renameMethod(tree, methodName, state)));
    }
    return builder.isEmpty() ? Description.NO_MATCH : describeMatch(tree, builder.build());
  }

  private boolean isValidMethodName(MethodTree tree, String methodName, VisitorState state) {
    if (isMethodNameInClass(methodName, state)) {
      reportIncorrectMethodName(
          methodName, tree, "A method with name %s already exists in the class.", state);
      return false;
    }

    if (isMethodNameStaticallyImported(methodName, state)) {
      reportIncorrectMethodName(
          methodName, tree, "A method with name %s is already statically imported.", state);
      return false;
    }

    if (isJavaKeyword(methodName)) {
      reportIncorrectMethodName(
          methodName,
          tree,
          "Method name `%s` is not possible because it is a Java keyword.",
          state);
      return false;
    }
    return true;
  }

  private void reportIncorrectMethodName(
      String methodName, MethodTree tree, String message, VisitorState state) {
    state.reportMatch(
        buildDescription(tree).setMessage(String.format(message, methodName)).build());
  }

  private static boolean isMethodNameInClass(String methodName, VisitorState state) {
    return state.findEnclosing(ClassTree.class).getMembers().stream()
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast)
        .filter(not(ASTHelpers::isGeneratedConstructor))
        .map(MethodTree::getName)
        .map(Name::toString)
        .anyMatch(methodName::equals);
  }

  private static boolean isMethodNameStaticallyImported(String methodName, VisitorState state) {
    CompilationUnitTree compilationUnit = state.getPath().getCompilationUnit();

    return compilationUnit.getImports().stream()
        .filter(Objects::nonNull)
        .filter(ImportTree::isStatic)
        .map(ImportTree::getQualifiedIdentifier)
        .map(tree -> getStaticImportIdentifier(tree, state))
        .anyMatch(methodName::contentEquals);
  }

  private static CharSequence getStaticImportIdentifier(Tree tree, VisitorState state) {
    String source = Util.treeToString(tree, state);
    return source.subSequence(source.lastIndexOf('.') + 1, source.length());
  }

  private static Optional<String> tryCanonicalizeMethodName(MethodTree tree) {
    return Optional.ofNullable(ASTHelpers.getSymbol(tree))
        .map(sym -> sym.getQualifiedName().toString())
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
